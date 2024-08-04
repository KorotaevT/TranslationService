package ru.cs.korotaev.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.cs.korotaev.exception.NoRequestsFoundByTextPartException
import ru.cs.korotaev.exception.NoRequestsFoundException
import ru.cs.korotaev.exception.RecordNotFoundByIpException
import ru.cs.korotaev.exception.RecordNotFoundException
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
        val translatedText = translationService.getTranslationFromApi(sourceLang, targetLang, text)
        saveTranslationRequest(ipAddress, text, translatedText)
        return translatedText
    }

    fun detectLanguage(text: String): String {
        return translationService.detectLanguage(text)
    }

    fun getSupportedLanguages(): String {
        return translationService.getSupportedLanguages()
    }

    fun getAllRequests(): List<TranslationRequest> {
        logger.info("Fetching all translation requests")
        val requests = translationRequestRepository.findAll()
        if (requests.isEmpty()) {
            throw NoRequestsFoundException()
        }
        return requests
    }

    fun getRequestById(id: Long): TranslationRequest? {
        logger.info("Fetching translation request by ID: $id")
        return translationRequestRepository.findById(id) ?: throw RecordNotFoundException(id)
    }

    fun getRequestsByIp(ipAddress: String): List<TranslationRequest> {
        logger.info("Fetching translation requests by IP: $ipAddress")
        val requests = translationRequestRepository.findByIpAddress(ipAddress)
        if (requests.isEmpty()) {
            throw RecordNotFoundByIpException(ipAddress)
        }
        return requests
    }

    fun getRequestsByTextPart(textPart: String): List<TranslationRequest> {
        logger.info("Fetching translation requests by text part: $textPart")
        val requests = translationRequestRepository.findByTextPart(textPart)
        if (requests.isEmpty()) {
            throw NoRequestsFoundByTextPartException(textPart)
        }
        return requests
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