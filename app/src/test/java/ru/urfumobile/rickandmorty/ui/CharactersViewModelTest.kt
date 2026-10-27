package ru.urfumobile.rickandmorty.ui

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `failed request shows an error and retry succeeds`() = runTest {
        val repository = FakeRepository().apply { failure = IOException("offline") }
        val viewModel = CharactersViewModel(GetCharactersUseCase(repository))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("Нет подключения"))

        repository.failure = null
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf(testCharacter), viewModel.uiState.value.characters)
    }

    @Test
    fun `replacing an active request cancels it without showing an error`() = runTest {
        val repository = FakeRepository()
        val viewModel = CharactersViewModel(GetCharactersUseCase(repository))
        advanceUntilIdle()
        repository.responseDelay = 1_000
        viewModel.retry()
        runCurrent()

        viewModel.updateQuery("morty")
        advanceTimeBy(500)
        runCurrent()

        assertEquals(1, repository.cancelledRequests)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertEquals("morty", repository.lastName)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}

private class FakeRepository : CharacterRepository {
    var lastName: String? = null
    var failure: Exception? = null
    var responseDelay: Long = 0
    var cancelledRequests = 0

    override suspend fun getCharacters(page: Int, name: String?): CharacterPage {
        lastName = name
        try {
            delay(responseDelay)
        } catch (error: CancellationException) {
            cancelledRequests++
            throw error
        }
        failure?.let { throw it }
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
