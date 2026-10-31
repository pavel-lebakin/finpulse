package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
internal fun CatalogFields() {
    var note by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var secret by rememberSaveable { mutableStateOf("") }
    Text(text = "Text fields", style = MaterialTheme.typography.titleMedium)
    TextField(
        value = note,
        onValueChange = { note = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Note") },
        supportingText = { Text(text = "Filled field") },
        singleLine = true,
    )
    OutlinedTextField(
        value = amount,
        onValueChange = { amount = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Amount") },
        prefix = { Text(text = "₽") },
        isError = amount.isNotEmpty() && amount.toDoubleOrNull() == null,
        supportingText = { Text(text = "Numbers only") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
    OutlinedTextField(
        value = secret,
        onValueChange = { secret = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Secret") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
    )
    OutlinedTextField(
        value = "Read only",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        label = { Text(text = "Read only") },
    )
    TextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        label = { Text(text = "Disabled") },
    )
}
