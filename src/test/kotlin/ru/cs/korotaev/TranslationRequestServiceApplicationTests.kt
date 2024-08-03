package ru.cs.korotaev

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import ru.cs.korotaev.model.TranslationRequest
import ru.cs.korotaev.service.TranslationRequestService
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfiguration::class)
@SpringBootTest(properties = ["spring.profiles.active=test"])
class TranslationRequestServiceApplicationTests {

	@Autowired
	private lateinit var translationRequestService: TranslationRequestService

	@Autowired
	private lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	private lateinit var redisTemplate: RedisTemplate<String, Any>

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@BeforeAll
	fun setupAll(@Autowired flyway: Flyway) {
		flyway.migrate()
	}

	@BeforeEach
	fun setup() {
		jdbcTemplate.execute("DELETE FROM translation_requests")
		redisTemplate.execute { it.flushAll() }
	}

	@Test
	fun testTranslationAndCaching() {
		val sourceLang = "en"
		val targetLang = "ru"
		val text = "test"
		val ipAddress = "0.0.0.1"

		val translatedText = translationRequestService.translate(sourceLang, targetLang, text, ipAddress)

		assertEquals("тест", translatedText)

		val requests = translationRequestService.getRequestsByIp(ipAddress)
		assertEquals(1, requests.size)
		assertEquals(text, requests[0].sourceText)
		assertEquals(translatedText, requests[0].translatedText)

		val cachedText = redisTemplate.opsForValue().get("translations::${sourceLang}:${targetLang}:${text}")
		assertEquals(translatedText, cachedText)
	}


	@Test
	fun testDetectLanguage() {
		val text = "Hello"
		val jsonResponse = translationRequestService.detectLanguage(text)
		val jsonNode: JsonNode = objectMapper.readTree(jsonResponse)
		val detectedLanguageCode = jsonNode.get("languageCode").asText()
		assertEquals("en", detectedLanguageCode)
	}

	@Test
	fun testGetSupportedLanguages() {
		val supportedLanguages = translationRequestService.getSupportedLanguages()
		assert(supportedLanguages.contains("en"))
		assert(supportedLanguages.contains("ru"))
	}

	@Test
	fun testGetAllRequests() {
		val ipAddress = "0.0.0.1"
		val request1 = TranslationRequest(0, ipAddress, "text1", "translated1", LocalDateTime.now())
		val request2 = TranslationRequest(0, ipAddress, "text2", "translated2", LocalDateTime.now())
		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request1.ipAddress, request1.sourceText, request1.translatedText, request1.requestTime)
		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request2.ipAddress, request2.sourceText, request2.translatedText, request2.requestTime)

		val requests = translationRequestService.getAllRequests()
		assertEquals(2, requests.size)
	}

	@Test
	fun testGetRequestById() {
		val ipAddress = "0.0.0.1"
		val request = TranslationRequest(0, ipAddress, "text", "translated", LocalDateTime.now())
		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request.ipAddress, request.sourceText, request.translatedText, request.requestTime)

		val savedRequest = jdbcTemplate.queryForObject(
			"SELECT * FROM translation_requests WHERE ip_address = ? AND source_text = ?",
			arrayOf(request.ipAddress, request.sourceText)
		) { rs, _ ->
			TranslationRequest(
				rs.getLong("id"),
				rs.getString("ip_address"),
				rs.getString("source_text"),
				rs.getString("translated_text"),
				rs.getTimestamp("request_time").toLocalDateTime()
			)
		}

		val foundRequest = translationRequestService.getRequestById(savedRequest!!.id)
		assertEquals(savedRequest.id, foundRequest?.id)
		assertEquals(savedRequest.sourceText, foundRequest?.sourceText)
		assertEquals(savedRequest.translatedText, foundRequest?.translatedText)
	}

	@Test
	fun testGetRequestsByTextPart() {
		val ipAddress = "0.0.0.1"
		val request1 = TranslationRequest(0, ipAddress, "test1 part", "translated1", LocalDateTime.now())
		val request2 = TranslationRequest(0, ipAddress, "test2 part", "translated2", LocalDateTime.now())
		val request3 = TranslationRequest(0, ipAddress, "test3 different", "translated3", LocalDateTime.now())

		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request1.ipAddress, request1.sourceText, request1.translatedText, request1.requestTime)
		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request2.ipAddress, request2.sourceText, request2.translatedText, request2.requestTime)
		jdbcTemplate.update("INSERT INTO translation_requests (ip_address, source_text, translated_text, request_time) VALUES (?, ?, ?, ?)",
			request3.ipAddress, request3.sourceText, request3.translatedText, request3.requestTime)

		val textPart = "part"
		val requests = translationRequestService.getRequestsByTextPart(textPart)
		assertEquals(2, requests.size)
		assert(requests.any { it.sourceText == "test1 part" })
		assert(requests.any { it.sourceText == "test2 part" })
		assert(requests.none { it.sourceText == "test3 different" })
	}

}