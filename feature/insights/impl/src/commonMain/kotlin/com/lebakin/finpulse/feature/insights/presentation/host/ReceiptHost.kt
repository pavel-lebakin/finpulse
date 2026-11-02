package com.lebakin.finpulse.feature.insights.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebakin.finpulse.feature.insights.presentation.view.ReceiptScreen
import com.lebakin.finpulse.feature.insights.presentation.viewmodel.ReceiptScreenViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ReceiptHost(transactionId: String) {
    val viewModel = koinViewModel<ReceiptScreenViewModel> { parametersOf(transactionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReceiptScreen(state = state, onAction = viewModel::onAction)
}
