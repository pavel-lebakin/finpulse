package com.lebakin.finpulse.feature.scanner.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.datetime.LocalDate

class ReceiptParserTest {
    private val parser = ReceiptParser(FixedClock)
    private val today = LocalDate(2026, 10, 29)

    @Test
    fun theSameTextProducesTheSameDraft() {
        val text = ReceiptSamples.text("groceries").orEmpty()
        val first = parser.parse(text)
        val second = parser.parse(text)
        assertEquals(first, second)
        assertEquals("Market", first.merchant)
        assertEquals(1_250L, first.amountMinor)
        assertEquals("USD", first.currencyCode)
        assertEquals(LocalDate(2026, 10, 2), first.occurredOn)
        assertEquals("Groceries", first.categoryName)
    }

    @Test
    fun cafeSampleUsesTheTotalLine() {
        val draft = parser.parse(ReceiptSamples.text("cafe").orEmpty())
        assertEquals("North Cafe", draft.merchant)
        assertEquals(450L, draft.amountMinor)
        assertEquals("EUR", draft.currencyCode)
        assertEquals(LocalDate(2026, 10, 1), draft.occurredOn)
        assertEquals("Eating out", draft.categoryName)
    }

    @Test
    fun metroSampleUsesTheLargestAmountWhenThereIsNoTotal() {
        val draft = parser.parse(ReceiptSamples.text("metro").orEmpty())
        assertEquals("City Metro", draft.merchant)
        assertEquals(800L, draft.amountMinor)
        assertEquals("RUB", draft.currencyCode)
        assertEquals(LocalDate(2026, 2, 13), draft.occurredOn)
        assertEquals("Transport", draft.categoryName)
    }

    @Test
    fun symbolAmountAndMissingDateUseToday() {
        val draft = parser.parse("Corner Shop\nTOTAL \$3.25")
        assertEquals("Corner Shop", draft.merchant)
        assertEquals(325L, draft.amountMinor)
        assertEquals("USD", draft.currencyCode)
        assertEquals(today, draft.occurredOn)
        assertEquals("General", draft.categoryName)
    }

    @Test
    fun businessLunchIsNotTransport() {
        val draft = parser.parse("Business lunch\nTOTAL 10.00 USD")
        assertEquals("General", draft.categoryName)
    }

    @Test
    fun dayFirstWhenBothNumbersCouldBeAMonth() {
        val draft = parser.parse("Shop\n01/02/2026\nTOTAL 1.00 USD")
        assertEquals(LocalDate(2026, 2, 1), draft.occurredOn)
    }

    @Test
    fun emptyTextIsUnreadable() {
        assertFailsWith<ReceiptUnreadable> { parser.parse("   ") }
    }

    @Test
    fun textWithoutAnAmountIsUnreadable() {
        assertFailsWith<ReceiptUnreadable> { parser.parse("Market\nHello") }
    }

    @Test
    fun userReceiptIsAnExpenseWithDistinctIds() {
        val draft = parser.parse(ReceiptSamples.text("groceries").orEmpty())
        val write = draft.toUserReceipt(SequenceIds())
        assertEquals("id-1", write.category.id)
        assertEquals("id-2", write.transaction.id)
        assertEquals(write.category.id, write.transaction.categoryId)
        assertEquals(com.lebakin.finpulse.feature.ledger.api.RecordOrigin.USER, write.transaction.origin)
        assertEquals(com.lebakin.finpulse.feature.ledger.api.MoneyDirection.EXPENSE, write.transaction.direction)
    }
}

private object FixedClock : ReceiptClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class SequenceIds : ReceiptIds {
    private var value = 0

    override fun next(): String {
        value += 1
        return "id-$value"
    }
}
