package com.lebakin.finpulse.feature.settings.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.settings.presentation.view.LicensesScreen
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.LicensesScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LicensesHost() {
    val viewModel = koinViewModel<LicensesScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LicensesScreen(state = state, onAction = viewModel::onAction)
}
