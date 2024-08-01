package ru.cs.korotaev.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.cs.korotaev.service.TranslationService

@RestController
@RequestMapping("/api/v1/translate")
@Tag(name = "TranslationController", description = "API для перевода текста")
class TranslationController(private val translationService: TranslationService) {

    @PostMapping
    @Operation(summary = "Перевод текста", description = "Переводит текст с одного языка на другой")
    fun translate(
        @RequestParam sourceLang: String,
        @RequestParam targetLang: String,
        @RequestBody text: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val ipAddress = request.remoteAddr
        return translationService.translate(sourceLang, targetLang, text, ipAddress)
    }

    @PostMapping("/detect")
    @Operation(summary = "Определение языка", description = "Определяет язык текста")
    fun detectLanguage(
        @RequestBody text: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val ipAddress = request.remoteAddr
        return translationService.detectLanguage(text, ipAddress)
    }

    @GetMapping("/languages")
    @Operation(summary = "Получение языков", description = "Возвращает список доступных языков")
    fun getSupportedLanguages(): ResponseEntity<String> {
        return translationService.getSupportedLanguages()
    }

}