package com.lebakin.finpulse.feature.assistant.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class PhraseRulesTest {
    private val rules = PhraseRules(FixedClock)

    @Test
    fun theSamePhraseProducesTheSameDraft() {
        val first = rules.read("Coffee 4.50 USD")
        val second = rules.read("Coffee 4.50 USD")
        val draft = assertIs<PhraseRead.Draft>(first).draft

        assertEquals(second, first)
        assertEquals("Coffee", draft.merchant)
        assertEquals(450L, draft.amountMinor)
        assertEquals("USD", draft.currencyCode)
        assertEquals(LocalDate(2026, 10, 29), draft.occurredOn)
        assertEquals("Eating out", draft.categoryName)
    }

    @Test
    fun aDatedTransportPhraseKeepsTheDate() {
        val draft = assertIs<PhraseRead.Draft>(rules.read("Metro 3.20 EUR on 2026-01-02")).draft

        assertEquals("Metro", draft.merchant)
        assertEquals(320L, draft.amountMinor)
        assertEquals("EUR", draft.currencyCode)
        assertEquals(LocalDate(2026, 1, 2), draft.occurredOn)
        assertEquals("Transport", draft.categoryName)
    }

    @Test
    fun howMuchThisMonthIsTheReadIntent() {
        assertEquals(PhraseRead.MonthSpend, rules.read("How much did I spend this month?"))
        assertEquals(PhraseRead.MonthSpend, rules.read("How much money did I spend this month?"))
        assertEquals(PhraseRead.MonthSpend, rules.read("spent this month"))
        assertEquals(PhraseRead.MonthSpend, rules.read("сколько я потратил за месяц"))
        assertEquals(PhraseRead.MonthSpend, rules.read("Сколько я потратила в этом месяце?"))
    }

    @Test
    fun anotherQuestionIsNotADraft() {
        assertEquals(PhraseRead.UnsupportedQuestion, rules.read("How much did I spend on coffee?"))
        assertNull(rules.read("hello"))
    }
}

private object FixedClock : PhraseClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}
