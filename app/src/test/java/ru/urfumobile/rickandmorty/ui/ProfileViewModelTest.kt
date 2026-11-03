package ru.urfumobile.rickandmorty.ui

import androidx.lifecycle.SavedStateHandle
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
import ru.urfumobile.rickandmorty.domain.ReminderScheduler
import ru.urfumobile.rickandmorty.model.UserProfile

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T05:00:45Z"), ZoneId.of("Asia/Yekaterinburg"))

    private fun editor(
        repository: ProfileRepository,
        handle: SavedStateHandle = SavedStateHandle(),
        reminders: ReminderScheduler = FakeReminders(),
    ) = EditProfileViewModel(repository, handle, reminders, clock)

    @Test
    fun `photo and text are drafts until Done and survive view model recreation`() = runTest {
        val repository = FakeProfileRepository()
        val handle = SavedStateHandle()
        val editor = editor(repository, handle)
        editor.edit(UserProfile("  Никита Франгу  ", resumeUrl = " https://example.com/resume.pdf ", lessonTime = "12:30"))
        editor.importPhoto("content://gallery/1")
        advanceUntilIdle()
        assertEquals(UserProfile(), repository.profile.value)

        val restored = editor(repository, handle)
        assertEquals("content://files/avatar.jpg", restored.state.value.draft.photoUri)
        assertEquals("12:30", restored.state.value.draft.lessonTime)
        restored.save()
        advanceUntilIdle()
        assertTrue(restored.state.value.saved)
        assertEquals("Никита Франгу", repository.profile.value.fullName)
        assertEquals("https://example.com/resume.pdf", repository.profile.value.resumeUrl)
    }

    @Test
    fun `invalid resume link does not save and reports error`() = runTest {
        val repository = FakeProfileRepository()
        val editor = editor(repository)
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
        val reminders = FakeReminders()
        val editor = editor(repository, reminders = reminders)
        editor.edit(UserProfile("Никита", lessonTime = "12:30"))
        repository.fail = true
        editor.save()
        advanceUntilIdle()
        assertFalse(editor.state.value.busy)
        assertFalse(editor.state.value.saved)
        assertNotNull(editor.state.value.error)
        assertEquals(UserProfile(), repository.profile.value)
        assertNull(reminders.scheduled)
        repository.fail = false
        editor.save()
        advanceUntilIdle()
        assertTrue(editor.state.value.saved)
    }

    @Test
    fun `failed photo import preserves current draft photo`() = runTest {
        val repository = FakeProfileRepository()
        val editor = editor(repository)
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

    @Test
    fun `only strict 24 hour times are accepted`() {
        listOf("00:00", "09:05", "12:30", "23:59").forEach {
            assertNull(UserProfile(lessonTime = it).lessonTimeError())
        }
        listOf("", " ", "9:05", "09:5", "24:00", "12:60", "-1:30", "12.30", "ab:cd", "12:30:00", " 12:30").forEach {
            assertNotNull("Invalid time: $it", UserProfile(lessonTime = it).lessonTimeError())
        }
    }

    @Test
    fun `invalid time never saves or schedules`() = runTest {
        val repository = FakeProfileRepository()
        val reminders = FakeReminders()
        val editor = editor(repository, reminders = reminders)
        listOf("", "24:00", "10:90", "no time").forEach { time ->
            editor.edit(UserProfile("Никита", lessonTime = time))
            editor.save()
            advanceUntilIdle()
            assertNotNull(editor.state.value.error)
            assertFalse(editor.state.value.saved)
            assertEquals(UserProfile(), repository.profile.value)
            assertNull(reminders.scheduled)
        }
    }

    @Test
    fun `past time and current minute are not silently moved to tomorrow`() = runTest {
        val repository = FakeProfileRepository()
        val reminders = FakeReminders()
        val editor = editor(repository, reminders = reminders)
        listOf("00:00", "09:59", "10:00").forEach { time ->
            editor.edit(UserProfile("Никита", lessonTime = time))
            editor.save()
            advanceUntilIdle()
            assertTrue(editor.state.value.error.orEmpty().contains("уже прошло"))
            assertEquals(UserProfile(), repository.profile.value)
            assertNull(reminders.scheduled)
        }
    }

    @Test
    fun `reminder uses todays local date and captures the saved name`() = runTest {
        val repository = FakeProfileRepository()
        val reminders = FakeReminders()
        val editor = editor(repository, reminders = reminders)
        editor.edit(UserProfile("  Никита  ", lessonTime = "12:30"))
        editor.save()
        advanceUntilIdle()
        assertEquals(Instant.parse("2026-11-03T07:30:00Z").toEpochMilli() to "Никита", reminders.scheduled)
        assertEquals("12:30", repository.profile.value.lessonTime)
        assertTrue(editor.state.value.saved)
        editor(repository, reminders = reminders).edit(UserProfile("Другое имя", lessonTime = "13:30"))
        assertEquals("Никита", reminders.scheduled?.second)
    }

    @Test
    fun `revoked permission reports incomplete reminder and allows retry`() = runTest {
        val repository = FakeProfileRepository()
        val reminders = FakeReminders().apply { denied = true }
        val editor = editor(repository, reminders = reminders)
        editor.edit(UserProfile("Никита", lessonTime = "12:30"))
        editor.save()
        advanceUntilIdle()
        assertFalse(editor.state.value.saved)
        assertFalse(editor.state.value.busy)
        assertTrue(editor.state.value.error.orEmpty().contains("напоминание не установлено"))
        assertNull(reminders.scheduled)
        reminders.denied = false
        editor.save()
        advanceUntilIdle()
        assertTrue(editor.state.value.saved)
        assertNotNull(reminders.scheduled)
    }
}

private class FakeReminders : ReminderScheduler {
    var scheduled: Pair<Long, String>? = null
    var denied = false
    override fun schedule(triggerAtMillis: Long, fullName: String) {
        if (denied) throw SecurityException()
        scheduled = triggerAtMillis to fullName
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
