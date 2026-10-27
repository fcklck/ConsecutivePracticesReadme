package ru.urfumobile.rickandmorty

import android.app.Application
import ru.urfumobile.rickandmorty.di.AppContainer

class RickAndMortyApplication : Application() {
    val container by lazy { AppContainer(this) }
}
