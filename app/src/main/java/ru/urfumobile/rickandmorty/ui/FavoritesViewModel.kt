package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.urfumobile.rickandmorty.domain.ObserveFavoritesUseCase
import ru.urfumobile.rickandmorty.domain.SetFavoriteUseCase
import ru.urfumobile.rickandmorty.model.Character

class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : ViewModel() {
    val favorites = observeFavorites().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val mutableMessages = MutableSharedFlow<String>()
    val messages = mutableMessages.asSharedFlow()

    fun remove(character: Character) {
        viewModelScope.launch {
            try {
                setFavorite(character, false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableMessages.emit("Не удалось удалить из избранного. Попробуйте ещё раз.")
            }
        }
    }
}
