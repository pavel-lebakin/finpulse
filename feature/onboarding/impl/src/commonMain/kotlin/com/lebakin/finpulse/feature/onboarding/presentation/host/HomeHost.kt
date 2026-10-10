package com.lebakin.finpulse.feature.onboarding.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.onboarding.presentation.view.HomeScreen
import com.lebakin.finpulse.feature.onboarding.presentation.viewmodel.HomeScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeHost() {
    val viewModel = koinViewModel<HomeScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onAction = viewModel::onAction)
}
