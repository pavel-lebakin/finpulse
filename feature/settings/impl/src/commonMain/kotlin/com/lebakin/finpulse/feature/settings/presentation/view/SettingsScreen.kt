package com.lebakin.finpulse.feature.settings.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lebakin.finpulse.core.preferences.ThemeMode
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsState

@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
        ThemeChoices(selected = state.theme, onSelect = { onAction(SettingsAction.ChooseTheme(it)) })
        SettingRow(label = "Allow sending to Gemini", onClick = { onAction(SettingsAction.OpenConsent) })
        SettingRow(label = "Gemini key", onClick = { onAction(SettingsAction.OpenKey) })
        SettingRow(label = "Show onboarding again", onClick = { onAction(SettingsAction.ShowOnboardingAgain) })
        MessageText(message = state.message, onDismiss = { onAction(SettingsAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(SettingsAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun ThemeChoices(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
        verticalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = { Text(text = mode.label()) },
            )
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

@Composable
private fun SettingRow(
    label: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
