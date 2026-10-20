package ru.urfumobile.rickandmorty.domain

import ru.urfumobile.rickandmorty.model.CharacterFilters

class GetCharactersUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(page: Int, filters: CharacterFilters) =
        repository.getCharacters(page, filters)
}
