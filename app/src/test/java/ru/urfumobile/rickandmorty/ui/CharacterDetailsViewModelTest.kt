package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.SavedStateHandle
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.domain.GetCharacterUseCase
import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterPage

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `details loads the character from the navigation id`() = runTest {
        val repository = DetailsRepository()
        val viewModel = createViewModel(repository, 2)
        advanceUntilIdle()

        assertEquals(2, repository.lastId)
        assertEquals(2, viewModel.uiState.value.character?.id)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `unknown id shows an error instead of another character`() = runTest {
        val repository = DetailsRepository().apply { failure = IllegalArgumentException("Unknown id") }
        val viewModel = createViewModel(repository, 999)
        advanceUntilIdle()

        assertEquals(999, repository.lastId)
        assertNull(viewModel.uiState.value.character)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `missing navigation id does not send a request`() = runTest {
        val repository = DetailsRepository()
        val viewModel = createViewModel(repository, null)
        advanceUntilIdle()

        assertNull(repository.lastId)
        assertEquals("Персонаж не найден", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `network failure is recoverable by retry`() = runTest {
        val repository = DetailsRepository().apply { failure = IOException("offline") }
        val viewModel = createViewModel(repository, 2)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)

        repository.failure = null
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(2, viewModel.uiState.value.character?.id)
    }

    private fun createViewModel(repository: CharacterRepository, id: Int?) = CharacterDetailsViewModel(
        SavedStateHandle(mapOf("characterId" to id)), GetCharacterUseCase(repository),
    )
}

private class DetailsRepository : CharacterRepository {
    var lastId: Int? = null
    var failure: Exception? = null

    override suspend fun getCharacters(page: Int, name: String?): CharacterPage = error("Not used")

    override suspend fun getCharacter(id: Int): Character {
        lastId = id
        failure?.let { throw it }
        return Character(id, "Character $id", "Живой", "Человек", gender = "Мужской",
            origin = "Earth", location = "Earth", episodeCount = 1, imageUrl = "")
    }
}
