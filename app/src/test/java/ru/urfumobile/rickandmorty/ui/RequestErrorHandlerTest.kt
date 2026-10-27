package ru.urfumobile.rickandmorty.ui

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test

class RequestErrorHandlerTest {
    @Test
    fun `critical error is rethrown instead of reported as a request failure`() {
        val error = AssertionError("fatal")
        val handler = requestErrorHandler { fail("Critical errors must not become UI messages") }

        val thrown = assertThrows(AssertionError::class.java) {
            handler.handleException(EmptyCoroutineContext, error)
        }

        assertSame(error, thrown)
    }

    @Test
    fun `cancellation does not become an error message`() {
        val handler = requestErrorHandler { fail("Cancellation must not become a UI message") }

        handler.handleException(EmptyCoroutineContext, CancellationException("cancelled"))
    }
}
