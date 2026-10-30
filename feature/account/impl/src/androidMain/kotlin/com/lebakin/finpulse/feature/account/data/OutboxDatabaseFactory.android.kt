package com.lebakin.finpulse.feature.account.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lebakin.finpulse.core.database.androidDatabaseContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val queryDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun openOutboxDatabase(path: String): OutboxDatabase =
    Room.databaseBuilder<OutboxDatabase>(context = androidDatabaseContext(), name = path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .build()
