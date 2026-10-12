package com.lebakin.finpulse.feature.scanner.domain

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun interface ReceiptClock {
    fun today(): LocalDate
}

internal class SystemReceiptClock : ReceiptClock {
    override fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

internal class SystemReceiptIds : ReceiptIds {
    override fun next(): String = "user-${kotlin.random.Random.nextLong().toULong()}"
}
