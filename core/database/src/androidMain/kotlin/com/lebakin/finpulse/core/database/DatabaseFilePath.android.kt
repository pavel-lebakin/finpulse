package com.lebakin.finpulse.core.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val queryDispatcher: CoroutineDispatcher = Dispatchers.IO

private var applicationContext: Context? = null

fun bindAndroidDatabase(context: Context) {
    applicationContext = context.applicationContext
}

fun androidDatabaseContext(): Context = checkNotNull(applicationContext) {
    "Call bindAndroidDatabase before opening the database."
}

actual fun databaseFilePath(): String {
    val directory = androidDatabaseContext().filesDir.absolutePath
    return "$directory/finpulse.db"
}

actual fun openFinPulseDatabase(path: String): FinPulseDatabase {
    return Room.databaseBuilder<FinPulseDatabase>(context = androidDatabaseContext(), name = path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .build()
}
