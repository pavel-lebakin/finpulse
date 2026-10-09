package com.lebakin.finpulse.core.preferences

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val preferencesDispatcher: CoroutineDispatcher = Dispatchers.IO

private var filesDirectory: String? = null

fun bindAndroidPreferencesDirectory(directory: String) {
    filesDirectory = directory
}

actual fun preferencesFilePath(): String {
    val directory = checkNotNull(filesDirectory) {
        "Call bindAndroidPreferencesDirectory before opening preferences."
    }
    return "$directory/finpulse.preferences_pb"
}
