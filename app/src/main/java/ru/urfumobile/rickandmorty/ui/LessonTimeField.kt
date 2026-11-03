package ru.urfumobile.rickandmorty.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalTime
import java.util.Locale

@Composable
internal fun LessonTimeField(value: String, error: String?, enabled: Boolean, onChange: (String) -> Unit) {
    val context = LocalContext.current
    var showClock by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Время любимой пары") },
        placeholder = { Text("HH:mm") },
        supportingText = { Text(error ?: "Одно напоминание на сегодня") },
        isError = error != null,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        trailingIcon = {
            IconButton(onClick = { showClock = true }, enabled = enabled) {
                Icon(Icons.Default.Schedule, contentDescription = "Выбрать время на часах")
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    if (showClock) {
        DisposableEffect(Unit) {
            val time = if (error == null) LocalTime.parse(value) else LocalTime.now()
            val dialog = TimePickerDialog(context, { _, hour, minute ->
                onChange(String.format(Locale.ROOT, "%02d:%02d", hour, minute))
            }, time.hour, time.minute, true)
            dialog.setOnDismissListener { showClock = false }
            dialog.show()
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }
}
