package ru.urfumobile.rickandmorty.data

import kotlinx.coroutines.flow.map
import ru.urfumobile.rickandmorty.data.local.FavoriteDao
import ru.urfumobile.rickandmorty.data.local.FavoriteEntity
import ru.urfumobile.rickandmorty.domain.FavoriteRepository
import ru.urfumobile.rickandmorty.model.Character

class FavoriteRepositoryImpl(private val favorites: FavoriteDao) : FavoriteRepository {
    override fun observeFavorites() = favorites.observeAll().map { rows ->
        rows.map { it.character }
    }

    override suspend fun getFavorite(id: Int) = favorites.getById(id)?.character

    override suspend fun setFavorite(character: Character, favorite: Boolean) {
        if (favorite) favorites.save(FavoriteEntity(character)) else favorites.delete(character.id)
    }
}
