package ru.urfumobile.rickandmorty.domain

import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterPage

interface CharacterRepository {
    suspend fun getCharacters(page: Int, name: String?): CharacterPage
    suspend fun getCharacter(id: Int): Character
}
