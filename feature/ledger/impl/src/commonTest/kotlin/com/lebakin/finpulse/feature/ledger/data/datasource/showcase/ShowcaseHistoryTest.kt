package com.lebakin.finpulse.feature.ledger.data.datasource.showcase

import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ShowcaseHistoryTest {
    @Test
    fun coversThreeMonthsAndMarksEveryRowAsShowcase() {
        val today = LocalDate.parse("2026-10-29")
        val snapshot = ShowcaseHistory.create(today)
        val months = snapshot.transactions.map { it.occurredOn.year to it.occurredOn.monthNumber }.toSet()
        val earliest = snapshot.transactions.minOf { it.occurredOn }
        assertEquals(3, months.size)
        assertEquals(LocalDate.parse("2026-08-01"), earliest)
        assertTrue(snapshot.transactions.all { it.occurredOn <= today })
        assertTrue(snapshot.transactions.all { it.origin == RecordOrigin.SHOWCASE })
        assertTrue(snapshot.categories.all { it.origin == RecordOrigin.SHOWCASE })
        assertTrue(snapshot.transactions.any { it.occurredOn >= today.minus(DatePeriod(days = 3)) })
    }
}
