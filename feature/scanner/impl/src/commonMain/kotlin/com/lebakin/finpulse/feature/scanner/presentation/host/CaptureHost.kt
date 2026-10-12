package com.lebakin.finpulse.feature.scanner.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.scanner.presentation.view.CaptureScreen
import com.lebakin.finpulse.feature.scanner.presentation.viewmodel.CaptureScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CaptureHost() {
    val viewModel = koinViewModel<CaptureScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CaptureScreen(state = state, onAction = viewModel::onAction)
}
