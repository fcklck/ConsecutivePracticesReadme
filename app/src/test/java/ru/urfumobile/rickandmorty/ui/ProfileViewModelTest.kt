package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.SavedStateHandle
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.urfumobile.rickandmorty.MainDispatcherRule
import ru.urfumobile.rickandmorty.domain.ProfileRepository
import ru.urfumobile.rickandmorty.model.UserProfile

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `photo and text are drafts until Done and survive view model recreation`() = runTest {
        val repository = FakeProfileRepository()
        val handle = SavedStateHandle()
        val editor = EditProfileViewModel(repository, handle)
        editor.edit(UserProfile("  Никита Франгу  ", resumeUrl = " https://example.com/resume.pdf "))
        editor.importPhoto("content://gallery/1")
        advanceUntilIdle()
        assertEquals(UserProfile(), repository.profile.value)

        val restored = EditProfileViewModel(repository, handle)
        assertEquals("content://files/avatar.jpg", restored.state.value.draft.photoUri)
        restored.save()
        advanceUntilIdle()
        assertTrue(restored.state.value.saved)
        assertEquals("Никита Франгу", repository.profile.value.fullName)
        assertEquals("https://example.com/resume.pdf", repository.profile.value.resumeUrl)
    }

    @Test
    fun `invalid resume link does not save and reports error`() = runTest {
        val repository = FakeProfileRepository()
        val editor = EditProfileViewModel(repository, SavedStateHandle())
        editor.edit(UserProfile("Никита", resumeUrl = "file:///private.pdf"))
        editor.save()
        advanceUntilIdle()
        assertNotNull(editor.state.value.error)
        assertFalse(editor.state.value.saved)
        assertEquals(UserProfile(), repository.profile.value)
    }

    @Test
    fun `failed save leaves stored profile unchanged and permits retry`() = runTest {
        val repository = FakeProfileRepository()
        val editor = EditProfileViewModel(repository, SavedStateHandle())
        editor.edit(UserProfile("Никита"))
        repository.fail = true
        editor.save()
        advanceUntilIdle()
        assertFalse(editor.state.value.busy)
        assertFalse(editor.state.value.saved)
        assertNotNull(editor.state.value.error)
        assertEquals(UserProfile(), repository.profile.value)
        repository.fail = false
        editor.save()
        advanceUntilIdle()
        assertTrue(editor.state.value.saved)
    }

    @Test
    fun `failed photo import preserves current draft photo`() = runTest {
        val repository = FakeProfileRepository()
        val editor = EditProfileViewModel(repository, SavedStateHandle())
        editor.edit(UserProfile("Никита", photoUri = "content://files/existing.jpg"))
        repository.fail = true
        editor.importPhoto("content://gallery/broken")
        advanceUntilIdle()
        assertEquals("content://files/existing.jpg", editor.state.value.draft.photoUri)
        assertNotNull(editor.state.value.error)
        assertFalse(editor.state.value.busy)
    }

    @Test
    fun `download failure does not open file and retry returns local URI`() = runTest {
        val repository = FakeProfileRepository()
        repository.profile.value = UserProfile("Никита", resumeUrl = "https://example.com/resume.pdf")
        val viewModel = ProfileViewModel(repository)
        repository.fail = true
        viewModel.downloadResume()
        advanceUntilIdle()
        assertFalse(viewModel.resume.value.downloading)
        assertNull(viewModel.resume.value.fileUri)
        assertNotNull(viewModel.resume.value.error)
        repository.fail = false
        viewModel.downloadResume()
        advanceUntilIdle()
        assertEquals("content://files/resume.pdf", viewModel.resume.value.fileUri)
        viewModel.clearResult()
        assertNull(viewModel.resume.value.fileUri)
    }
}

private class FakeProfileRepository : ProfileRepository {
    override val profile = MutableStateFlow(UserProfile())
    var fail = false
    override suspend fun save(profile: UserProfile) {
        if (fail) throw IOException()
        this.profile.value = profile
    }
    override suspend fun importPhoto(uri: String): String {
        if (fail) throw IOException()
        return "content://files/avatar.jpg"
    }
    override suspend fun createCameraPhoto() = "content://files/camera.jpg"
    override suspend fun downloadResume(url: String): String {
        if (fail) throw IOException()
        return "content://files/resume.pdf"
    }
}
