package ru.cs.korotaev.model

import java.time.LocalDateTime

data class TranslationRequest(
    val id: Long = 0,
    val ipAddress: String,
    val sourceText: String,
    val translatedText: String,
    val requestTime: LocalDateTime = LocalDateTime.now()
)