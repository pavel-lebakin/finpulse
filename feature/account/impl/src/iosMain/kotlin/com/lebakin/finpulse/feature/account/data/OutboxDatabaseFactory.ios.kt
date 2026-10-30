package com.lebakin.finpulse.feature.account.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val queryDispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun openOutboxDatabase(path: String): OutboxDatabase =
    Room.databaseBuilder<OutboxDatabase>(name = path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .build()
