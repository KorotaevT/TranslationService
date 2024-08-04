package ru.cs.korotaev.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(LanguageNotFoundException::class)
    fun handleLanguageNotFoundException(ex: LanguageNotFoundException): ResponseEntity<String> {
        return ResponseEntity("Не найден язык исходного сообщения", ex.httpStatus)
    }

    @ExceptionHandler(TranslationServiceException::class)
    fun handleTranslationServiceException(ex: TranslationServiceException): ResponseEntity<String> {
        return ResponseEntity("Ошибка доступа к ресурсу перевода", ex.httpStatus)
    }

    @ExceptionHandler(RecordNotFoundException::class)
    fun handleRecordNotFoundException(ex: RecordNotFoundException): ResponseEntity<String> {
        return ResponseEntity("Запись с таким ID не найдена", ex.httpStatus)
    }

    @ExceptionHandler(RecordNotFoundByIpException::class)
    fun handleRecordNotFoundByIpException(ex: RecordNotFoundByIpException): ResponseEntity<String> {
        return ResponseEntity("Записи с IP-адресом не найдены", ex.httpStatus)
    }

    @ExceptionHandler(NoRequestsFoundException::class)
    fun handleNoRequestsFoundException(ex: NoRequestsFoundException): ResponseEntity<String> {
        return ResponseEntity("Список записей пуст", ex.httpStatus)
    }

    @ExceptionHandler(NoRequestsFoundByTextPartException::class)
    fun handleNoRequestsFoundByTextPartException(ex: NoRequestsFoundByTextPartException): ResponseEntity<String> {
        return ResponseEntity("Записи с таким содержанием не найдены", ex.httpStatus)
    }

}