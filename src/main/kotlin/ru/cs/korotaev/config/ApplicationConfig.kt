package ru.cs.korotaev.config

import com.fasterxml.jackson.databind.ObjectMapper
import javax.sql.DataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.web.client.RestTemplate

@Configuration
class ApplicationConfig {

    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.url = "jdbc:postgresql://localhost:5432/translationServiceDb"
        dataSource.username = "postgres"
        dataSource.password = "postgres"
        dataSource.setDriverClassName("org.postgresql.Driver")
        return dataSource
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
        return ObjectMapper()
    }

}