package ru.cs.korotaev.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.repository.TranslationRequestRepository
import java.time.LocalDateTime

@Service
class TranslationService(
    private val restTemplate: RestTemplate,
    private val translationRequestRepository: TranslationRequestRepository,
    private val objectMapper: ObjectMapper
) {

    private val apiUrl = "https://translate.api.cloud.yandex.net/translate/v2/translate"
    private val detectApiUrl = "https://translate.api.cloud.yandex.net/translate/v2/detect"
    private val languagesApiUrl = "https://translate.api.cloud.yandex.net/translate/v2/languages"
    private val apiKey = "AQVNzrcNaCyDyi6GljtCEsy0347VWICDPRbjwFai"

    fun translate(sourceLang: String, targetLang: String, text: String, ipAddress: String): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
        }

        val requestBody = mapOf(
            "sourceLanguageCode" to sourceLang,
            "targetLanguageCode" to targetLang,
            "texts" to listOf(text)
        )

        val request = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String::class.java)
            val responseBody = response.body ?: throw IllegalStateException("Empty response body")

            val jsonNode: JsonNode = objectMapper.readTree(responseBody)
            val translatedText = jsonNode.path("translations").first().path("text").asText()

            val translationRequest = TranslationRequest(
                id = 0,
                ipAddress = ipAddress,
                sourceText = text,
                translatedText = translatedText,
                requestTime = LocalDateTime.now()
            )
            translationRequestRepository.save(translationRequest)

            ResponseEntity.ok(translatedText)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body("Внутренняя ошибка сервера: ${ex.message}")
        }
    }

    fun detectLanguage(text: String, ipAddress: String): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
        }

        val requestBody = mapOf("text" to text)

        val request = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.exchange(detectApiUrl, HttpMethod.POST, request, String::class.java)
            val responseBody = response.body ?: throw IllegalStateException("Empty response body")

            ResponseEntity.ok(responseBody)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body("Внутренняя ошибка сервера: ${ex.message}")
        }
    }

    fun getSupportedLanguages(): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
        }

        val request = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(languagesApiUrl, HttpMethod.POST, request, String::class.java)
            ResponseEntity.ok(response.body)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body("Внутренняя ошибка сервера: ${ex.message}")
        }
    }

}