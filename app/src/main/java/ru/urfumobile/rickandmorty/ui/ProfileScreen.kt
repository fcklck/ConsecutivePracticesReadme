@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.urfumobile.rickandmorty.R
import ru.urfumobile.rickandmorty.model.UserProfile

@Composable
fun ProfileScreen(
    profile: UserProfile,
    resume: ResumeState,
    onEdit: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProfilePhoto(profile.photoUri)
            Text(
                profile.fullName.ifBlank { "Ваше имя" },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            if (profile.fullName.isBlank()) {
                Text("Нажмите на карандаш, чтобы заполнить профиль.", color = AppColors.muted)
            }
            if (profile.resumeUrl.isNotBlank()) {
                Text(profile.resumeUrl, color = AppColors.muted, textAlign = TextAlign.Center)
            }
            Button(onClick = onResume, enabled = profile.resumeUrl.isNotBlank() && !resume.downloading) {
                Text(if (resume.downloading) "Скачивание…" else "Резюме")
            }
            if (resume.downloading) CircularProgressIndicator()
            resume.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
internal fun ProfilePhoto(uri: String, modifier: Modifier = Modifier) {
    Box(
        modifier.size(128.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        if (uri.isBlank()) {
            Icon(Icons.Default.Person, contentDescription = "Фото профиля", modifier = Modifier.size(64.dp))
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Фото профиля",
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
