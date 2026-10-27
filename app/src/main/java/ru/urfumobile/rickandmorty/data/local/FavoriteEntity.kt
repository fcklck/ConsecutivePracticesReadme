package ru.urfumobile.rickandmorty.data.local

import androidx.room.Embedded
import androidx.room.Entity
import ru.urfumobile.rickandmorty.model.Character

@Entity(tableName = "favorites", primaryKeys = ["id"])
data class FavoriteEntity(@Embedded val character: Character)
