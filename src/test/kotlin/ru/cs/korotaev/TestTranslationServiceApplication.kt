package ru.cs.korotaev

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<TranslationServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
