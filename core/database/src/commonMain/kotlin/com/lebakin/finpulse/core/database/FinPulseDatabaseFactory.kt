package com.lebakin.finpulse.core.database

import kotlinx.coroutines.CoroutineDispatcher

internal expect val queryDispatcher: CoroutineDispatcher

expect fun databaseFilePath(): String

expect fun openFinPulseDatabase(path: String): FinPulseDatabase
