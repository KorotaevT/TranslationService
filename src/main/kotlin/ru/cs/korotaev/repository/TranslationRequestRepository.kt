package ru.cs.korotaev.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.cs.korotaev.model.TranslationRequest

@Repository
class TranslationRequestRepository(private val jdbcTemplate: JdbcTemplate) {

    fun save(translationRequest: TranslationRequest): Int {
        val sql = """
            INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) 
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            translationRequest.ipAddress,
            translationRequest.sourceText,
            translationRequest.translatedText,
            translationRequest.requestTime
        )
    }

}
