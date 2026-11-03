package ru.urfumobile.rickandmorty.model

import java.io.Serializable
import java.net.URI

data class UserProfile(
    val fullName: String = "",
    val photoUri: String = "",
    val resumeUrl: String = "",
    val lessonTime: String = "",
) : Serializable {
    fun lessonTimeError(): String? = when {
        lessonTime.isEmpty() -> "Укажите время любимой пары"
        !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(lessonTime) -> "Введите время в формате HH:mm, например 09:30"
        else -> null
    }

    fun validationError(): String? = when {
        fullName.isBlank() -> "Укажите ФИО"
        resumeUrl.isNotBlank() && !runCatching {
            val uri = URI(resumeUrl)
            uri.scheme == "https" && !uri.host.isNullOrBlank()
        }.getOrDefault(false) -> "Укажите HTTPS-ссылку на резюме в PDF"
        else -> lessonTimeError()
    }
}
