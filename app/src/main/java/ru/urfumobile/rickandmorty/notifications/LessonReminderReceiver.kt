package ru.urfumobile.rickandmorty.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ru.urfumobile.rickandmorty.MainActivity
import ru.urfumobile.rickandmorty.R

class LessonReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(OWNER_NAME) ?: return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = "$name, ваша любимая пара начинается!"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lesson_notification)
            .setContentTitle("Время любимой пары")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(1, notification)
    }

    companion object {
        const val CHANNEL_ID = "lesson_reminders"
        const val OWNER_NAME = "owner_name"

        fun notificationManager(context: Context): NotificationManager =
            context.getSystemService(NotificationManager::class.java).apply {
                createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Любимая пара", NotificationManager.IMPORTANCE_HIGH),
                )
            }
    }
}
