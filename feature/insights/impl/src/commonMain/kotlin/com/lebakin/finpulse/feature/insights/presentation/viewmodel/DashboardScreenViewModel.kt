package com.lebakin.finpulse.feature.insights.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.insights.domain.InsightsClock
import com.lebakin.finpulse.feature.insights.domain.spendingOf
import com.lebakin.finpulse.feature.insights.presentation.mapper.toUiModel
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsNavigator
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardAction
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardContent
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardReducer
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardState
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.UserMessage
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.yield
import kotlinx.datetime.LocalDate

internal class DashboardScreenViewModel(
    private val showcase: ShowcasePreferences,
    private val ledgerReader: LedgerReader,
    private val ledgerWriter: LedgerWriter,
    private val clock: InsightsClock,
    private val navigator: InsightsNavigator,
) : ScreenViewModel<DashboardState, DashboardAction>() {
    init {
        observe()
    }

    override fun createInitialState(): DashboardState = DashboardReducer.initial()

    override fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.ClearSample -> clearSample()
            DashboardAction.Retry -> observe()
            DashboardAction.DismissMessage -> reduce { DashboardReducer.run { withMessage(null) } }
            DashboardAction.DismissSheet -> reduce { DashboardReducer.run { withSheet(null) } }
            is DashboardAction.OpenReceipt -> navigator.openReceipt(action.transactionId)
        }
    }

    private fun observe() {
        launchCatching(onError = { error -> reduce { DashboardReducer.run { withError(error) } } }) {
            yield()
            combine(
                showcase.observeShowcaseMode(),
                ledgerReader.observeTransactions(),
                ledgerReader.observeCategories(),
            ) { showcaseOn, transactions, categories ->
                dashboardContent(showcaseOn, transactions, categories, clock.today())
            }.collect { content ->
                reduce { DashboardReducer.run { withContent(content) } }
            }
        }
    }

    private fun clearSample() {
        launchCatching(onError = { error ->
            reduce { DashboardReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            ledgerWriter.clearShowcase()
            reduce { DashboardReducer.run { withMessage(UserMessage(SAMPLE_REMOVED)) } }
        }
    }

    private companion object {
        const val SAMPLE_REMOVED = "The sample was removed."
    }
}

internal fun dashboardContent(
    showcaseOn: Boolean,
    transactions: List<Transaction>,
    categories: List<Category>,
    today: LocalDate,
): DashboardContent {
    if (!showcaseOn && transactions.isEmpty()) {
        return DashboardContent.Empty
    }
    return DashboardContent.Ready(
        showcase = showcaseOn,
        spending = spendingOf(transactions, categories, today).toUiModel(),
    )
}
