package ru.urfumobile.rickandmorty.ui

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.model.Character
import ru.urfumobile.rickandmorty.model.CharacterFilters
import ru.urfumobile.rickandmorty.model.CharacterPage
import ru.urfumobile.rickandmorty.model.FilterCache

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load exposes characters and pagination`() = runTest {
        val repository = FakeRepository()
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(listOf("Rick Sanchez"), viewModel.uiState.value.characters.map { it.name })
        assertEquals(42, viewModel.uiState.value.totalPages)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `saved filters load at startup and changes reset page and badge`() = runTest {
        val repository = FakeRepository()
        val cache = FilterCache(CharacterFilters("morty", "alive"))
        val viewModel = createViewModel(repository, cache)
        advanceUntilIdle()
        assertEquals(CharacterFilters("morty", "alive"), repository.lastFilters)
        assertTrue(viewModel.uiState.value.showFilterBadge)

        viewModel.nextPage()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.page)

        cache.update(CharacterFilters())
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.page)
        assertEquals(CharacterFilters(), repository.lastFilters)
        assertFalse(viewModel.uiState.value.showFilterBadge)
    }

    @Test
    fun `favorites remain visible when network fails and can be removed`() = runTest {
        val repository = FakeRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.toggleFavorite(testCharacter)
        advanceUntilIdle()
        repository.failNetwork = true
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(listOf(testCharacter), viewModel.favorites.value)
        assertTrue(viewModel.uiState.value.errorMessage != null)
        viewModel.toggleFavorite(testCharacter)
        advanceUntilIdle()
        assertTrue(viewModel.favorites.value.isEmpty())
    }

    private fun createViewModel(
        repository: FakeRepository,
        cache: FilterCache = FilterCache(CharacterFilters()),
    ) = CharactersViewModel(GetCharactersUseCase(repository), repository, cache)
}

private class FakeRepository : CharacterRepository {
    var lastFilters = CharacterFilters()
    var failNetwork = false
    private val favorites = MutableStateFlow(emptyList<Character>())

    override suspend fun getCharacters(page: Int, filters: CharacterFilters): CharacterPage {
        if (failNetwork) throw IOException()
        lastFilters = filters
        return CharacterPage(
            characters = listOf(testCharacter),
            page = page,
            totalPages = 42,
            totalCount = 826,
        )
    }

    override suspend fun getCharacter(id: Int) = testCharacter
    override fun observeFavorites() = favorites
    override suspend fun setFavorite(character: Character, favorite: Boolean) {
        favorites.value = if (favorite) listOf(character) else emptyList()
    }
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
