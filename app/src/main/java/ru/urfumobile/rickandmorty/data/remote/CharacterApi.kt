package ru.urfumobile.rickandmorty.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterApi {
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String?,
        @Query("status") status: String?,
    ): CharacterPageDto

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): CharacterDto
}
