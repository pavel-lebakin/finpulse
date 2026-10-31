package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.uikit.FinPulseIcons
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing

private val segmentLabels = listOf("Day", "Week", "Month")

@Composable
internal fun CatalogButtons() {
    Text(text = "Buttons", style = MaterialTheme.typography.titleMedium)
    Button(onClick = {}) { Text(text = "Filled") }
    FilledTonalButton(onClick = {}) { Text(text = "Tonal") }
    ElevatedButton(onClick = {}) { Text(text = "Elevated") }
    OutlinedButton(onClick = {}) { Text(text = "Outlined") }
    TextButton(onClick = {}) { Text(text = "Text") }
    Button(onClick = {}, enabled = false) { Text(text = "Disabled") }
    IconButtons()
    FloatingButtons()
    SegmentChoices()
}

@Composable
private fun IconButtons() {
    val space = LocalFinPulseSpacing.current
    Text(text = "Icon buttons", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(space.s)) {
        IconButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Scan") }
        FilledIconButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Scan filled") }
        FilledTonalIconButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Scan tonal") }
        OutlinedIconButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Scan outlined") }
    }
}

@Composable
private fun FloatingButtons() {
    val space = LocalFinPulseSpacing.current
    Text(text = "Floating action buttons", style = MaterialTheme.typography.titleMedium)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space.m),
        verticalArrangement = Arrangement.spacedBy(space.m),
    ) {
        SmallFloatingActionButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Small") }
        FloatingActionButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Default") }
        LargeFloatingActionButton(onClick = {}) { Icon(FinPulseIcons.Scan, contentDescription = "Large") }
        ExtendedFloatingActionButton(
            onClick = {},
            icon = { Icon(FinPulseIcons.Scan, contentDescription = null) },
            text = { Text(text = "Create") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentChoices() {
    var single by rememberSaveable { mutableIntStateOf(0) }
    var week by rememberSaveable { mutableStateOf(true) }
    var month by rememberSaveable { mutableStateOf(false) }
    Text(text = "Segmented buttons", style = MaterialTheme.typography.titleMedium)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        segmentLabels.forEachIndexed { index, label ->
            SegmentedButton(
                selected = single == index,
                onClick = { single = index },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = segmentLabels.size),
            ) {
                Text(text = label)
            }
        }
    }
    MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            checked = week,
            onCheckedChange = { week = it },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(text = "Week")
        }
        SegmentedButton(
            checked = month,
            onCheckedChange = { month = it },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(text = "Month")
        }
    }
}
