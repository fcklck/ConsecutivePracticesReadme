package ru.urfumobile.rickandmorty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteEntity::class], version = 1)
abstract class CharacterDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
}
