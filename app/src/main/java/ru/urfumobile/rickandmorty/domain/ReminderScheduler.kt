package ru.urfumobile.rickandmorty.domain

fun interface ReminderScheduler {
    fun schedule(triggerAtMillis: Long, fullName: String)
}
