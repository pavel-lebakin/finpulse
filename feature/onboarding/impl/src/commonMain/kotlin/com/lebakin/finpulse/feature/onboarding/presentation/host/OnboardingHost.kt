package com.lebakin.finpulse.feature.onboarding.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.onboarding.presentation.view.OnboardingScreen
import com.lebakin.finpulse.feature.onboarding.presentation.viewmodel.OnboardingScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingHost() {
    val viewModel = koinViewModel<OnboardingScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreen(state = state, onAction = viewModel::onAction)
}
