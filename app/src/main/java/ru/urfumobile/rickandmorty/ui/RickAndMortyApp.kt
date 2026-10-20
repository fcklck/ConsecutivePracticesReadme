@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.chuckerteam.chucker.api.Chucker
import ru.urfumobile.rickandmorty.di.AppContainer
import ru.urfumobile.rickandmorty.model.Character

private enum class Tab(val title: String, val icon: ImageVector) {
    CHARACTERS("Персонажи", Icons.Default.Home),
    LOCATIONS("Локации", Icons.Default.LocationOn),
    EPISODES("Эпизоды", Icons.Default.Tv),
}

private const val catalogRoute = "catalog"
private const val detailsRoute = "character/{characterId}"

@Composable
fun RickAndMortyApp(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val listViewModel: CharactersViewModel = viewModel(
        factory = viewModelFactory {
            CharactersViewModel(container.getCharacters, container.repository, container.filterCache)
        },
    )
    val listState by listViewModel.uiState.collectAsStateWithLifecycle()
    val favorites by listViewModel.favorites.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(listViewModel) {
        listViewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    RickAndMortyTheme {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                NavHost(navController = navController, startDestination = catalogRoute) {
                    composable(catalogRoute) {
                        CatalogScreen(
                            state = listState,
                            favoriteIds = favorites.map { it.id }.toSet(),
                            onToggleFavorite = listViewModel::toggleFavorite,
                            onOpenFilters = { navController.navigate("filters") },
                            onOpenFavorites = { navController.navigate("favorites") },
                            onNextPage = listViewModel::nextPage,
                            onPreviousPage = listViewModel::previousPage,
                            onRetry = listViewModel::retry,
                            onCharacterClick = { navController.navigate("character/${it.id}") },
                            onOpenNetworkInspector = {
                                context.startActivity(Chucker.getLaunchIntent(context))
                            },
                        )
                    }
                    composable("filters") {
                        val filtersViewModel: FiltersViewModel = viewModel(
                            factory = viewModelFactory {
                                FiltersViewModel(container.filterPreferences, container.filterCache)
                            },
                        )
                        FiltersScreen(
                            initial = filtersViewModel.initialFilters,
                            onBack = { navController.popBackStack() },
                            onSave = { name, status ->
                                filtersViewModel.save(name, status)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable("favorites") {
                        FavoritesScreen(
                            characters = favorites,
                            onBack = { navController.popBackStack() },
                            onCharacterClick = { navController.navigate("character/${it.id}") },
                            onRemove = listViewModel::toggleFavorite,
                        )
                    }
                    composable(
                        route = detailsRoute,
                        arguments = listOf(navArgument("characterId") { type = NavType.IntType }),
                    ) { entry ->
                        val characterId = entry.arguments?.getInt("characterId") ?: return@composable
                        val detailsViewModel: CharacterDetailsViewModel = viewModel(
                            key = "character-$characterId",
                            factory = CharacterDetailsViewModel.factory(
                                characterId = characterId,
                                useCase = container.getCharacter,
                            ),
                        )
                        val detailsState by detailsViewModel.uiState.collectAsStateWithLifecycle()
                        CharacterDetailsScreen(
                            state = detailsState,
                            onBack = navController::popBackStack,
                            onRetry = detailsViewModel::retry,
                        )
                    }
                }
                SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
private fun CatalogScreen(
    state: CharactersUiState,
    favoriteIds: Set<Int>,
    onToggleFavorite: (Character) -> Unit,
    onOpenFilters: () -> Unit,
    onOpenFavorites: () -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onRetry: () -> Unit,
    onCharacterClick: (Character) -> Unit,
    onOpenNetworkInspector: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.CHARACTERS) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = AppColors.card) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { padding ->
        when (selectedTab) {
            Tab.CHARACTERS -> CharactersScreen(
                state = state,
                favoriteIds = favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onOpenFilters = onOpenFilters,
                onOpenFavorites = onOpenFavorites,
                onNextPage = onNextPage,
                onPreviousPage = onPreviousPage,
                onRetry = onRetry,
                onCharacterClick = onCharacterClick,
                onOpenNetworkInspector = onOpenNetworkInspector,
                modifier = Modifier.padding(padding),
            )
            Tab.LOCATIONS -> PlaceholderScreen(
                "Локации",
                "Здесь будут планеты и измерения",
                Tab.LOCATIONS,
                Modifier.padding(padding),
            )
            Tab.EPISODES -> PlaceholderScreen(
                "Эпизоды",
                "Здесь появится список эпизодов",
                Tab.EPISODES,
                Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    tab: Tab,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(tab.icon, contentDescription = null, modifier = Modifier.size(56.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(subtitle, color = AppColors.muted, modifier = Modifier.padding(top = 8.dp))
    }
}
