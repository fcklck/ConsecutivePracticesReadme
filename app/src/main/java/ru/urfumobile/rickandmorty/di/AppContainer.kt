package ru.urfumobile.rickandmorty.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.urfumobile.rickandmorty.data.CharacterRepositoryImpl
import ru.urfumobile.rickandmorty.data.remote.CharacterApi
import ru.urfumobile.rickandmorty.domain.GetCharacterUseCase
import ru.urfumobile.rickandmorty.domain.GetCharactersUseCase
import java.util.concurrent.TimeUnit

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

    private val repository = CharacterRepositoryImpl(api)

    val getCharacters = GetCharactersUseCase(repository)
    val getCharacter = GetCharacterUseCase(repository)
}
