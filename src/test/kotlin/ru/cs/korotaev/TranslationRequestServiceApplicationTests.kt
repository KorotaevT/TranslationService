package ru.cs.korotaev

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class TranslationRequestServiceApplicationTests {

	@Test
	fun contextLoads() {
	}

}
