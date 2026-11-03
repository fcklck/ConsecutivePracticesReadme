@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfumobile.rickandmorty.di.AppContainer
import ru.urfumobile.rickandmorty.model.Character

@Composable
fun FavoritesScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onCharacterClick: (Character) -> Unit,
) {
    val viewModel: FavoritesViewModel = viewModel(factory = viewModelFactory {
        FavoritesViewModel(container.observeFavorites, container.setFavorite)
    })
    val characters by viewModel.favorites.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        TopAppBar(
            title = { Text("Избранное") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
        )
    }) { padding ->
        if (characters.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("В избранном пока пусто")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(characters, key = Character::id) { character ->
                    CharacterListItem(
                        character, isFavorite = true,
                        onToggleFavorite = { viewModel.remove(character) },
                        onClick = { onCharacterClick(character) },
                    )
                }
            }
        }
    }
}
