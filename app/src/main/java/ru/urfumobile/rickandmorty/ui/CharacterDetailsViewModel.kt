package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
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
    private val characterId: Int,
    private val getCharacter: GetCharacterUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CharacterDetailsUiState())
    val uiState: StateFlow<CharacterDetailsUiState> = mutableState.asStateFlow()

    init {
        loadCharacter()
    }

    fun retry() = loadCharacter()

    private fun loadCharacter() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                mutableState.value = CharacterDetailsUiState(
                    character = getCharacter(characterId),
                    isLoading = false,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableState.value = CharacterDetailsUiState(
                    isLoading = false,
                    errorMessage = error.toMessage(),
                )
            }
        }
    }

    companion object {
        fun factory(characterId: Int, useCase: GetCharacterUseCase) = viewModelFactory {
            CharacterDetailsViewModel(characterId, useCase)
        }
    }
}
