package com.lebakin.finpulse.feature.assistant.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.assistant.presentation.view.ConfirmScreen
import com.lebakin.finpulse.feature.assistant.presentation.viewmodel.ConfirmScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConfirmHost() {
    val viewModel = koinViewModel<ConfirmScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConfirmScreen(state = state, onAction = viewModel::onAction)
}
