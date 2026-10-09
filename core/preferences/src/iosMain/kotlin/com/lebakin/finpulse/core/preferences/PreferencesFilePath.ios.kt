package com.lebakin.finpulse.core.preferences

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual val preferencesDispatcher: CoroutineDispatcher = Dispatchers.Default

@OptIn(ExperimentalForeignApi::class)
actual fun preferencesFilePath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val directory = requireNotNull(url?.path) { "Documents directory is unavailable." }
    return "$directory/finpulse.preferences_pb"
}
