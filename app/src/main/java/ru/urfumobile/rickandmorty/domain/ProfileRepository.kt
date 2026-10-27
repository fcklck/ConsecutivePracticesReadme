package ru.urfumobile.rickandmorty.domain

import kotlinx.coroutines.flow.StateFlow
import ru.urfumobile.rickandmorty.model.UserProfile

interface ProfileRepository {
    val profile: StateFlow<UserProfile>
    suspend fun save(profile: UserProfile)
    suspend fun importPhoto(uri: String): String
    suspend fun createCameraPhoto(): String
    suspend fun downloadResume(url: String): String
}
