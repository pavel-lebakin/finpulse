package com.lebakin.finpulse.feature.onboarding.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.onboarding.api.OnboardingChoice
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingReducer
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingState
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingNavigator

internal class OnboardingScreenViewModel(
    private val writer: OnboardingWriter,
    private val navigator: OnboardingNavigator,
    private val insights: InsightsEntry,
) : ScreenViewModel<OnboardingState, OnboardingAction>() {
    override fun createInitialState(): OnboardingState = OnboardingReducer.initial()

    override fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Next -> reduce { OnboardingReducer.run { withNext() } }
            OnboardingAction.Back -> reduce { OnboardingReducer.run { withBack() } }
            OnboardingAction.SeeSample -> complete(OnboardingChoice.Sample)
            OnboardingAction.StartEmpty -> complete(OnboardingChoice.Empty)
            OnboardingAction.DismissMessage -> reduce { OnboardingReducer.run { withMessage(null) } }
            OnboardingAction.DismissSheet -> reduce { OnboardingReducer.run { withSheet(null) } }
        }
    }

    private fun complete(choice: OnboardingChoice) {
        if (currentState.index != currentState.pages.lastIndex) {
            return
        }
        launchCatching(onError = { error ->
            reduce { OnboardingReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            writer.complete(choice)
            insights.openDashboard()
        }
    }
}
