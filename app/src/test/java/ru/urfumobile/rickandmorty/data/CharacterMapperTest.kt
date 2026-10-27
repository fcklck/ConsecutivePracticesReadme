package ru.urfumobile.rickandmorty.data

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.urfumobile.rickandmorty.data.remote.CharacterDto
import ru.urfumobile.rickandmorty.data.remote.PlaceDto

class CharacterMapperTest {
    @Test
    fun `api model maps to domain model`() {
        val result = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = PlaceDto("Earth (C-137)"),
            location = PlaceDto("Citadel of Ricks"),
            image = "https://example.com/rick.jpeg",
            episode = listOf("episode/1", "episode/2"),
        ).toDomain()

        assertEquals("Живой", result.status)
        assertEquals("Человек", result.species)
        assertEquals("Мужской", result.gender)
        assertEquals(2, result.episodeCount)
        assertEquals("https://example.com/rick.jpeg", result.imageUrl)
    }
}
