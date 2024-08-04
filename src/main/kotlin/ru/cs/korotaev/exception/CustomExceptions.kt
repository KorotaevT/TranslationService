package ru.cs.korotaev.exception

import org.springframework.http.HttpStatus

open class TranslationException(
    message: String,
    val httpStatus: HttpStatus
) : RuntimeException(message)

class LanguageNotFoundException : TranslationException(
    "Source language not found",
    HttpStatus.BAD_REQUEST
)

class TranslationServiceException : TranslationException(
    "Error accessing translation resource",
    HttpStatus.FORBIDDEN
)

class RecordNotFoundException(id: Long) : TranslationException(
    "Record with ID $id not found",
    HttpStatus.NOT_FOUND
)

class RecordNotFoundByIpException(ipAddress: String) : TranslationException(
    "No records found for IP address $ipAddress",
    HttpStatus.NOT_FOUND
)

class NoRequestsFoundException : TranslationException(
    "No translation requests found",
    HttpStatus.NOT_FOUND
)

class NoRequestsFoundByTextPartException(textPart: String) : TranslationException(
    "No translation requests found for text part: $textPart",
    HttpStatus.NOT_FOUND
)