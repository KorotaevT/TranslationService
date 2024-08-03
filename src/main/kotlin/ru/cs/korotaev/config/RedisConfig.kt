package ru.cs.korotaev.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    private val logger: Logger = LoggerFactory.getLogger(RedisConfig::class.java)

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory()
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
        return MessageListener { message, _ ->
            logger.info("Received message: ${String(message.body)}")
        }
    }

}