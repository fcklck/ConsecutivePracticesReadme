package ru.urfumobile.rickandmorty.model

data class CharacterFilters(val name: String = "", val status: String = "") {
    val isActive: Boolean get() = name.isNotBlank() || status.isNotEmpty()
}
