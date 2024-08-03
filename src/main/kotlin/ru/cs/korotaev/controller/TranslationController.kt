package ru.cs.korotaev.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.service.TranslationRequestService

@RestController
@RequestMapping("/api/v1/translate")
@Tag(name = "TranslationController", description = "API для перевода текста")
class TranslationController(
    private val translationRequestService: TranslationRequestService
) {

    @PostMapping
    @Operation(summary = "Перевод текста", description = "Переводит текст с одного языка на другой")
    fun translate(
        @RequestParam sourceLang: String,
        @RequestParam targetLang: String,
        @RequestBody text: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val ipAddress = request.remoteAddr
        val translatedText = translationRequestService.translate(sourceLang, targetLang, text, ipAddress)
        return ResponseEntity.ok(translatedText)
    }

    @PostMapping("/detect")
    @Operation(summary = "Определение языка", description = "Определяет язык текста")
    fun detectLanguage(
        @RequestBody text: String
    ): ResponseEntity<String> {
        val detectedLanguage = translationRequestService.detectLanguage(text)
        return ResponseEntity.ok(detectedLanguage)
    }

    @GetMapping("/languages")
    @Operation(summary = "Получение языков", description = "Возвращает список доступных языков")
    fun getSupportedLanguages(): ResponseEntity<String> {
        val supportedLanguages = translationRequestService.getSupportedLanguages()
        return ResponseEntity.ok(supportedLanguages)
    }

    @GetMapping("/requests")
    @Operation(summary = "Получение всех запросов", description = "Возвращает все запросы на перевод")
    fun getAllRequests(): ResponseEntity<List<TranslationRequest>> {
        val allRequests = translationRequestService.getAllRequests()
        return ResponseEntity.ok(allRequests)
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Получение запроса по ID", description = "Возвращает запрос на перевод по ID")
    fun getRequestById(@PathVariable id: Long): ResponseEntity<TranslationRequest?> {
        val request = translationRequestService.getRequestById(id)
        return ResponseEntity.ok(request)
    }

    @GetMapping("/requests/ip/{ipAddress}")
    @Operation(summary = "Получение запросов по IP", description = "Возвращает все запросы на перевод по IP-адресу")
    fun getRequestsByIp(@PathVariable ipAddress: String): ResponseEntity<List<TranslationRequest>> {
        val requests = translationRequestService.getRequestsByIp(ipAddress)
        return ResponseEntity.ok(requests)
    }

    @GetMapping("/requests/text")
    @Operation(summary = "Получение запросов по тексту", description = "Возвращает все запросы на перевод по части текста")
    fun getRequestsByTextPart(@RequestParam textPart: String): ResponseEntity<List<TranslationRequest>> {
        val requests = translationRequestService.getRequestsByTextPart(textPart)
        return ResponseEntity.ok(requests)
    }

}