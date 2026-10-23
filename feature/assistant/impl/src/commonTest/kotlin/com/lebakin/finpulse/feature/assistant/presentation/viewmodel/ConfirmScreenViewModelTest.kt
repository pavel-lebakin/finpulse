package com.lebakin.finpulse.feature.assistant.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.assistant.domain.PhraseClock
import com.lebakin.finpulse.feature.assistant.domain.PhraseIds
import com.lebakin.finpulse.feature.assistant.domain.PhraseRules
import com.lebakin.finpulse.feature.assistant.domain.PhraseRead
import com.lebakin.finpulse.feature.assistant.domain.PhraseSession
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmAction
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigator
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
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
class ConfirmScreenViewModelTest {
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
        val model = model(PhraseSession(), RecordingWriter(removed = false), RecordingInsights())
        assertIs<Loadable.Failed>(model.state.value.content)
    }

    @Test
    fun confirmWritesTheUserExpense() {
        val session = session()
        val writer = RecordingWriter(removed = false)
        val insights = RecordingInsights()
        val model = model(session, writer, insights)
        assertEquals("4.50 USD", (model.state.value.content as Loadable.Ready).value.amount)
        model.onAction(ConfirmAction.Confirm)
        assertEquals(1, writer.writes)
        assertEquals(MoneyDirection.EXPENSE, writer.direction)
        assertEquals(RecordOrigin.USER, writer.origin)
        assertEquals("Saved.", model.state.value.message?.text)
        assertTrue(model.state.value.saved)
        assertNull(session.current())
        model.onAction(ConfirmAction.Done)
        assertEquals(1, insights.opens)
    }

    @Test
    fun confirmTellsTheUserWhenTheSampleWasRemoved() {
        val model = model(session(), RecordingWriter(removed = true), RecordingInsights())
        model.onAction(ConfirmAction.Confirm)
        assertEquals("Saved. The sample was removed.", model.state.value.message?.text)
    }
}

private fun session(): PhraseSession {
    val session = PhraseSession()
    val draft = (PhraseRules(ConfirmClock).read("Coffee 4.50 USD") as PhraseRead.Draft).draft
    session.store(draft)
    return session
}

private fun model(
    session: PhraseSession,
    writer: LedgerWriter,
    insights: InsightsEntry,
) = ConfirmScreenViewModel(
    session = session,
    ledgerWriter = writer,
    insights = insights,
    navigator = SilentNavigator,
    ids = SequenceIds(),
)

private object ConfirmClock : PhraseClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class SequenceIds : PhraseIds {
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

private object SilentNavigator : AssistantNavigator {
    override fun openAsk() = Unit

    override fun openConfirm() = Unit

    override fun back() = Unit
}
