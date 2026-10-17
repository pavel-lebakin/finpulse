package com.lebakin.finpulse.core.ai

import kotlin.coroutines.cancellation.CancellationException

interface OnDeviceEngine {
    suspend fun ready(): Boolean

    suspend fun complete(prompt: String): String
}

// A ready-check failure is not a model. CancellationException is rethrown above.
@Suppress("TooGenericExceptionCaught")
internal suspend fun openInstalledEngine(engine: OnDeviceEngine?): OnDeviceModel? {
    val installed = engine ?: return null
    val ready = try {
        installed.ready()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }
    return onDeviceModelFor(ready, InstalledOnDeviceModel(installed))
}

private class InstalledOnDeviceModel(
    private val engine: OnDeviceEngine,
) : OnDeviceModel {
    // An engine failure becomes OnDeviceFailed. CancellationException is rethrown above.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun complete(prompt: String): String {
        require(prompt.isNotBlank()) { "Prompt is empty." }
        val failure = try {
            val text = engine.complete(prompt).trim()
            if (text.isEmpty()) OnDeviceFailed() else return text
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            error
        }
        throw if (failure is OnDeviceFailed) failure else OnDeviceFailed(failure)
    }
}
