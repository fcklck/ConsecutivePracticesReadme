package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.ViewModel
import ru.urfumobile.rickandmorty.data.local.FilterPreferences
import ru.urfumobile.rickandmorty.model.CharacterFilters
import ru.urfumobile.rickandmorty.model.FilterCache

class FiltersViewModel(
    private val preferences: FilterPreferences,
    private val cache: FilterCache,
) : ViewModel() {
    val initialFilters = cache.filters.value

    fun save(name: String, status: String) {
        val filters = CharacterFilters(name.trim(), status)
        preferences.save(filters)
        cache.update(filters)
    }
}
