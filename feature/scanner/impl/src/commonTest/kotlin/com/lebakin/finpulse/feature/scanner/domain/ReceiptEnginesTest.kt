package com.lebakin.finpulse.feature.scanner.domain

import com.lebakin.finpulse.core.ai.ByokFailed
import com.lebakin.finpulse.core.ai.OnDeviceFailed
import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ReceiptEnginesTest {
    private val parser = ReceiptParser(EngineClock)
    private val cafe = ReceiptSamples.text("cafe").orEmpty()

    @Test
    fun acceptedOnDeviceReplyBecomesTheDraft() = runTest {
        var byokOpened = 0
        val engines = engines(
            onDevice = { CountingModel(ACCEPTED) },
            byok = {
                byokOpened += 1
                null
            },
        )

        val draft = engines.parse(cafe)

        assertEquals("Model Shop", draft.merchant)
        assertEquals(999L, draft.amountMinor)
        assertEquals("GBP", draft.currencyCode)
        assertEquals(LocalDate(2026, 3, 4), draft.occurredOn)
        assertEquals("Transport", draft.categoryName)
        assertEquals(0, byokOpened)
    }

    @Test
    fun rejectedOnDeviceReplyUsesTheRules() = runTest {
        var byokOpened = 0
        val engines = engines(
            onDevice = { CountingModel("not json") },
            byok = {
                byokOpened += 1
                null
            },
        )

        val draft = engines.parse(cafe)

        assertEquals("North Cafe", draft.merchant)
        assertEquals(0, byokOpened)
    }

    @Test
    fun onDeviceFailureUsesTheRules() = runTest {
        val engines = engines(onDevice = { CountingModel { throw OnDeviceFailed() } })

        assertEquals("North Cafe", engines.parse(cafe).merchant)
    }

    @Test
    fun rulesWinWithoutOpeningByok() = runTest {
        var byokOpened = 0
        val engines = engines(byok = {
            byokOpened += 1
            CountingModel(ACCEPTED)
        })

        assertEquals("North Cafe", engines.parse(cafe).merchant)
        assertEquals(0, byokOpened)
    }

    @Test
    fun unreadableRulesUseAnAcceptedByokReply() = runTest {
        val engines = engines(byok = { CountingModel(ACCEPTED) })

        val draft = engines.parse("Hello")

        assertEquals("Model Shop", draft.merchant)
        assertEquals(999L, draft.amountMinor)
    }

    @Test
    fun unreadableRulesStayUnreadableWhenByokRejects() = runTest {
        val engines = engines(byok = { CountingModel("not json") })

        assertFailsWith<ReceiptUnreadable> { engines.parse("Hello") }
    }

    @Test
    fun byokFailureStaysUnreadable() = runTest {
        val engines = engines(byok = { CountingModel { throw ByokFailed() } })

        assertFailsWith<ReceiptUnreadable> { engines.parse("Hello") }
    }

    @Test
    fun missingModelsLeaveTheSampleToTheRules() = runTest {
        assertEquals(parser.parse(cafe), engines().parse(cafe))
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

        assertFailsWith<ReceiptUnreadable> { engines.parse("   ") }
        assertEquals(0, opened)
    }

    @Test
    fun onDevicePromptContainsTheReceiptAsData() = runTest {
        val model = CountingModel(ACCEPTED)
        engines(onDevice = { model }).parse(cafe)

        assertTrue(model.prompts.single().contains("North Cafe"))
    }

    @Test
    fun cancellationFromOnDevicePropagates() = runTest {
        val engines = engines(onDevice = { CountingModel { throw CancellationException("stop") } })

        assertFailsWith<CancellationException> { engines.parse(cafe) }
    }

    @Test
    fun byokConnectionFailurePropagates() = runTest {
        val engines = engines(byok = { CountingModel { throw NoConnectionException() } })

        assertFailsWith<NoConnectionException> { engines.parse("Hello") }
    }

    @Test
    fun byokServerFailurePropagates() = runTest {
        val engines = engines(byok = { CountingModel { throw ServerStatusException(503) } })

        assertFailsWith<ServerStatusException> { engines.parse("Hello") }
    }

    private fun engines(
        onDevice: suspend () -> ReceiptModel? = { null },
        byok: suspend () -> ReceiptModel? = { null },
    ) = ReceiptEngines(parser, onDevice, byok)
}

private const val ACCEPTED =
    """{"merchant":"Model Shop","amountMinor":999,"currencyCode":"GBP","occurredOn":"2026-03-04","categoryName":"Transport"}"""

private object EngineClock : ReceiptClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class CountingModel(
    private val reply: suspend () -> String,
) : ReceiptModel {
    val prompts = mutableListOf<String>()

    constructor(text: String) : this({ text })

    override suspend fun complete(prompt: String): String {
        prompts += prompt
        return reply()
    }
}
