package com.lebakin.finpulse.feature.onboarding.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.onboarding.api.OnboardingReader
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.LaunchAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.LaunchReducer
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.LaunchState
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingNavigator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield

internal class LaunchScreenViewModel(
    private val reader: OnboardingReader,
    private val navigator: OnboardingNavigator,
    private val insights: InsightsEntry,
) : ScreenViewModel<LaunchState, LaunchAction>() {
    init {
        open()
    }

    override fun createInitialState(): LaunchState = LaunchReducer.initial()

    override fun onAction(action: LaunchAction) {
        when (action) {
            LaunchAction.Retry -> open()
            LaunchAction.DismissMessage -> reduce { LaunchReducer.run { withMessage(null) } }
            LaunchAction.DismissSheet -> reduce { LaunchReducer.run { withSheet(null) } }
        }
    }

    private fun open() {
        launchCatching(onError = { error -> reduce { LaunchReducer.run { withError(error) } } }) {
            yield()
            reduce { LaunchReducer.run { withLoading() } }
            val completed = reader.observeCompleted().first()
            if (completed) {
                insights.openDashboard()
            } else {
                navigator.openFlow()
            }
            reduce { LaunchReducer.run { withReady() } }
        }
    }
}
