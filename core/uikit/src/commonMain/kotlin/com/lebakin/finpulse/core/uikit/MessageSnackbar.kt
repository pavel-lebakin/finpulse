package com.lebakin.finpulse.core.uikit

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun MessageSnackbar(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        host.showSnackbar(message = text, duration = SnackbarDuration.Short)
        onDismiss()
    }
    SnackbarHost(hostState = host, modifier = modifier)
}
