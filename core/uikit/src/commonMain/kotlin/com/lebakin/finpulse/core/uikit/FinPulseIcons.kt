package com.lebakin.finpulse.core.uikit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

object FinPulseIcons {
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack

    val Menu: ImageVector = Icons.Filled.Menu

    val Home: ImageVector = Icons.Filled.Home

    val Private: ImageVector = Icons.Filled.Lock

    val Scan: ImageVector = Icons.Filled.Receipt

    val Ask: ImageVector = Icons.AutoMirrored.Filled.Chat

    val Sample: ImageVector = Icons.Filled.BarChart

    val Settings: ImageVector = Icons.Filled.Settings
}

@Composable
fun BackArrow(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onBack, modifier = modifier) {
        Icon(imageVector = FinPulseIcons.Back, contentDescription = "Back")
    }
}

@Composable
fun MenuButton(
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onMenu, modifier = modifier) {
        Icon(imageVector = FinPulseIcons.Menu, contentDescription = "Menu")
    }
}
