package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfumobile.rickandmorty.domain.ProfileRepository
import ru.urfumobile.rickandmorty.model.UserProfile

data class EditProfileState(
    val draft: UserProfile,
    val busy: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val cameraUri: String? = null,
)

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        EditProfileState(savedState["draft"] ?: repository.profile.value),
    )
    val state = mutableState.asStateFlow()

    fun edit(profile: UserProfile) {
        savedState["draft"] = profile
        mutableState.update { it.copy(draft = profile, error = null) }
    }

    fun importPhoto(uri: String) = runOperation("Не удалось открыть фотографию.") {
        edit(state.value.draft.copy(photoUri = repository.importPhoto(uri)))
    }

    fun prepareCamera() = runOperation("Не удалось подготовить фотографию.") {
        val uri = repository.createCameraPhoto()
        mutableState.update { it.copy(cameraUri = uri) }
    }

    fun cameraOpened() { mutableState.update { it.copy(cameraUri = null) } }
    fun showError(message: String) { mutableState.update { it.copy(error = message) } }

    fun save() {
        val profile = state.value.draft.let {
            it.copy(fullName = it.fullName.trim(), resumeUrl = it.resumeUrl.trim())
        }
        profile.validationError()?.let { showError(it); return }
        runOperation("Не удалось сохранить профиль.") {
            repository.save(profile)
            mutableState.update { it.copy(saved = true) }
        }
    }

    private fun runOperation(message: String, operation: suspend () -> Unit) {
        if (state.value.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, error = null) }
            try {
                operation()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showError(message)
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
