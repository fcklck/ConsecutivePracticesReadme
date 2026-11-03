package ru.urfumobile.rickandmorty.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ru.urfumobile.rickandmorty.domain.ReminderScheduler

class LessonReminderScheduler(private val context: Context) : ReminderScheduler {
    override fun schedule(triggerAtMillis: Long, fullName: String) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= 31 && !alarms.canScheduleExactAlarms()) {
            throw SecurityException("Exact alarms are disabled")
        }
        val notifications = LessonReminderReceiver.notificationManager(context)
        if (!notifications.areNotificationsEnabled() ||
            notifications.getNotificationChannel(LessonReminderReceiver.CHANNEL_ID).importance == NotificationManager.IMPORTANCE_NONE
        ) {
            throw SecurityException("Notifications are disabled")
        }
        val intent = Intent(context, LessonReminderReceiver::class.java)
            .putExtra(LessonReminderReceiver.OWNER_NAME, fullName)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // The same PendingIntent replaces the previous reminder, including its owner name.
        alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
