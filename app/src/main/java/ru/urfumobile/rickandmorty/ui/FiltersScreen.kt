@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.urfumobile.rickandmorty.model.CharacterFilters

@Composable
fun FiltersScreen(
    initial: CharacterFilters,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var status by rememberSaveable { mutableStateOf(initial.status) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Фильтры") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
        )
    }) { padding ->
        Column(
            Modifier.padding(padding).imePadding().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя персонажа") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Статус")
            listOf("" to "Любой", "alive" to "Живой", "dead" to "Мёртв", "unknown" to "Неизвестно")
                .forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().selectable(
                            selected = status == value,
                            onClick = { status = value },
                            role = Role.RadioButton,
                        ).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = status == value, onClick = null)
                        Text(label, Modifier.padding(start = 12.dp))
                    }
                }
            TextButton(onClick = { name = ""; status = "" }) { Text("Сбросить") }
            Button(onClick = { onSave(name, status) }, modifier = Modifier.fillMaxWidth()) {
                Text("Готово")
            }
        }
    }
}
