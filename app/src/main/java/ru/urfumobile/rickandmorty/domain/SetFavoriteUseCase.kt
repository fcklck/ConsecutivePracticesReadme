package ru.urfumobile.rickandmorty.domain

import ru.urfumobile.rickandmorty.model.Character

class SetFavoriteUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(character: Character, favorite: Boolean) =
        repository.setFavorite(character, favorite)
}
