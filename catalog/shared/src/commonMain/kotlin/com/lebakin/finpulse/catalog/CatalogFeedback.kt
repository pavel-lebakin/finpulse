package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

private const val PROGRESS = 0.4f

@Composable
internal fun CatalogFeedback() {
    ProgressSamples()
    SnackbarSample()
    DialogSample()
    SheetSample()
    RefreshSample()
}

@Composable
private fun ProgressSamples() {
    var progress by rememberSaveable { mutableFloatStateOf(PROGRESS) }
    Text(text = "Progress", style = MaterialTheme.typography.titleMedium)
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    CircularProgressIndicator()
    CircularProgressIndicator(progress = { progress })
    Button(onClick = { progress = if (progress > PROGRESS) PROGRESS else 1f }) {
        Text(text = "Toggle progress")
    }
}

@Composable
private fun SnackbarSample() {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Text(text = "Snackbar", style = MaterialTheme.typography.titleMedium)
    Button(onClick = { scope.launch { host.showSnackbar("Saved") } }) {
        Text(text = "Show snackbar")
    }
    SnackbarHost(hostState = host)
}

@Composable
private fun DialogSample() {
    var open by rememberSaveable { mutableStateOf(false) }
    Text(text = "Dialog", style = MaterialTheme.typography.titleMedium)
    Button(onClick = { open = true }) { Text(text = "Show dialog") }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(text = "Cancel") }
            },
            title = { Text(text = "Delete this receipt?") },
            text = { Text(text = "This cannot be undone.") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetSample() {
    var open by rememberSaveable { mutableStateOf(false) }
    Text(text = "Bottom sheet", style = MaterialTheme.typography.titleMedium)
    Button(onClick = { open = true }) { Text(text = "Show sheet") }
    if (open) {
        ModalBottomSheet(onDismissRequest = { open = false }) {
            Text(text = "A sheet covers the page until you dismiss it.")
            TextButton(onClick = { open = false }) { Text(text = "Close") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshSample() {
    var refreshing by rememberSaveable { mutableStateOf(false) }
    Text(text = "Pull to refresh", style = MaterialTheme.typography.titleMedium)
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { refreshing = false },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(onClick = { refreshing = !refreshing }) { Text(text = "Toggle refresh") }
    }
}
