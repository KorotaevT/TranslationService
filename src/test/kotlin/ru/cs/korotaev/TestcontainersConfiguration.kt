package ru.cs.korotaev

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import javax.sql.DataSource

@TestConfiguration
@PropertySource("classpath:application-test.properties")
class TestcontainersConfiguration(
	@Value("\${spring.datasource.username}") private val postgresUsername: String,
	@Value("\${spring.datasource.password}") private val postgresPassword: String,
	@Value("\${spring.datasource.driver-class-name}") private val postgresDriverClassName: String,
	@Value("\${spring.data.redis.host}") private val redisHost: String,
	@Value("\${spring.data.redis.port}") private val redisPort: Int
) {

	@Bean
	fun postgresContainer(): PostgreSQLContainer<*> {
		return PostgreSQLContainer(DockerImageName.parse("postgres:latest")).apply {
			withDatabaseName("testdb")
			withUsername(postgresUsername)
			withPassword(postgresPassword)
			start()
		}
	}

	@Bean
	fun redisContainer(): GenericContainer<*> {
		return GenericContainer(DockerImageName.parse("redis:latest")).apply {
			withExposedPorts(redisPort)
			start()
		}
	}

	@Bean
	fun redisConnectionFactory(redisContainer: GenericContainer<*>): RedisConnectionFactory {
		val redisPort = redisContainer.getMappedPort(redisPort)
		return LettuceConnectionFactory(redisHost, redisPort)
	}

	@Bean
	fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
		val template = RedisTemplate<String, Any>()
		template.connectionFactory = redisConnectionFactory
		template.keySerializer = StringRedisSerializer()
		template.valueSerializer = GenericJackson2JsonRedisSerializer()
		return template
	}

	@Bean
	fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
		val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
			.entryTtl(Duration.ofSeconds(60))

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(redisCacheConfiguration)
			.build()
	}

	@Bean
	fun redisMessageListenerContainer(
		redisConnectionFactory: RedisConnectionFactory,
		messageListener: MessageListener
	): RedisMessageListenerContainer {
		val container = RedisMessageListenerContainer()
		container.connectionFactory = redisConnectionFactory
		container.addMessageListener(messageListener, ChannelTopic("redis"))
		return container
	}

	@Bean
	fun messageListener(): MessageListener {
		return MessageListener { _, _ -> }
	}

	@Bean
	@Primary
	fun dataSource(postgresContainer: PostgreSQLContainer<*>): DataSource {
		return DriverManagerDataSource().apply {
			setDriverClassName(postgresDriverClassName)
			url = "jdbc:postgresql://${postgresContainer.host}:${postgresContainer.firstMappedPort}/testdb"
			username = postgresUsername
			password = postgresPassword
		}
	}

	@Bean
	fun restTemplate(): RestTemplate {
		return RestTemplate()
	}

	@Bean
	fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
		return JdbcTemplate(dataSource)
	}

	@Bean
	fun objectMapper(): ObjectMapper {
		return Jackson2ObjectMapperBuilder.json()
			.modules(JavaTimeModule())
			.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build()
	}

	@Bean
	fun flyway(dataSource: DataSource): Flyway {
		return Flyway.configure()
			.dataSource(dataSource)
			.cleanDisabled(false)
			.load()
	}

}