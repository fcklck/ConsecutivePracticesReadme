package ru.urfumobile.rickandmorty.ui

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.FakeFavoriteRepository
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.savedCharacter
import ru.urfumobile.rickandmorty.domain.ObserveFavoritesUseCase
import ru.urfumobile.rickandmorty.domain.SetFavoriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `favorites screen observes changes without a catalog ViewModel`() = runTest {
        val repository = FakeFavoriteRepository()
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository), SetFavoriteUseCase(repository))
        advanceUntilIdle()

        SetFavoriteUseCase(repository)(savedCharacter, true)
        advanceUntilIdle()

        assertEquals(listOf(savedCharacter), viewModel.favorites.value)
    }

    @Test
    fun `failed removal keeps the favorite and reports an error then allows retry`() = runTest {
        val repository = FakeFavoriteRepository(listOf(savedCharacter)).apply { failure = IOException("disk") }
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository), SetFavoriteUseCase(repository))
        advanceUntilIdle()
        val message = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { viewModel.messages.first() }

        viewModel.remove(savedCharacter)
        advanceUntilIdle()

        assertTrue(message.await().contains("Не удалось удалить"))
        assertEquals(listOf(savedCharacter), viewModel.favorites.value)

        repository.failure = null
        viewModel.remove(savedCharacter)
        advanceUntilIdle()
        assertTrue(viewModel.favorites.value.isEmpty())
    }
}
