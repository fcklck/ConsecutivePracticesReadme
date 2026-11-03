package ru.urfumobile.rickandmorty.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import ru.urfumobile.rickandmorty.notifications.LessonReminderReceiver

@Composable
internal fun ReminderSaveButton(enabled: Boolean, onSave: () -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    var permissionDialog by rememberSaveable { mutableStateOf<String?>(null) }
    fun saveIfAllowed() {
        val notifications = context.getSystemService(NotificationManager::class.java)
        val alarms = context.getSystemService(AlarmManager::class.java)
        when {
            !notifications.areNotificationsEnabled() || notifications
                .getNotificationChannel(LessonReminderReceiver.CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE ->
                permissionDialog = "notifications"
            Build.VERSION.SDK_INT >= 31 && !alarms.canScheduleExactAlarms() ->
                permissionDialog = "alarms"
            else -> onSave()
        }
    }
    val notificationAccess = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) saveIfAllowed() else permissionDialog = "notifications"
    }
    Button(
        onClick = {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) notificationAccess.launch(Manifest.permission.POST_NOTIFICATIONS)
            else saveIfAllowed()
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Готово") }

    permissionDialog?.let { permission ->
        val exactAlarms = permission == "alarms"
        AlertDialog(
            onDismissRequest = { permissionDialog = null },
            title = { Text(if (exactAlarms) "Напоминание вовремя" else "Разрешите уведомления") },
            text = {
                Text(if (exactAlarms)
                    "Чтобы напомнить точно в выбранное время, разрешите будильники и напоминания. Затем вернитесь и нажмите «Готово»."
                else
                    "Разрешите уведомления о любимой паре в настройках приложения. Затем вернитесь и нажмите «Готово».")
            },
            confirmButton = {
                TextButton(onClick = {
                    permissionDialog = null
                    val intent = if (exactAlarms && Build.VERSION.SDK_INT >= 31) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                    } else {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (error: ActivityNotFoundException) {
                        onError("Откройте настройки приложения и разрешите уведомления и напоминания.")
                    }
                }) { Text("Открыть настройки") }
            },
            dismissButton = { TextButton(onClick = { permissionDialog = null }) { Text("Отмена") } },
        )
    }
}
