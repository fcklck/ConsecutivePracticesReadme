package ru.urfumobile.rickandmorty.di

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import ru.urfumobile.rickandmorty.data.CharacterRepositoryImpl
import ru.urfumobile.rickandmorty.data.local.CharacterDatabase
import ru.urfumobile.rickandmorty.data.local.FilterPreferences
import ru.urfumobile.rickandmorty.data.remote.CharacterApi
import ru.urfumobile.rickandmorty.domain.CharacterRepository
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import ru.urfumobile.rickandmorty.domain.GetCharacterUseCase
import ru.urfumobile.rickandmorty.model.FilterCache

class AppContainer(context: Context) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
        )
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CharacterApi::class.java)

    private val database = Room.databaseBuilder(
        context, CharacterDatabase::class.java, "characters.db",
    ).build()

    val filterPreferences = FilterPreferences(
        context.getSharedPreferences("filters", Context.MODE_PRIVATE),
    )
    val filterCache = FilterCache(filterPreferences.read())
    val repository: CharacterRepository = CharacterRepositoryImpl(api, database.favorites())

    val getCharacters = GetCharactersUseCase(repository)
    val getCharacter = GetCharacterUseCase(repository)
}
