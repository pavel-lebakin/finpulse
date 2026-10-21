package com.lebakin.finpulse.feature.settings.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesContent
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
internal fun LicensesScreen(
    state: LicensesState,
    onAction: (LicensesAction) -> Unit,
) {
    when (val content = state.content) {
        is Loadable.Ready -> LicensesBody(content = content.value, state = state, onAction = onAction)
        Loadable.Loading -> PendingColumn(state = state, onAction = onAction) {
            Text(text = "Loading")
        }
        is Loadable.Failed -> PendingColumn(state = state, onAction = onAction) {
            FailedContent(message = content.error.toUserMessage().text, onAction = onAction)
        }
    }
}

@Composable
private fun LicensesBody(
    content: LicensesContent,
    state: LicensesState,
    onAction: (LicensesAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    if (content.libraries.libraries.isEmpty()) {
        PendingColumn(state = state, onAction = onAction) {
            Text(text = EMPTY, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LibrariesContainer(
        libraries = content.libraries,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.l),
        header = {
            item {
                Text(
                    text = TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = spacing.m),
                )
            }
        },
        footer = {
            item {
                NoticeFooter(state = state, onAction = onAction)
            }
        },
    )
}

@Composable
private fun PendingColumn(
    state: LicensesState,
    onAction: (LicensesAction) -> Unit,
    body: @Composable () -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = TITLE, style = MaterialTheme.typography.headlineSmall)
        body()
        NoticeFooter(state = state, onAction = onAction)
    }
}

@Composable
private fun FailedContent(
    message: String,
    onAction: (LicensesAction) -> Unit,
) {
    Text(text = message)
    Button(onClick = { onAction(LicensesAction.Retry) }) {
        Text(text = "Try again")
    }
}

@Composable
private fun NoticeFooter(
    state: LicensesState,
    onAction: (LicensesAction) -> Unit,
) {
    MessageText(message = state.message, onDismiss = { onAction(LicensesAction.DismissMessage) })
    if (state.sheet != null) {
        TextButton(onClick = { onAction(LicensesAction.DismissSheet) }) {
            Text(text = "Close")
        }
    }
}

private const val TITLE = "Open source licenses"

private const val EMPTY = "No open source notices were found."
