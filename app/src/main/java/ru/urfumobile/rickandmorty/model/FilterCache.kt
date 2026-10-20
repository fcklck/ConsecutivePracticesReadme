package ru.urfumobile.rickandmorty.model

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow

// Один экземпляр для списка и настроек; состояние бейджа хранится только в памяти.
class FilterCache(initial: CharacterFilters) {
    private val mutableFilters = MutableStateFlow(initial)
    val filters = mutableFilters.asStateFlow()
    val showBadge: Boolean get() = filters.value.isActive

    fun update(filters: CharacterFilters) {
        mutableFilters.value = filters
    }
}
