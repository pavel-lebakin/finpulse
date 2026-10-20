package com.lebakin.finpulse.feature.settings.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.settings.presentation.view.KeyScreen
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.KeyScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun KeyHost() {
    val viewModel = koinViewModel<KeyScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    KeyScreen(state = state, onAction = viewModel::onAction)
}
