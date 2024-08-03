package ru.cs.korotaev.exception

open class TranslationException(message: String) : RuntimeException(message)
class LanguageNotFoundException : TranslationException("Не найден язык исходного сообщения")
class TranslationServiceException : TranslationException("Ошибка доступа к ресурсу перевода")