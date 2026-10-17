package com.lebakin.finpulse.core.ai

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class OnDeviceEngineTest {
    @Test
    fun anUnregisteredEngineDoesNotPromiseAModel() = runTest {
        assertNull(openInstalledEngine(null))
    }

    @Test
    fun anEngineThatIsNotReadyDoesNotPromiseAModel() = runTest {
        assertNull(openInstalledEngine(ScriptedEngine(isReady = false)))
    }

    @Test
    fun aReadyEngineReturnsTrimmedText() = runTest {
        val model = openInstalledEngine(ScriptedEngine(isReady = true, reply = "  cafe  "))
        assertEquals("cafe", model?.complete("prompt"))
    }

    @Test
    fun anEmptyReplyIsAFailure() = runTest {
        val model = openInstalledEngine(ScriptedEngine(isReady = true, reply = " "))
        assertFailsWith<OnDeviceFailed> {
            model?.complete("prompt")
        }
    }

    @Test
    fun aBlankPromptIsRejected() = runTest {
        val model = openInstalledEngine(ScriptedEngine(isReady = true, reply = "cafe"))
        assertFailsWith<IllegalArgumentException> {
            model?.complete(" ")
        }
    }

    @Test
    fun anEngineFailureIsOnDeviceFailed() = runTest {
        val model = openInstalledEngine(ScriptedEngine(isReady = true, failure = IllegalStateException("down")))
        val error = assertFailsWith<OnDeviceFailed> {
            model?.complete("prompt")
        }
        assertEquals("down", error.cause?.message)
    }

    @Test
    fun anOnDeviceFailureIsRethrown() = runTest {
        val failure = OnDeviceFailed()
        val model = openInstalledEngine(ScriptedEngine(isReady = true, failure = failure))
        assertSame(
            failure,
            assertFailsWith<OnDeviceFailed> {
                model?.complete("prompt")
            },
        )
    }

    @Test
    fun cancellationFromReadyIsRethrown() = runTest {
        assertFailsWith<CancellationException> {
            openInstalledEngine(ScriptedEngine(readyFailure = CancellationException("stopped")))
        }
    }

    @Test
    fun cancellationFromCompleteIsRethrown() = runTest {
        val model = openInstalledEngine(
            ScriptedEngine(isReady = true, failure = CancellationException("stopped")),
        )
        assertFailsWith<CancellationException> {
            model?.complete("prompt")
        }
    }

    @Test
    fun aReadyCheckFailureIsNotAModel() = runTest {
        assertNull(openInstalledEngine(ScriptedEngine(readyFailure = IllegalStateException("unavailable"))))
    }
}

private class ScriptedEngine(
    private val isReady: Boolean = false,
    private val reply: String = "",
    private val failure: Throwable? = null,
    private val readyFailure: Throwable? = null,
) : OnDeviceEngine {
    override suspend fun ready(): Boolean {
        readyFailure?.let { throw it }
        return isReady
    }

    override suspend fun complete(prompt: String): String {
        failure?.let { throw it }
        return reply
    }
}
