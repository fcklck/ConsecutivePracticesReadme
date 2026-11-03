package ru.urfumobile.rickandmorty.model

import java.io.Serializable
import java.net.URI

data class UserProfile(
    val fullName: String = "",
    val photoUri: String = "",
    val resumeUrl: String = "",
) : Serializable {
    fun validationError(): String? = when {
        fullName.isBlank() -> "Укажите ФИО"
        resumeUrl.isNotBlank() && !runCatching {
            val uri = URI(resumeUrl)
            uri.scheme == "https" && !uri.host.isNullOrBlank()
        }.getOrDefault(false) -> "Укажите HTTPS-ссылку на резюме в PDF"
        else -> null
    }
}
