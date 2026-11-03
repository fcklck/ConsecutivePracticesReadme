package ru.urfumobile.rickandmorty.ui

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.FakeFavoriteRepository
import ru.urfumobile.rickandmorty.domain.ObserveFavoritesUseCase
import ru.urfumobile.rickandmorty.domain.SetFavoriteUseCase
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
        val favorites = FakeFavoriteRepository()
        val viewModel = createViewModel(repository, favorites = favorites)
        val favoritesViewModel = FavoritesViewModel(ObserveFavoritesUseCase(favorites), SetFavoriteUseCase(favorites))
        advanceUntilIdle()
        viewModel.toggleFavorite(testCharacter)
        advanceUntilIdle()
        repository.failNetwork = true
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(listOf(testCharacter), viewModel.favorites.value)
        assertEquals(listOf(testCharacter), favoritesViewModel.favorites.value)
        assertTrue(viewModel.uiState.value.errorMessage != null)
        favoritesViewModel.remove(testCharacter)
        advanceUntilIdle()
        assertTrue(viewModel.favorites.value.isEmpty())
        assertTrue(favoritesViewModel.favorites.value.isEmpty())
    }

    @Test
    fun `failed request can be retried`() = runTest {
        val repository = FakeRepository().apply { failNetwork = true }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("Нет подключения"))

        repository.failNetwork = false
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf(testCharacter), viewModel.uiState.value.characters)
    }

    @Test
    fun `changing filters cancels a pending request without an error`() = runTest {
        val repository = FakeRepository()
        val cache = FilterCache(CharacterFilters())
        val viewModel = createViewModel(repository, cache)
        advanceUntilIdle()
        repository.responseDelay = 1_000
        viewModel.retry()
        runCurrent()

        cache.update(CharacterFilters("morty"))
        runCurrent()

        assertEquals(1, repository.cancelledRequests)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertEquals(CharacterFilters("morty"), repository.lastFilters)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun createViewModel(
        repository: FakeRepository,
        cache: FilterCache = FilterCache(CharacterFilters()),
        favorites: FakeFavoriteRepository = FakeFavoriteRepository(),
    ) = CharactersViewModel(
        GetCharactersUseCase(repository), ObserveFavoritesUseCase(favorites), SetFavoriteUseCase(favorites), cache,
    )
}

private class FakeRepository : CharacterRepository {
    var lastFilters = CharacterFilters()
    var failNetwork = false
    var responseDelay: Long = 0
    var cancelledRequests = 0

    override suspend fun getCharacters(page: Int, filters: CharacterFilters): CharacterPage {
        try {
            delay(responseDelay)
        } catch (error: CancellationException) {
            cancelledRequests++
            throw error
        }
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
