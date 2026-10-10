package com.lebakin.finpulse.feature.onboarding.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeContent
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {
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
        val model = HomeScreenViewModel(
            showcase = showcase,
            ledgerReader = EmptyLedgerReader(),
            ledgerWriter = writer,
            entry = RecordingOnboardingEntry(),
        )
        val before = assertIs<Loadable.Ready<HomeContent>>(model.state.value.content)
        assertEquals(HomeContent.Showcase, before.value)
        model.onAction(HomeAction.ClearSample)
        assertEquals(listOf("clear"), writer.calls)
        val after = assertIs<Loadable.Ready<HomeContent>>(model.state.value.content)
        assertEquals(HomeContent.Empty, after.value)
        assertEquals("The sample was removed.", model.state.value.message?.text)
    }

    @Test
    fun showAgainCallsTheEntry() {
        val entry = RecordingOnboardingEntry()
        val model = HomeScreenViewModel(
            showcase = FakeShowcasePreferences(false),
            ledgerReader = EmptyLedgerReader(),
            ledgerWriter = RecordingLedgerWriter(FakeShowcasePreferences(false)),
            entry = entry,
        )
        val ready = assertIs<Loadable.Ready<HomeContent>>(model.state.value.content)
        assertEquals(HomeContent.Empty, ready.value)
        model.onAction(HomeAction.ShowOnboardingAgain)
        assertEquals(1, entry.calls)
    }

    @Test
    fun userRowsAreReady() {
        assertEquals(HomeContent.Ready, homeContent(showcaseOn = false, ledgerEmpty = false))
    }
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

private class RecordingOnboardingEntry : OnboardingEntry {
    var calls: Int = 0

    override suspend fun showAgain() {
        calls += 1
    }
}
