package com.lebakin.finpulse.feature.settings.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.settings.presentation.view.SettingsScreen
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.SettingsScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsHost() {
    val viewModel = koinViewModel<SettingsScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(state = state, onAction = viewModel::onAction)
}
