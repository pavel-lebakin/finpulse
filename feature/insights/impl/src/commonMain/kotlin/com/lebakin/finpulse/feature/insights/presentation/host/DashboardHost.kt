package com.lebakin.finpulse.feature.insights.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.insights.presentation.view.DashboardScreen
import com.lebakin.finpulse.feature.insights.presentation.viewmodel.DashboardScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DashboardHost() {
    val viewModel = koinViewModel<DashboardScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardScreen(state = state, onAction = viewModel::onAction)
}
