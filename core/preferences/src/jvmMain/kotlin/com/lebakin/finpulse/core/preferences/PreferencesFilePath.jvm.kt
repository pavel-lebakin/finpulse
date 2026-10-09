package com.lebakin.finpulse.core.preferences

import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val preferencesDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun preferencesFilePath(): String {
    val override = System.getProperty("finpulse.preferences.file")
    if (!override.isNullOrBlank()) {
        return override
    }
    val directory = File(System.getProperty("user.home"), ".finpulse")
    directory.mkdirs()
    return File(directory, "finpulse.preferences_pb").absolutePath
}
