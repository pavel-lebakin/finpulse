package com.lebakin.finpulse.feature.insights.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.insights.domain.InsightsClock
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsNavigator
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardAction
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardContent
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clearRemovesTheSampleAndShowsTheMessage() {
        val showcase = FakeShowcasePreferences(true)
        val writer = RecordingLedgerWriter(showcase)
        val model = DashboardScreenViewModel(
            showcase = showcase,
            ledgerReader = EmptyLedgerReader(),
            ledgerWriter = writer,
            clock = FixedClock,
            navigator = SilentNavigator,
        )
        val before = assertIs<Loadable.Ready<DashboardContent>>(model.state.value.content)
        assertIs<DashboardContent.Ready>(before.value)
        model.onAction(DashboardAction.ClearSample)
        assertEquals(listOf("clear"), writer.calls)
        val after = assertIs<Loadable.Ready<DashboardContent>>(model.state.value.content)
        assertEquals(DashboardContent.Empty, after.value)
        assertEquals("The sample was removed.", model.state.value.message?.text)
    }

    @Test
    fun userExpenseIsReady() {
        val content = dashboardContent(
            showcaseOn = false,
            transactions = listOf(
                Transaction(
                    id = "cafe",
                    categoryId = "eating-out",
                    amountMinor = 18_645,
                    currencyCode = "RUB",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Cafe",
                    occurredOn = LocalDate(2026, 10, 1),
                    origin = RecordOrigin.USER,
                ),
            ),
            categories = listOf(Category(id = "eating-out", name = "Eating out", origin = RecordOrigin.USER)),
            today = LocalDate(2026, 10, 29),
        )
        val ready = assertIs<DashboardContent.Ready>(content)
        assertEquals(false, ready.showcase)
        assertEquals("−186.45 RUB", ready.spending.monthExpense)
        assertEquals(listOf("Aug", "Sep", "Oct"), ready.spending.months.map { it.label })
    }
}

private object SilentNavigator : InsightsNavigator {
    override fun openDashboard() = Unit

    override fun openReceipt(transactionId: String) = Unit

    override fun back() = Unit
}

private object FixedClock : InsightsClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class FakeShowcasePreferences(
    enabled: Boolean,
) : ShowcasePreferences {
    private val mode = MutableStateFlow(enabled)

    override fun observeShowcaseMode(): Flow<Boolean> = mode

    override suspend fun setShowcaseMode(enabled: Boolean) {
        mode.value = enabled
    }
}

private class EmptyLedgerReader : LedgerReader {
    override fun observeTransactions(): Flow<List<Transaction>> = MutableStateFlow(emptyList())

    override fun observeCategories(): Flow<List<Category>> = MutableStateFlow(emptyList())
}

private class RecordingLedgerWriter(
    private val showcase: ShowcasePreferences,
) : LedgerWriter {
    val calls = mutableListOf<String>()

    override suspend fun seedShowcase() {
        calls += "seed"
    }

    override suspend fun clearShowcase() {
        calls += "clear"
        showcase.setShowcaseMode(false)
    }

    override suspend fun addUserTransaction(category: Category, transaction: Transaction): Boolean = false
}
