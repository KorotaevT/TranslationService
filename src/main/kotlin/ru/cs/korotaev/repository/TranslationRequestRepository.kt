package ru.cs.korotaev.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.cs.korotaev.model.TranslationRequest
import java.sql.ResultSet

@Repository
class TranslationRequestRepository(
    private val jdbcTemplate: JdbcTemplate
) {

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

    fun findAll(): List<TranslationRequest> {
        val sql = "SELECT * FROM translation_requests"
        return jdbcTemplate.query(sql, rowMapper)
    }

    fun findById(id: Long): TranslationRequest? {
        val sql = "SELECT * FROM translation_requests WHERE id = ?"
        return jdbcTemplate.query(sql, arrayOf(id), rowMapper).firstOrNull()
    }

    fun findByIpAddress(ipAddress: String): List<TranslationRequest> {
        val sql = "SELECT * FROM translation_requests WHERE ip_address = ?"
        return jdbcTemplate.query(sql, arrayOf(ipAddress), rowMapper)
    }

    fun findByTextPart(textPart: String): List<TranslationRequest> {
        val sql = """
            SELECT * FROM translation_requests 
            WHERE source_text LIKE ? OR translated_text LIKE ?
        """.trimIndent()
        val likeTextPart = "%$textPart%"
        return jdbcTemplate.query(sql, arrayOf(likeTextPart, likeTextPart), rowMapper)
    }

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        TranslationRequest(
            id = rs.getLong("id"),
            ipAddress = rs.getString("ip_address"),
            sourceText = rs.getString("source_text"),
            translatedText = rs.getString("translated_text"),
            requestTime = rs.getTimestamp("request_time").toLocalDateTime()
        )
    }

}