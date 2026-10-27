package ru.urfumobile.rickandmorty.domain

class GetCharacterUseCase(
    private val repository: CharacterRepository,
    private val favorites: FavoriteRepository,
) {
    suspend operator fun invoke(id: Int) = favorites.getFavorite(id) ?: repository.getCharacter(id)
}
