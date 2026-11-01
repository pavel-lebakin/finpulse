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
        assertEquals(listOf("Milk", "Bread"), first.lines.map { it.name })
        assertEquals(listOf(240L, 310L), first.lines.map { it.amountMinor })
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
    fun metroSampleSumsTheItemsWhenThereIsNoTotal() {
        val draft = parser.parse(ReceiptSamples.text("metro").orEmpty())
        assertEquals("City Metro", draft.merchant)
        assertEquals(1_075L, draft.amountMinor)
        assertEquals(listOf("Single ride", "Day pass"), draft.lines.map { it.name })
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
    fun aPriceOnTheNextLineStaysWithTheItem() {
        val draft = parser.parse("Market\nMilk\n2.40\nBread\n3.10\nTOTAL\n12.50 USD")
        assertEquals(listOf("Milk", "Bread"), draft.lines.map { it.name })
        assertEquals(listOf(240L, 310L), draft.lines.map { it.amountMinor })
        assertEquals(1_250L, draft.amountMinor)
    }

    @Test
    fun aSplitPriceAndAThousandsSeparatorStayIntact() {
        val split = parser.parse("Market\nMilk 2 40\nTOTAL 2.40 USD")
        assertEquals(240L, split.lines.single().amountMinor)
        val us = parser.parse("Market\nTOTAL 1,250.00 USD")
        assertEquals(125_000L, us.amountMinor)
        val eu = parser.parse("Market\nTOTAL 1.250,00 EUR")
        assertEquals(125_000L, eu.amountMinor)
        assertEquals("EUR", eu.currencyCode)
    }

    @Test
    fun aSpacedDecimalIsAPrice() {
        val draft = parser.parse("Market\nMilk 2 . 4O\nTOTAL 2.40 USD")
        assertEquals(240L, draft.lines.single().amountMinor)
        assertEquals("Milk", draft.lines.single().name)
    }

    @Test
    fun aDottedDateIsNotAPrice() {
        val draft = parser.parse("North Cafe\n01.10.2026\nCoffee 4.50 EUR\nTOTAL 4.50 EUR")
        assertEquals(450L, draft.amountMinor)
        assertEquals(listOf("Coffee"), draft.lines.map { it.name })
        assertEquals(LocalDate(2026, 10, 1), draft.occurredOn)
    }

    @Test
    fun russianTotalKeepsTheItems() {
        val draft = parser.parse("Магазин\nМолоко 2,40\nХлеб 3,10\nИтого 12,50 руб")
        assertEquals("Магазин", draft.merchant)
        assertEquals(1_250L, draft.amountMinor)
        assertEquals("RUB", draft.currencyCode)
        assertEquals(listOf("Молоко", "Хлеб"), draft.lines.map { it.name })
    }

    @Test
    fun russianTextWithoutASymbolHintsRubles() {
        val draft = parser.parse("Магазин\nМолоко 2,40\nХлеб 3,10\nИтого 5,50")
        assertEquals("RUB", draft.currencyCode)
        assertEquals(550L, draft.amountMinor)
    }

    @Test
    fun portugueseReceiptHintsEuroAndSkipsTax() {
        val draft = parser.parse("Padaria\nPão 1,20\nLeite 0,89\nIVA 0,40\nTotal 2,49")
        assertEquals("EUR", draft.currencyCode)
        assertEquals(249L, draft.amountMinor)
        assertEquals(listOf("Pão", "Leite"), draft.lines.map { it.name })
    }

    @Test
    fun englishPoundsAreNotDollars() {
        val draft = parser.parse("Cafe\nCoffee 4.50\nTotal 4.50 pounds")
        assertEquals("GBP", draft.currencyCode)
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
