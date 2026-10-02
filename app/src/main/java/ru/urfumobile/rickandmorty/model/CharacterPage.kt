package ru.urfumobile.rickandmorty.model

data class CharacterPage(
    val characters: List<Character>,
    val page: Int,
    val totalPages: Int,
    val totalCount: Int,
)
