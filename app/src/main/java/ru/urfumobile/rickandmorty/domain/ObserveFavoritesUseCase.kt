package ru.urfumobile.rickandmorty.domain

class ObserveFavoritesUseCase(private val repository: FavoriteRepository) {
    operator fun invoke() = repository.observeFavorites()
}
