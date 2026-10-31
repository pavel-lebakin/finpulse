package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.uikit.FinPulseIcons

private val menuItems = listOf("Edit", "Share", "Delete")

private val accounts = listOf("Cash", "Card")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogBars() {
    Text(text = "Top app bars", style = MaterialTheme.typography.titleMedium)
    TopAppBar(title = { Text(text = "Small") })
    CenterAlignedTopAppBar(title = { Text(text = "Centered") })
    MediumTopAppBar(title = { Text(text = "Medium") })
    MenuSample()
    ExposedSample()
    DateSample()
    TimeSample()
    TooltipSample()
}

@Composable
private fun MenuSample() {
    var open by rememberSaveable { mutableStateOf(false) }
    Text(text = "Menu", style = MaterialTheme.typography.titleMedium)
    IconButton(onClick = { open = true }) {
        Icon(imageVector = FinPulseIcons.Menu, contentDescription = "More")
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        menuItems.forEach { label ->
            DropdownMenuItem(
                text = { Text(text = label) },
                onClick = { open = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedSample() {
    var open by rememberSaveable { mutableStateOf(false) }
    var account by rememberSaveable { mutableStateOf(accounts.first()) }
    Text(text = "Exposed menu", style = MaterialTheme.typography.titleMedium)
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = account,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            accounts.forEach { label ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        account = label
                        open = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSample() {
    var open by rememberSaveable { mutableStateOf(false) }
    val state = rememberDatePickerState()
    Text(text = "Date", style = MaterialTheme.typography.titleMedium)
    TextButton(onClick = { open = true }) { Text(text = "Pick a date") }
    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text(text = "OK") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSample() {
    val state = rememberTimePickerState()
    Text(text = "Time", style = MaterialTheme.typography.titleMedium)
    TimePicker(state = state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TooltipSample() {
    Text(text = "Tooltip", style = MaterialTheme.typography.titleMedium)
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = "More about this") } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = {}) {
            Icon(imageVector = FinPulseIcons.Menu, contentDescription = "More")
        }
    }
}
