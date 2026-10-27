package ru.urfumobile.rickandmorty.data

import ru.urfumobile.rickandmorty.data.remote.CharacterDto
import ru.urfumobile.rickandmorty.model.Character

fun CharacterDto.toDomain() = Character(
    id = id,
    name = name,
    status = status.translateStatus(),
    species = species.translateSpecies(),
    type = type,
    gender = gender.translateGender(),
    origin = origin.name,
    location = location.name,
    episodeCount = episode.size,
    imageUrl = image,
)

private fun String.translateStatus() = when (this) {
    "Alive" -> "Живой"
    "Dead" -> "Мёртв"
    else -> "Неизвестно"
}

private fun String.translateGender() = when (this) {
    "Male" -> "Мужской"
    "Female" -> "Женский"
    "Genderless" -> "Бесполый"
    else -> "Неизвестно"
}

private fun String.translateSpecies() = when (this) {
    "Human" -> "Человек"
    "Humanoid" -> "Гуманоид"
    "Alien" -> "Пришелец"
    else -> this
}
