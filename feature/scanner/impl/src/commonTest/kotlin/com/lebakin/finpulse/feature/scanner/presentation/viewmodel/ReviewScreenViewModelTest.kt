package com.lebakin.finpulse.feature.scanner.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.scanner.domain.ReceiptClock
import com.lebakin.finpulse.feature.scanner.domain.ReceiptIds
import com.lebakin.finpulse.feature.scanner.domain.ReceiptParser
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSamples
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSession
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewAction
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun missingDraftIsAnError() {
        val model = model(ReceiptSession(), RecordingWriter(removed = false), RecordingInsights())
        assertIs<Loadable.Failed>(model.state.value.content)
    }

    @Test
    fun confirmWritesTheUserExpense() {
        val session = ReceiptSession()
        session.store(ReceiptParser(ReviewClock).parse(ReceiptSamples.text("groceries").orEmpty()))
        val writer = RecordingWriter(removed = false)
        val insights = RecordingInsights()
        val model = model(session, writer, insights)
        assertEquals("12.50 USD", (model.state.value.content as Loadable.Ready).value.amount)
        model.onAction(ReviewAction.Confirm)
        assertEquals(1, writer.writes)
        assertEquals(MoneyDirection.EXPENSE, writer.direction)
        assertEquals(RecordOrigin.USER, writer.origin)
        assertEquals("Saved.", model.state.value.message?.text)
        assertTrue(model.state.value.saved)
        assertNull(session.current())
        model.onAction(ReviewAction.Done)
        assertEquals(1, insights.opens)
    }

    @Test
    fun confirmTellsTheUserWhenTheSampleWasRemoved() {
        val session = ReceiptSession()
        session.store(ReceiptParser(ReviewClock).parse(ReceiptSamples.text("cafe").orEmpty()))
        val model = model(session, RecordingWriter(removed = true), RecordingInsights())
        model.onAction(ReviewAction.Confirm)
        assertEquals("Saved. The sample was removed.", model.state.value.message?.text)
    }
}

private fun model(
    session: ReceiptSession,
    writer: LedgerWriter,
    insights: InsightsEntry,
) = ReviewScreenViewModel(
    session = session,
    ledgerWriter = writer,
    insights = insights,
    navigator = SilentNavigator,
    ids = SequenceIds(),
)

private object ReviewClock : ReceiptClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class SequenceIds : ReceiptIds {
    private var value = 0

    override fun next(): String {
        value += 1
        return "id-$value"
    }
}

private class RecordingWriter(
    private val removed: Boolean,
) : LedgerWriter {
    var writes: Int = 0
    var direction: MoneyDirection? = null
    var origin: RecordOrigin? = null

    override suspend fun seedShowcase() = Unit

    override suspend fun clearShowcase() = Unit

    override suspend fun addUserTransaction(category: Category, transaction: Transaction): Boolean {
        writes += 1
        direction = transaction.direction
        origin = transaction.origin
        return removed
    }
}

private class RecordingInsights : InsightsEntry {
    var opens: Int = 0

    override fun openDashboard() {
        opens += 1
    }
}

private object SilentNavigator : ScannerNavigator {
    override fun openCapture() = Unit

    override fun openReview() = Unit

    override fun back() = Unit
}
