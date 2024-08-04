package ru.cs.korotaev.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.cs.korotaev.exception.LanguageNotFoundException
import ru.cs.korotaev.exception.TranslationServiceException
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
@EnableCaching
class TranslationService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${translation.api.url}") private val apiUrl: String,
    @Value("\${translation.api.key}") private val apiKey: String,
    @Value("\${translation.api.detect.url}") private val detectApiUrl: String,
    @Value("\${translation.api.languages.url}") private val languagesApiUrl: String,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val executorService = Executors.newFixedThreadPool(10)
    private val logger = LoggerFactory.getLogger(TranslationService::class.java)

    @Cacheable(value = ["translations"], key = "#sourceLang + ':' + #targetLang + ':' + #text")
    fun getTranslationFromApi(sourceLang: String, targetLang: String, text: String): String {
        logger.info("Fetching translation from API for sourceLang=$sourceLang, targetLang=$targetLang, text=$text")
        val words = text.split(" ")

        val futures: List<Future<String>> = words.map { word ->
            executorService.submit<String> { translateWord(sourceLang, targetLang, word) }
        }

        val translatedWords = futures.map { it.get() }
        val translatedText = translatedWords.joinToString(" ")

        publishMessageToRedis("redis", translatedText)

        return translatedText
    }

    fun detectLanguage(text: String): String {
        logger.info("Detecting language for text: $text")
        return performPostRequest(detectApiUrl, mapOf("text" to text))
    }

    fun getSupportedLanguages(): String {
        logger.info("Fetching supported languages")
        return performPostRequest(languagesApiUrl, emptyMap())
    }

    private fun translateWord(sourceLang: String, targetLang: String, word: String): String {
        logger.debug("Translating word: $word from $sourceLang to $targetLang")
        val headers = createHeaders()
        val requestBody = mapOf(
            "sourceLanguageCode" to sourceLang,
            "targetLanguageCode" to targetLang,
            "texts" to listOf(word)
        )
        val request = HttpEntity(requestBody, headers)

        val response = try {
            restTemplate.exchange(apiUrl, HttpMethod.POST, request, String::class.java)
        } catch (ex: HttpClientErrorException) {
            val responseBody = ex.responseBodyAsString
            val jsonNode: JsonNode = objectMapper.readTree(responseBody)
            val errorMessage = jsonNode.path("message").asText()
            if (errorMessage.contains("unsupported target_language_code", ignoreCase = true)) {
                throw LanguageNotFoundException()
            } else {
                throw TranslationServiceException()
            }
        }

        val responseBody = response.body ?: throw TranslationServiceException()
        val jsonNode: JsonNode = objectMapper.readTree(responseBody)
        val translation = jsonNode.path("translations").firstOrNull()?.path("text")?.asText()

        return translation ?: throw LanguageNotFoundException()
    }

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
        }
    }

    private fun performPostRequest(url: String, requestBody: Map<String, Any>): String {
        val headers = createHeaders()
        val request = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, String::class.java)
            response.body ?: throw TranslationServiceException()
        } catch (ex: Exception) {
            logger.error("Error performing POST request to $url", ex)
            throw TranslationServiceException()
        }
    }

    fun publishMessageToRedis(topic: String, message: String) {
        redisTemplate.convertAndSend(topic, message)
    }

}