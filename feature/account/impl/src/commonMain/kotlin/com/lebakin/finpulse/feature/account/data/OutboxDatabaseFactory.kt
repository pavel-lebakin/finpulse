package com.lebakin.finpulse.feature.account.data

import kotlinx.coroutines.CoroutineDispatcher

internal expect val queryDispatcher: CoroutineDispatcher

expect fun openOutboxDatabase(path: String): OutboxDatabase

internal fun outboxFilePath(ledgerPath: String): String {
    val separator = ledgerPath.lastIndexOf('/')
    val directory = if (separator >= 0) ledgerPath.substring(0, separator + 1) else ""
    return directory + "finpulse-outbox.db"
}
