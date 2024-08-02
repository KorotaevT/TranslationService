package ru.cs.korotaev.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.repository.TranslationRequestRepository
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
class TranslationService(
    private val restTemplate: RestTemplate,
    private val translationRequestRepository: TranslationRequestRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${translation.api.url}") private val apiUrl: String,
    @Value("\${translation.api.detect.url}") private val detectApiUrl: String,
    @Value("\${translation.api.languages.url}") private val languagesApiUrl: String,
    @Value("\${translation.api.key}") private val apiKey: String
) {

    private val executorService = Executors.newFixedThreadPool(10)

    fun translate(sourceLang: String, targetLang: String, text: String, ipAddress: String): ResponseEntity<String> {
        val words = text.split(" ")

        val futures: List<Future<String>> = words.map { word ->
            executorService.submit<String> { translateWord(sourceLang, targetLang, word) }
        }

        val translatedWords = futures.map { it.get() }
        val translatedText = translatedWords.joinToString(" ")

        saveTranslationRequest(ipAddress, text, translatedText)

        return ResponseEntity.ok(translatedText)
    }

    fun detectLanguage(text: String, ipAddress: String): ResponseEntity<String> {
        return performPostRequest(detectApiUrl, mapOf("text" to text))
    }

    fun getSupportedLanguages(): ResponseEntity<String> {
        return performPostRequest(languagesApiUrl, emptyMap())
    }

    private fun translateWord(sourceLang: String, targetLang: String, word: String): String {
        val headers = createHeaders()
        val requestBody = mapOf(
            "sourceLanguageCode" to sourceLang,
            "targetLanguageCode" to targetLang,
            "texts" to listOf(word)
        )
        val request = HttpEntity(requestBody, headers)
        val response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String::class.java)
        val responseBody = response.body ?: throw IllegalStateException("Empty response body")
        return extractTranslatedText(responseBody)
    }

    private fun extractTranslatedText(responseBody: String): String {
        val jsonNode: JsonNode = objectMapper.readTree(responseBody)
        return jsonNode.path("translations").first().path("text").asText()
    }

    private fun saveTranslationRequest(ipAddress: String, sourceText: String, translatedText: String) {
        val translationRequest = TranslationRequest(
            id = 0,
            ipAddress = ipAddress,
            sourceText = sourceText,
            translatedText = translatedText,
            requestTime = LocalDateTime.now()
        )
        translationRequestRepository.save(translationRequest)
    }

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
        }
    }

    private fun performPostRequest(url: String, requestBody: Map<String, Any>): ResponseEntity<String> {
        val headers = createHeaders()
        val request = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, String::class.java)
            ResponseEntity.ok(response.body)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body("Внутренняя ошибка сервера: ${ex.message}")
        }
    }

}