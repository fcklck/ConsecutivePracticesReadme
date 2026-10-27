package ru.urfumobile.rickandmorty.domain

class GetCharactersUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(page: Int, name: String?) =
        repository.getCharacters(page, name?.trim()?.takeIf(String::isNotEmpty))
}
