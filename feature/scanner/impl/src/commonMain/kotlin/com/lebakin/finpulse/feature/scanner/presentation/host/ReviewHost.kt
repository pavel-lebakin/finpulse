package com.lebakin.finpulse.feature.scanner.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.scanner.presentation.view.ReviewScreen
import com.lebakin.finpulse.feature.scanner.presentation.viewmodel.ReviewScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ReviewHost() {
    val viewModel = koinViewModel<ReviewScreenViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReviewScreen(state = state, onAction = viewModel::onAction)
}
