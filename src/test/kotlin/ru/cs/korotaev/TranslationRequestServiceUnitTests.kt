package ru.cs.korotaev

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.repository.TranslationRequestRepository
import ru.cs.korotaev.service.TranslationRequestService
import ru.cs.korotaev.service.TranslationService
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TranslationRequestServiceUnitTests {

    @Mock
    private lateinit var translationRequestRepository: TranslationRequestRepository

    @Mock
    private lateinit var translationService: TranslationService

    @InjectMocks
    private lateinit var translationRequestService: TranslationRequestService

    @BeforeEach
    fun setup() {}

    @Test
    fun testTranslate() {
        val sourceLang = "en"
        val targetLang = "ru"
        val text = "test"
        val ipAddress = "0.0.0.1"
        val translatedText = "тест"
        `when`(translationService.getTranslationFromApi(sourceLang, targetLang, text)).thenReturn(translatedText)
        val result = translationRequestService.translate(sourceLang, targetLang, text, ipAddress)
        verify(translationService).getTranslationFromApi(sourceLang, targetLang, text)
        assertEquals(translatedText, result)
    }

    @Test
    fun testDetectLanguage() {
        val text = "Hello"
        val detectedLanguageCode = "en"
        `when`(translationService.detectLanguage(text)).thenReturn(detectedLanguageCode)
        val result = translationRequestService.detectLanguage(text)
        verify(translationService).detectLanguage(text)
        assertEquals(detectedLanguageCode, result)
    }

    @Test
    fun testGetSupportedLanguages() {
        val supportedLanguages = "en,ru"
        `when`(translationService.getSupportedLanguages()).thenReturn(supportedLanguages)
        val result = translationRequestService.getSupportedLanguages()
        assertEquals(supportedLanguages, result)
    }

    @Test
    fun testGetAllRequests() {
        val ipAddress = "0.0.0.1"
        val requests = listOf(
            TranslationRequest(1, ipAddress, "text1", "translated1", LocalDateTime.now()),
            TranslationRequest(2, ipAddress, "text2", "translated2", LocalDateTime.now())
        )
        `when`(translationRequestRepository.findAll()).thenReturn(requests)
        val result = translationRequestService.getAllRequests()
        assertEquals(2, result.size)
    }

    @Test
    fun testGetRequestById() {
        val ipAddress = "0.0.0.1"
        val request = TranslationRequest(1, ipAddress, "text", "translated", LocalDateTime.now())
        `when`(translationRequestRepository.findById(1)).thenReturn(request)
        val result = translationRequestService.getRequestById(1)
        assertEquals(request.id, result?.id)
        assertEquals(request.sourceText, result?.sourceText)
        assertEquals(request.translatedText, result?.translatedText)
    }

    @Test
    fun testGetRequestsByIp() {
        val ipAddress = "0.0.0.1"
        val requests = listOf(
            TranslationRequest(1, ipAddress, "text1", "translated1", LocalDateTime.now()),
            TranslationRequest(2, ipAddress, "text2", "translated2", LocalDateTime.now())
        )
        `when`(translationRequestRepository.findByIpAddress(ipAddress)).thenReturn(requests)
        val result = translationRequestService.getRequestsByIp(ipAddress)
        assertEquals(2, result.size)
        assert(result.any { it.sourceText == "text1" })
        assert(result.any { it.sourceText == "text2" })
    }

    @Test
    fun testGetRequestsByTextPart() {
        val ipAddress = "0.0.0.1"
        val textPart = "part"
        val requests = listOf(
            TranslationRequest(1, ipAddress, "test1 part", "translated1", LocalDateTime.now()),
            TranslationRequest(2, ipAddress, "test2 part", "translated2", LocalDateTime.now())
        )
        `when`(translationRequestRepository.findByTextPart(textPart)).thenReturn(requests)
        val result = translationRequestService.getRequestsByTextPart(textPart)
        assertEquals(2, result.size)
        assert(result.any { it.sourceText == "test1 part" })
        assert(result.any { it.sourceText == "test2 part" })
    }

}