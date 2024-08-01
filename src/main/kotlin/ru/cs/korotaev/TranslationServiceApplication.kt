package ru.cs.korotaev

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TranslationServiceApplication

fun main(args: Array<String>) {

	runApplication<TranslationServiceApplication>(*args)

}
