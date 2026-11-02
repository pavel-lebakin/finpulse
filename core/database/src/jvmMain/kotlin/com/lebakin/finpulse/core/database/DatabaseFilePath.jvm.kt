package com.lebakin.finpulse.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val queryDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun databaseFilePath(): String {
    val override = System.getProperty("finpulse.database.file")
    if (!override.isNullOrBlank()) {
        return override
    }
    val directory = File(System.getProperty("user.home"), ".finpulse")
    directory.mkdirs()
    return File(directory, "finpulse.db").absolutePath
}

actual fun openFinPulseDatabase(path: String): FinPulseDatabase =
    Room.databaseBuilder<FinPulseDatabase>(name = path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .addMigrations(Migration1To2)
        .build()
