package com.lebakin.finpulse.feature.assistant.domain

import com.lebakin.finpulse.core.ai.ByokFailed
import com.lebakin.finpulse.core.ai.OnDeviceFailed
import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class PhraseEnginesTest {
    private val rules = PhraseRules(EngineClock)

    @Test
    fun aQuestionDoesNotOpenAModel() = runTest {
        var opened = 0
        val engines = engines(
            onDevice = {
                opened += 1
                null
            },
            byok = {
                opened += 1
                null
            },
        )

        assertEquals(PhraseResult.MonthSpend, engines.read("How much did I spend?"))
        assertEquals(PhraseResult.UnsupportedQuestion, engines.read("Why is the bus late?"))
        assertEquals(0, opened)
    }

    @Test
    fun aRuleDraftDoesNotOpenByok() = runTest {
        var byokOpened = 0
        val engines = engines(byok = {
            byokOpened += 1
            CountingModel(ACCEPTED)
        })

        val draft = assertIs<PhraseResult.Draft>(engines.read("Coffee 4.50 USD")).draft

        assertEquals("Coffee", draft.merchant)
        assertEquals(0, byokOpened)
    }

    @Test
    fun anAcceptedOnDeviceReplyBecomesTheDraft() = runTest {
        var byokOpened = 0
        val engines = engines(
            onDevice = { CountingModel(ACCEPTED) },
            byok = {
                byokOpened += 1
                null
            },
        )

        val draft = assertIs<PhraseResult.Draft>(engines.read("lunch 12")).draft

        assertEquals("Model Shop", draft.merchant)
        assertEquals(999L, draft.amountMinor)
        assertEquals("GBP", draft.currencyCode)
        assertEquals(LocalDate(2026, 3, 4), draft.occurredOn)
        assertEquals("Transport", draft.categoryName)
        assertEquals(0, byokOpened)
    }

    @Test
    fun aRejectedOnDeviceReplyUsesByok() = runTest {
        val engines = engines(
            onDevice = { CountingModel("not json") },
            byok = { CountingModel(ACCEPTED) },
        )

        assertEquals("Model Shop", assertIs<PhraseResult.Draft>(engines.read("lunch 12")).draft.merchant)
    }

    @Test
    fun onDeviceFailureUsesByok() = runTest {
        val engines = engines(
            onDevice = { CountingModel { throw OnDeviceFailed() } },
            byok = { CountingModel(ACCEPTED) },
        )

        assertEquals("Model Shop", assertIs<PhraseResult.Draft>(engines.read("lunch 12")).draft.merchant)
    }

    @Test
    fun aPhraseWithoutADigitDoesNotOpenAModel() = runTest {
        var opened = 0
        val engines = engines(
            onDevice = {
                opened += 1
                CountingModel(ACCEPTED)
            },
            byok = {
                opened += 1
                CountingModel(ACCEPTED)
            },
        )

        assertFailsWith<PhraseUnreadable> { engines.read("hello there") }
        assertEquals(0, opened)
    }

    @Test
    fun aRejectedReplyStaysUnreadable() = runTest {
        val engines = engines(
            onDevice = { CountingModel("not json") },
            byok = { CountingModel("""{"intent":"drop"}""") },
        )

        assertFailsWith<PhraseUnreadable> { engines.read("lunch 12") }
    }

    @Test
    fun byokFailureStaysUnreadable() = runTest {
        val engines = engines(byok = { CountingModel { throw ByokFailed() } })

        assertFailsWith<PhraseUnreadable> { engines.read("lunch 12") }
    }

    @Test
    fun blankTextDoesNotOpenAModel() = runTest {
        var opened = 0
        val engines = engines(
            onDevice = {
                opened += 1
                null
            },
            byok = {
                opened += 1
                null
            },
        )

        assertFailsWith<PhraseUnreadable> { engines.read("   ") }
        assertEquals(0, opened)
    }

    @Test
    fun onDevicePromptContainsThePhraseAsData() = runTest {
        val model = CountingModel(ACCEPTED)
        engines(onDevice = { model }).read("lunch 12")

        assertTrue(model.prompts.single().contains("lunch 12"))
        assertTrue(model.prompts.single().contains("PHRASE_DATA_BEGIN"))
    }

    @Test
    fun cancellationFromOnDevicePropagates() = runTest {
        val engines = engines(onDevice = { CountingModel { throw CancellationException("stop") } })

        assertFailsWith<CancellationException> { engines.read("lunch 12") }
    }

    @Test
    fun byokConnectionFailurePropagates() = runTest {
        val engines = engines(byok = { CountingModel { throw NoConnectionException() } })

        assertFailsWith<NoConnectionException> { engines.read("lunch 12") }
    }

    @Test
    fun byokServerFailurePropagates() = runTest {
        val engines = engines(byok = { CountingModel { throw ServerStatusException(503) } })

        assertFailsWith<ServerStatusException> { engines.read("lunch 12") }
    }

    @Test
    fun theSameRulePhraseMatchesTheRules() = runTest {
        val phrase = "Coffee 4.50 USD"
        val ruled = assertIs<PhraseRead.Draft>(rules.read(phrase)).draft
        val parsed = assertIs<PhraseResult.Draft>(engines().read(phrase)).draft

        assertEquals(ruled, parsed)
    }

    private fun engines(
        onDevice: suspend () -> PhraseModel? = { null },
        byok: suspend () -> PhraseModel? = { null },
    ) = PhraseEngines(rules, onDevice, byok)
}

private const val ACCEPTED =
    """{"merchant":"Model Shop","amountMinor":999,"currencyCode":"GBP","occurredOn":"2026-03-04","categoryName":"Transport"}"""

private object EngineClock : PhraseClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class CountingModel(
    private val reply: suspend () -> String,
) : PhraseModel {
    val prompts = mutableListOf<String>()

    constructor(text: String) : this({ text })

    override suspend fun complete(prompt: String): String {
        prompts += prompt
        return reply()
    }
}
