package ru.urfumobile.rickandmorty

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.urfumobile.rickandmorty.domain.FavoriteRepository
import ru.urfumobile.rickandmorty.model.Character

internal class FakeFavoriteRepository(initial: List<Character> = emptyList()) : FavoriteRepository {
    private val items = MutableStateFlow(initial)
    var failure: Exception? = null

    override fun observeFavorites() = items
    override suspend fun getFavorite(id: Int) = items.value.find { it.id == id }
    override suspend fun setFavorite(character: Character, favorite: Boolean) {
        failure?.let { throw it }
        items.update { list -> list.filterNot { it.id == character.id } + if (favorite) listOf(character) else emptyList() }
    }
}

internal val savedCharacter = Character(
    id = 2, name = "Morty Smith", status = "Живой", species = "Человек", gender = "Мужской",
    origin = "Earth", location = "Earth", episodeCount = 1, imageUrl = "",
)
