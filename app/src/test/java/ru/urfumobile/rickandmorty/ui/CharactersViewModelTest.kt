package ru.urfumobile.rickandmorty.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterPage

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load exposes characters and pagination`() = runTest {
        val repository = FakeRepository()
        val viewModel = CharactersViewModel(GetCharactersUseCase(repository))

        advanceUntilIdle()

        assertEquals(listOf("Rick Sanchez"), viewModel.uiState.value.characters.map { it.name })
        assertEquals(42, viewModel.uiState.value.totalPages)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `search is debounced and sent as query parameter`() = runTest {
        val repository = FakeRepository()
        val viewModel = CharactersViewModel(GetCharactersUseCase(repository))
        advanceUntilIdle()

        viewModel.updateQuery("  morty  ")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals("morty", repository.lastName)
    }
}

private class FakeRepository : CharacterRepository {
    var lastName: String? = null

    override suspend fun getCharacters(page: Int, name: String?): CharacterPage {
        lastName = name
        return CharacterPage(
            characters = listOf(testCharacter),
            page = page,
            totalPages = 42,
            totalCount = 826,
        )
    }

    override suspend fun getCharacter(id: Int) = testCharacter
}

private val testCharacter = Character(
    id = 1,
    name = "Rick Sanchez",
    status = "Живой",
    species = "Человек",
    gender = "Мужской",
    origin = "Earth (C-137)",
    location = "Citadel of Ricks",
    episodeCount = 51,
    imageUrl = "https://example.com/rick.jpeg",
)
