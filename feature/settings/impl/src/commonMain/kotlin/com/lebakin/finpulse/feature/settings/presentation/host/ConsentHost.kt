package com.lebakin.finpulse.feature.settings.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.settings.presentation.view.ConsentScreen
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.ConsentScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConsentHost() {
    val viewModel = koinViewModel<ConsentScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConsentScreen(state = state, onAction = viewModel::onAction)
}
