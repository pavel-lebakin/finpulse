package com.lebakin.finpulse.feature.assistant.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.assistant.presentation.view.AskScreen
import com.lebakin.finpulse.feature.assistant.presentation.viewmodel.AskScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AskHost() {
    val viewModel = koinViewModel<AskScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    AskScreen(state = state, onAction = viewModel::onAction)
}
