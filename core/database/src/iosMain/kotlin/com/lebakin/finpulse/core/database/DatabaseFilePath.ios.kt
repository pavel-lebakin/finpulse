package com.lebakin.finpulse.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual val queryDispatcher: CoroutineDispatcher = Dispatchers.Default

@OptIn(ExperimentalForeignApi::class)
actual fun databaseFilePath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val directory = requireNotNull(url?.path) { "Documents directory is unavailable." }
    return "$directory/finpulse.db"
}

actual fun openFinPulseDatabase(path: String): FinPulseDatabase =
    Room.databaseBuilder<FinPulseDatabase>(name = path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .build()
