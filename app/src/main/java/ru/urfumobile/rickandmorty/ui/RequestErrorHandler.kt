package ru.urfumobile.rickandmorty.ui

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler

internal fun requestErrorHandler(onError: (String) -> Unit) = CoroutineExceptionHandler { _, error ->
    when (error) {
        is CancellationException -> Unit
        is IOException -> onError("Нет подключения к интернету. Проверьте сеть и повторите.")
        is Exception -> onError("Не удалось загрузить данные. Попробуйте ещё раз.")
        else -> throw error
    }
}
