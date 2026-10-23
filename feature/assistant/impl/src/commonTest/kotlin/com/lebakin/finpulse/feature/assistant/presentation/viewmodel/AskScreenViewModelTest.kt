package com.lebakin.finpulse.feature.assistant.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.assistant.domain.PhraseClock
import com.lebakin.finpulse.feature.assistant.domain.PhraseEngines
import com.lebakin.finpulse.feature.assistant.domain.PhraseRules
import com.lebakin.finpulse.feature.assistant.domain.PhraseSession
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskAction
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigator
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AskScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aSpendPhraseOpensConfirmAndDoesNotReadTheLedger() {
        val session = PhraseSession()
        val navigator = RecordingNavigator()
        val reader = CountingReader(emptyList())
        val model = model(session, navigator, reader)

        model.onAction(AskAction.Send("Coffee 4.50 USD"))

        assertEquals(listOf("confirm"), navigator.calls)
        assertEquals("Coffee", session.current()?.merchant)
        assertEquals(0, reader.reads)
        val ready = assertIs<Loadable.Ready<com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskContent>>(
            model.state.value.content,
        )
        assertEquals(false, ready.value.sending)
    }

    @Test
    fun aMonthQuestionAnswersFromTheLedger() {
        val reader = CountingReader(
            listOf(
                Transaction(
                    id = "cafe",
                    categoryId = "eating-out",
                    amountMinor = 450,
                    currencyCode = "USD",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Coffee",
                    occurredOn = LocalDate(2026, 10, 2),
                    origin = RecordOrigin.USER,
                ),
            ),
        )
        val model = model(PhraseSession(), RecordingNavigator(), reader)

        model.onAction(AskAction.Send("How much did I spend this month?"))

        val ready = assertIs<Loadable.Ready<com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskContent>>(
            model.state.value.content,
        )
        assertEquals("You spent 4.50 USD this month.", ready.value.answer)
        assertEquals(1, reader.reads)
        assertNull(model.state.value.message)
    }

    @Test
    fun anUnknownQuestionDoesNotReadTheLedger() {
        val reader = CountingReader(emptyList())
        val model = model(PhraseSession(), RecordingNavigator(), reader)

        model.onAction(AskAction.Send("Why is the bus late?"))

        val ready = assertIs<Loadable.Ready<com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskContent>>(
            model.state.value.content,
        )
        assertEquals("I can tell you how much you spent this month.", ready.value.answer)
        assertEquals(0, reader.reads)
    }

    @Test
    fun anUnreadablePhraseShowsTheMessage() {
        val model = model(PhraseSession(), RecordingNavigator(), CountingReader(emptyList()))

        model.onAction(AskAction.Send("hello"))

        assertEquals("That phrase could not be read.", model.state.value.message?.text)
    }
}

private fun model(
    session: PhraseSession,
    navigator: AssistantNavigator,
    reader: LedgerReader,
) = AskScreenViewModel(
    engines = PhraseEngines(
        rules = PhraseRules(AskClock),
        onDevice = { null },
        byok = { null },
    ),
    ledgerReader = reader,
    clock = AskClock,
    session = session,
    navigator = navigator,
)

private object AskClock : PhraseClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class CountingReader(
    private val transactions: List<Transaction>,
) : LedgerReader {
    var reads: Int = 0

    override fun observeTransactions(): Flow<List<Transaction>> {
        reads += 1
        return MutableStateFlow(transactions)
    }

    override fun observeCategories(): Flow<List<Category>> = MutableStateFlow(emptyList())
}

private class RecordingNavigator : AssistantNavigator {
    val calls = mutableListOf<String>()

    override fun openAsk() {
        calls += "ask"
    }

    override fun openConfirm() {
        calls += "confirm"
    }

    override fun back() {
        calls += "back"
    }
}
