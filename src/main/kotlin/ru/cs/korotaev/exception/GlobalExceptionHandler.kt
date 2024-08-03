package ru.cs.korotaev.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(LanguageNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleLanguageNotFoundException(ex: LanguageNotFoundException): ResponseEntity<String> {
        return ResponseEntity("Не найден язык исходного сообщения", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TranslationServiceException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTranslationServiceException(ex: TranslationServiceException): ResponseEntity<String> {
        return ResponseEntity("Ошибка доступа к ресурсу перевода", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneralException(ex: Exception): ResponseEntity<String> {
        return ResponseEntity("Внутренняя ошибка сервера: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
    }

}