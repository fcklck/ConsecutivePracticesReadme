@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.urfumobile.rickandmorty.model.Character

@Composable
fun CharactersScreen(
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
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Rick and Morty", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Данные из открытого API", fontSize = 12.sp, color = AppColors.muted)
                }
            },
            actions = {
                IconButton(onClick = onOpenFilters) {
                    BadgedBox(badge = {
                        if (state.showFilterBadge) Badge(
                            Modifier.semantics { contentDescription = "Фильтры включены" },
                        )
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                }
                IconButton(onClick = onOpenFavorites) {
                    Icon(Icons.Default.Star, contentDescription = "Избранное")
                }
                IconButton(onClick = onOpenNetworkInspector) {
                    Icon(Icons.Default.Cloud, contentDescription = "История сетевых запросов")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Персонажи", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Всего в API: ${state.totalCount} • Страница ${state.page} из ${state.totalPages}",
                color = AppColors.muted,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
            )
        }

        when {
            state.isLoading && state.characters.isEmpty() -> LoadingState()
            state.errorMessage != null -> ErrorState(state.errorMessage, onRetry)
            state.characters.isEmpty() -> EmptySearchState()
            else -> CharacterList(
                state = state,
                favoriteIds = favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onCharacterClick = onCharacterClick,
                onNextPage = onNextPage,
                onPreviousPage = onPreviousPage,
            )
        }
    }
}

@Composable
private fun CharacterList(
    state: CharactersUiState,
    favoriteIds: Set<Int>,
    onToggleFavorite: (Character) -> Unit,
    onCharacterClick: (Character) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    LaunchedEffect(state.page) { scrollState.scrollToItem(0) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.characters, key = Character::id) { character ->
                CharacterListItem(
                    character, character.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(character) },
                    onClick = { onCharacterClick(character) },
                )
            }
            item {
                Pagination(
                    page = state.page,
                    totalPages = state.totalPages,
                    enabled = !state.isLoading,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                )
            }
        }
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }
    }
}

@Composable
internal fun CharacterListItem(
    character: Character,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.card),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CharacterAvatar(character, 68.dp)
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(character.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${character.species} • ${character.gender}",
                    color = AppColors.muted,
                    modifier = Modifier.padding(top = 3.dp),
                )
                StatusLabel(character.status)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                )
            }
        }
    }
}

@Composable
private fun Pagination(
    page: Int,
    totalPages: Int,
    enabled: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onPreviousPage,
            enabled = enabled && page > 1,
            modifier = Modifier.weight(1f),
        ) {
            Text("Назад")
        }
        Button(
            onClick = onNextPage,
            enabled = enabled && page < totalPages,
            modifier = Modifier.weight(1f),
        ) {
            Text("Далее")
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text("Загружаем персонажей…", color = AppColors.muted, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(48.dp), tint = AppColors.muted)
        Text(
            "Ошибка загрузки",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            message,
            color = AppColors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 20.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text("Повторить", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.muted)
        Text("Ничего не найдено", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Text("Попробуйте изменить запрос", color = AppColors.muted, modifier = Modifier.padding(top = 4.dp))
    }
}
