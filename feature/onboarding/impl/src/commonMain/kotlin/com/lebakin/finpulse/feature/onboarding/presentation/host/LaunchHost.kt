package com.lebakin.finpulse.feature.onboarding.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.onboarding.presentation.view.LaunchScreen
import com.lebakin.finpulse.feature.onboarding.presentation.viewmodel.LaunchScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LaunchHost() {
    val viewModel = koinViewModel<LaunchScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchScreen(state = state, onAction = viewModel::onAction)
}
