package ru.urfumobile.rickandmorty.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.urfumobile.rickandmorty.domain.ProfileRepository
import ru.urfumobile.rickandmorty.model.UserProfile

class ProfileRepositoryImpl(
    private val context: Context,
    private val client: OkHttpClient,
) : ProfileRepository {
    private val preferences = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
    private val current = MutableStateFlow(
        UserProfile(
            preferences.getString("name", "").orEmpty(),
            preferences.getString("photo", "").orEmpty(),
            preferences.getString("resume", "").orEmpty(),
        ),
    )
    override val profile = current.asStateFlow()

    override suspend fun save(profile: UserProfile) = withContext(Dispatchers.IO) {
        check(preferences.edit()
            .putString("name", profile.fullName)
            .putString("photo", profile.photoUri)
            .putString("resume", profile.resumeUrl)
            .commit())
        current.value = profile
        File(context.filesDir, "avatars").listFiles()
            ?.filter { it.name != Uri.parse(profile.photoUri).lastPathSegment }
            ?.forEach { it.delete() }
        Unit
    }

    override suspend fun importPhoto(uri: String): String = withContext(Dispatchers.IO) {
        val source = Uri.parse(uri)
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(source)) ?: "jpg"
        val directory = File(context.filesDir, "avatars").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".$extension", directory)
        try {
            requireNotNull(context.contentResolver.openInputStream(source)).use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, options)
            require(options.outWidth > 0 && options.outHeight > 0)
            fileUri(file)
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    override suspend fun createCameraPhoto(): String = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "camera").apply { mkdirs() }
        fileUri(File(directory, "photo.jpg").apply { writeBytes(byteArrayOf()) })
    }

    override suspend fun downloadResume(url: String): String = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "resumes").apply { mkdirs() }
        val file = File.createTempFile("resume_", ".pdf", directory)
        try {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                check(response.isSuccessful)
                requireNotNull(response.body).byteStream().use { input ->
                    file.outputStream().use { input.copyTo(it) }
                }
            }
            val header = ByteArray(5)
            file.inputStream().use { it.read(header) }
            require(header.contentEquals("%PDF-".toByteArray()))
            fileUri(file)
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    private fun fileUri(file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.files", file).toString()
}
