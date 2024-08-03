package ru.cs.korotaev.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.cs.korotaev.exception.LanguageNotFoundException
import ru.cs.korotaev.exception.TranslationServiceException
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.repository.TranslationRequestRepository
import java.time.LocalDateTime

@Service
class TranslationRequestService(
    private val translationRequestRepository: TranslationRequestRepository,
    private val translationService: TranslationService,
) {

    private val logger = LoggerFactory.getLogger(TranslationRequestService::class.java)

    fun translate(sourceLang: String, targetLang: String, text: String, ipAddress: String): String {
        logger.info("Translating text: $text from $sourceLang to $targetLang for IP: $ipAddress")
        return try {
            val translatedText = translationService.getTranslationFromApi(sourceLang, targetLang, text)
            saveTranslationRequest(ipAddress, text, translatedText)
            translatedText
        } catch (ex: LanguageNotFoundException) {
            throw ex
        } catch (ex: TranslationServiceException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Unexpected error", ex)
            throw ex
        }
    }

    fun detectLanguage(text: String): String {
        return translationService.detectLanguage(text)
    }

    fun getSupportedLanguages(): String {
        return translationService.getSupportedLanguages()
    }

    fun getAllRequests(): List<TranslationRequest> {
        logger.info("Fetching all translation requests")
        return translationRequestRepository.findAll()
    }

    fun getRequestById(id: Long): TranslationRequest? {
        logger.info("Fetching translation request by ID: $id")
        return translationRequestRepository.findById(id)
    }

    fun getRequestsByIp(ipAddress: String): List<TranslationRequest> {
        logger.info("Fetching translation requests by IP: $ipAddress")
        return translationRequestRepository.findByIpAddress(ipAddress)
    }

    fun getRequestsByTextPart(textPart: String): List<TranslationRequest> {
        logger.info("Fetching translation requests by text part: $textPart")
        return translationRequestRepository.findByTextPart(textPart)
    }

    private fun saveTranslationRequest(ipAddress: String, sourceText: String, translatedText: String) {
        logger.info("Saving translation request from IP: $ipAddress")
        val translationRequest = TranslationRequest(
            id = 0,
            ipAddress = ipAddress,
            sourceText = sourceText,
            translatedText = translatedText,
            requestTime = LocalDateTime.now()
        )
        translationRequestRepository.save(translationRequest)
    }

}