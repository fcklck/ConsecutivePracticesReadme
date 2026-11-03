package ru.urfumobile.rickandmorty.domain

import kotlinx.coroutines.flow.Flow
import ru.urfumobile.rickandmorty.model.Character

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<Character>>
    suspend fun getFavorite(id: Int): Character?
    suspend fun setFavorite(character: Character, favorite: Boolean)
}
