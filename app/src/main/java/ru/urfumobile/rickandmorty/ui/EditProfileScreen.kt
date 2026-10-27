@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ru.urfumobile.rickandmorty.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditProfileScreen(viewModel: EditProfileViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var photoDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var accessGranted by remember { mutableStateOf(photoPermissions().any { context.hasPermission(it) }) }
    val photoAccess = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        accessGranted = result.values.any { it }
        if (!accessGranted) onBack()
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importPhoto(it.toString()) }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let(viewModel::importPhoto)
        pendingCameraUri = null
    }
    val cameraAccess = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.prepareCamera()
        else viewModel.showError("Для съёмки разрешите доступ к камере.")
    }
    LaunchedEffect(Unit) {
        if (!accessGranted) photoAccess.launch(photoPermissions())
    }
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    LaunchedEffect(state.cameraUri) {
        state.cameraUri?.let { uri ->
            pendingCameraUri = uri
            viewModel.cameraOpened()
            try {
                camera.launch(Uri.parse(uri))
            } catch (error: ActivityNotFoundException) {
                pendingCameraUri = null
                viewModel.showError("На устройстве нет приложения камеры.")
            }
        }
    }
    BackHandler(enabled = state.busy) { }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Редактирование профиля") },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !state.busy) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
        )
    }) { padding ->
        if (accessGranted) {
            Column(
                Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ProfilePhoto(
                    state.draft.photoUri,
                    Modifier.clickable(enabled = !state.busy) { photoDialog = true }
                        .semantics { contentDescription = "Изменить фото" },
                )
                Text("Нажмите на фото, чтобы заменить его", color = AppColors.muted)
                OutlinedTextField(
                    value = state.draft.fullName,
                    onValueChange = { viewModel.edit(state.draft.copy(fullName = it)) },
                    label = { Text("ФИО") },
                    singleLine = true,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.draft.resumeUrl,
                    onValueChange = { viewModel.edit(state.draft.copy(resumeUrl = it)) },
                    label = { Text("Ссылка на резюме (PDF)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.busy) CircularProgressIndicator()
                Button(onClick = viewModel::save, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Готово")
                }
            }
        }
    }
    if (photoDialog) {
        AlertDialog(
            onDismissRequest = { photoDialog = false },
            title = { Text("Новое фото") },
            text = {
                Column {
                    TextButton(onClick = { photoDialog = false; gallery.launch("image/*") }) { Text("Галерея") }
                    TextButton(onClick = {
                        photoDialog = false
                        if (context.hasPermission(Manifest.permission.CAMERA)) viewModel.prepareCamera()
                        else cameraAccess.launch(Manifest.permission.CAMERA)
                    }) { Text("Камера") }
                }
            },
            confirmButton = { TextButton(onClick = { photoDialog = false }) { Text("Отмена") } },
        )
    }
}

private fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun photoPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
    )
    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}
