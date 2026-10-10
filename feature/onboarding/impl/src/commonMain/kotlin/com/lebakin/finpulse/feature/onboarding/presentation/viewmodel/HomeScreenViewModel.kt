package com.lebakin.finpulse.feature.onboarding.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeContent
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeReducer
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeState
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.UserMessage
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.toUserMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.yield

internal class HomeScreenViewModel(
    private val showcase: ShowcasePreferences,
    private val ledgerReader: LedgerReader,
    private val ledgerWriter: LedgerWriter,
    private val entry: OnboardingEntry,
) : ScreenViewModel<HomeState, HomeAction>() {
    init {
        observe()
    }

    override fun createInitialState(): HomeState = HomeReducer.initial()

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.ClearSample -> clearSample()
            HomeAction.ShowOnboardingAgain -> showAgain()
            HomeAction.Retry -> observe()
            HomeAction.DismissMessage -> reduce { HomeReducer.run { withMessage(null) } }
            HomeAction.DismissSheet -> reduce { HomeReducer.run { withSheet(null) } }
        }
    }

    private fun observe() {
        launchCatching(onError = { error -> reduce { HomeReducer.run { withError(error) } } }) {
            yield()
            combine(
                showcase.observeShowcaseMode(),
                ledgerReader.observeTransactions(),
            ) { showcaseOn, transactions ->
                homeContent(showcaseOn, transactions.isEmpty())
            }.collect { content ->
                reduce { HomeReducer.run { withContent(content) } }
            }
        }
    }

    private fun clearSample() {
        launchCatching(onError = { error ->
            reduce { HomeReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            ledgerWriter.clearShowcase()
            reduce { HomeReducer.run { withMessage(UserMessage(SAMPLE_REMOVED)) } }
        }
    }

    private fun showAgain() {
        launchCatching(onError = { error ->
            reduce { HomeReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            entry.showAgain()
        }
    }

    private companion object {
        const val SAMPLE_REMOVED = "The sample was removed."
    }
}

internal fun homeContent(showcaseOn: Boolean, ledgerEmpty: Boolean): HomeContent = when {
    showcaseOn -> HomeContent.Showcase
    ledgerEmpty -> HomeContent.Empty
    else -> HomeContent.Ready
}
