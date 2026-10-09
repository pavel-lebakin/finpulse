package com.lebakin.finpulse.feature.ledger.data.repository

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun interface LedgerClock {
    fun today(): LocalDate
}

internal class SystemLedgerClock : LedgerClock {
    override fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
