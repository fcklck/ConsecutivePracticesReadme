package ru.urfumobile.rickandmorty.data.remote

data class CharacterPageDto(
    val info: PageInfoDto,
    val results: List<CharacterDto>,
)

data class PageInfoDto(
    val count: Int,
    val pages: Int,
)

data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: PlaceDto,
    val location: PlaceDto,
    val image: String,
    val episode: List<String>,
)

data class PlaceDto(val name: String)
