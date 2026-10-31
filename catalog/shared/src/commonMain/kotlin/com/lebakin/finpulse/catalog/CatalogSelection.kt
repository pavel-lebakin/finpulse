package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.state.ToggleableState
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing

private const val RANGE_START = 0.2f

private const val RANGE_END = 0.8f

@Composable
internal fun CatalogSelection() {
    Checks()
    Radios()
    Sliders()
    Chips()
}

@Composable
private fun Checks() {
    var checked by rememberSaveable { mutableStateOf(true) }
    var enabled by rememberSaveable { mutableStateOf(false) }
    val space = LocalFinPulseSpacing.current
    Text(text = "Check, switch", style = MaterialTheme.typography.titleMedium)
    Row(
        horizontalArrangement = Arrangement.spacedBy(space.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Text(text = "Checkbox")
        TriStateCheckbox(state = toggleState(checked, enabled), onClick = { enabled = !enabled })
        Text(text = "Tri-state")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Switch", modifier = androidx.compose.ui.Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = { enabled = it })
    }
}

private fun toggleState(checked: Boolean, enabled: Boolean): ToggleableState = when {
    checked && enabled -> ToggleableState.On
    checked || enabled -> ToggleableState.Indeterminate
    else -> ToggleableState.Off
}

@Composable
private fun Radios() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Text(text = "Radio", style = MaterialTheme.typography.titleMedium)
    listOf("Home", "Away").forEachIndexed { index, label ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected == index, onClick = { selected = index })
            Text(text = label)
        }
    }
}

@Composable
private fun Sliders() {
    var value by rememberSaveable { mutableFloatStateOf(RANGE_START) }
    var start by rememberSaveable { mutableFloatStateOf(RANGE_START) }
    var end by rememberSaveable { mutableFloatStateOf(RANGE_END) }
    Text(text = "Sliders", style = MaterialTheme.typography.titleMedium)
    Slider(value = value, onValueChange = { value = it })
    RangeSlider(
        value = start..end,
        onValueChange = { range ->
            start = range.start
            end = range.endInclusive
        },
    )
}

@Composable
private fun Chips() {
    var filter by rememberSaveable { mutableStateOf(true) }
    var input by rememberSaveable { mutableStateOf(true) }
    val space = LocalFinPulseSpacing.current
    Text(text = "Chips", style = MaterialTheme.typography.titleMedium)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space.s),
        verticalArrangement = Arrangement.spacedBy(space.s),
    ) {
        AssistChip(onClick = {}, label = { Text(text = "Assist") })
        ElevatedAssistChip(onClick = {}, label = { Text(text = "Elevated assist") })
        FilterChip(selected = filter, onClick = { filter = !filter }, label = { Text(text = "Filter") })
        ElevatedFilterChip(selected = filter, onClick = { filter = !filter }, label = { Text(text = "Elevated") })
        InputChip(selected = input, onClick = { input = !input }, label = { Text(text = "Input") })
        SuggestionChip(onClick = {}, label = { Text(text = "Suggestion") })
        ElevatedSuggestionChip(onClick = {}, label = { Text(text = "Elevated suggestion") })
    }
}
