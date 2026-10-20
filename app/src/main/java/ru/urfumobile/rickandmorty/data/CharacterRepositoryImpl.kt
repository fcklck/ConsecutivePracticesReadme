package ru.urfumobile.rickandmorty.data

import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import ru.urfumobile.rickandmorty.data.local.FavoriteDao
import ru.urfumobile.rickandmorty.data.local.FavoriteEntity
import ru.urfumobile.rickandmorty.data.remote.CharacterApi
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterFilters
import ru.urfumobile.rickandmorty.model.CharacterPage

class CharacterRepositoryImpl(
    private val api: CharacterApi,
    private val favorites: FavoriteDao,
) : CharacterRepository {
    override suspend fun getCharacters(page: Int, filters: CharacterFilters): CharacterPage = try {
        val response = api.getCharacters(
            page,
            filters.name.trim().takeIf { it.isNotEmpty() },
            filters.status.takeIf { it.isNotEmpty() },
        )
        CharacterPage(
            characters = response.results.map { it.toDomain() },
            page = page,
            totalPages = response.info.pages,
            totalCount = response.info.count,
        )
    } catch (error: HttpException) {
        if (error.code() == 404 && filters.isActive) {
            CharacterPage(emptyList(), page = 1, totalPages = 1, totalCount = 0)
        } else {
            throw error
        }
    }

    override suspend fun getCharacter(id: Int) =
        favorites.getById(id)?.character ?: api.getCharacter(id).toDomain()

    override fun observeFavorites() = favorites.observeAll().map { rows ->
        rows.map { it.character }
    }

    override suspend fun setFavorite(character: Character, favorite: Boolean) {
        if (favorite) favorites.save(FavoriteEntity(character)) else favorites.delete(character.id)
    }
}
