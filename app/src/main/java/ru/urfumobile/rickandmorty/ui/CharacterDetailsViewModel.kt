package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfumobile.rickandmorty.domain.GetCharacterUseCase
import ru.urfumobile.rickandmorty.model.Character

data class CharacterDetailsUiState(
    val character: Character? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class CharacterDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCharacter: GetCharacterUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CharacterDetailsUiState())
    val uiState: StateFlow<CharacterDetailsUiState> = mutableState.asStateFlow()
    private val characterId: Int? = savedStateHandle["characterId"]
    private val errorHandler = requestErrorHandler { message ->
        mutableState.value = CharacterDetailsUiState(isLoading = false, errorMessage = message)
    }

    init {
        loadCharacter()
    }

    fun retry() = loadCharacter()

    private fun loadCharacter() {
        val id = characterId
        if (id == null) {
            mutableState.value = CharacterDetailsUiState(isLoading = false, errorMessage = "Персонаж не найден")
            return
        }
        viewModelScope.launch(errorHandler) {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            mutableState.value = CharacterDetailsUiState(character = getCharacter(id), isLoading = false)
        }
    }

    companion object {
        fun factory(useCase: GetCharacterUseCase) = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { CharacterDetailsViewModel(createSavedStateHandle(), useCase) }
        }
    }
}
