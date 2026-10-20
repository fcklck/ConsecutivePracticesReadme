package ru.urfumobile.rickandmorty.domain

import kotlinx.coroutines.flow.Flow
import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterFilters
import ru.urfumobile.rickandmorty.model.CharacterPage

interface CharacterRepository {
    suspend fun getCharacters(page: Int, filters: CharacterFilters): CharacterPage
    suspend fun getCharacter(id: Int): Character
    fun observeFavorites(): Flow<List<Character>>
    suspend fun setFavorite(character: Character, favorite: Boolean)
}
