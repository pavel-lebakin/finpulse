package com.lebakin.finpulse.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AppErrorTest {
    @Test
    fun noConnectionKeepsTheCause() {
        val failure = NoConnectionException()
        val error = failure.toAppError()
        assertIs<AppError.NoConnection>(error)
        assertEquals(failure, error.cause)
    }

    @Test
    fun serverKeepsTheStatusCode() {
        val failure = ServerStatusException(statusCode = 503)
        val error = failure.toAppError()
        assertIs<AppError.Server>(error)
        assertEquals(503, error.statusCode)
        assertEquals(failure, error.cause)
    }

    @Test
    fun unexpectedWrapsAnythingElse() {
        val failure = IllegalStateException("boom")
        val error = failure.toAppError()
        assertIs<AppError.Unexpected>(error)
        assertEquals(failure, error.cause)
    }

    @Test
    fun cancellationIsNotAnAppError() {
        assertFailsWith<CancellationException> {
            CancellationException("stop").toAppError()
        }
    }
}
