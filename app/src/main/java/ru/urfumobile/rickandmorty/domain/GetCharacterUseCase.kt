package ru.urfumobile.rickandmorty.domain

class GetCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(id: Int) = repository.getCharacter(id)
}
