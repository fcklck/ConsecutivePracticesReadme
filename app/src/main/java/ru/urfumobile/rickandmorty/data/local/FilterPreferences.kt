package ru.urfumobile.rickandmorty.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.urfumobile.rickandmorty.model.CharacterFilters

class FilterPreferences(private val preferences: SharedPreferences) {
    fun read() = CharacterFilters(
        name = preferences.getString("name", "").orEmpty(),
        status = preferences.getString("status", "").orEmpty(),
    )

    fun save(filters: CharacterFilters) = preferences.edit {
        putString("name", filters.name)
        putString("status", filters.status)
    }
}
