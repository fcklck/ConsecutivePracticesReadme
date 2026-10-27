package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import ru.urfumobile.rickandmorty.model.Character

data class CharactersUiState(
    val characters: List<Character> = emptyList(),
    val query: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class CharactersViewModel(
    private val getCharacters: GetCharactersUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CharactersUiState())
    val uiState: StateFlow<CharactersUiState> = mutableState.asStateFlow()
    private val errorHandler = requestErrorHandler { message ->
        mutableState.update {
            it.copy(characters = emptyList(), isLoading = false, errorMessage = message)
        }
    }

    private var requestJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadCharacters()
    }

    fun updateQuery(query: String) {
        mutableState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadCharacters(page = 1)
        }
    }

    fun nextPage() {
        val state = mutableState.value
        if (!state.isLoading && state.page < state.totalPages) {
            loadCharacters(state.page + 1)
        }
    }

    fun previousPage() {
        val state = mutableState.value
        if (!state.isLoading && state.page > 1) {
            loadCharacters(state.page - 1)
        }
    }

    fun retry() = loadCharacters(mutableState.value.page)

    private fun loadCharacters(page: Int = 1) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch(errorHandler) {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getCharacters(page, mutableState.value.query)
            mutableState.update {
                it.copy(
                    characters = result.characters,
                    page = result.page,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount,
                    isLoading = false,
                )
            }
        }
    }

    companion object {
        fun factory(useCase: GetCharactersUseCase) = viewModelFactory {
            CharactersViewModel(useCase)
        }
    }
}

internal inline fun <reified T : ViewModel> viewModelFactory(
    crossinline create: () -> T,
) = object : ViewModelProvider.Factory {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        if (modelClass.isAssignableFrom(T::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return create() as VM
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
