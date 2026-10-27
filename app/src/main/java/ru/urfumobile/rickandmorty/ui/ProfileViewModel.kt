package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.urfumobile.rickandmorty.domain.ProfileRepository

data class ResumeState(
    val downloading: Boolean = false,
    val fileUri: String? = null,
    val error: String? = null,
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    val profile = repository.profile
    private val mutableState = MutableStateFlow(ResumeState())
    val resume = mutableState.asStateFlow()

    fun downloadResume() {
        if (mutableState.value.downloading || profile.value.resumeUrl.isBlank()) return
        viewModelScope.launch {
            mutableState.value = ResumeState(downloading = true)
            try {
                mutableState.value = ResumeState(fileUri = repository.downloadResume(profile.value.resumeUrl))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = ResumeState(error = "Не удалось скачать PDF. Проверьте ссылку и интернет.")
            }
        }
    }

    fun clearResult() { mutableState.value = ResumeState() }
    fun viewerUnavailable() { mutableState.value = ResumeState(error = "Нет приложения для просмотра PDF.") }
}
