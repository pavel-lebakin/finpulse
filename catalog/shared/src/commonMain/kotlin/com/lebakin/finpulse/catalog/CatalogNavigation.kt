package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebakin.finpulse.core.uikit.BottomNavBar
import com.lebakin.finpulse.core.uikit.BottomNavItem
import com.lebakin.finpulse.core.uikit.FinPulseIcons

private val railHeight = 240.dp

private val tabLabels = listOf("Day", "Week", "Month", "Year")

@Composable
internal fun CatalogNavigation() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var primary by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val scheme = MaterialTheme.colorScheme
    Text(text = "Bottom navigation", style = MaterialTheme.typography.titleMedium)
    RowSwitch(label = "Primary container", checked = primary, onChecked = { primary = it })
    BottomNavBar(
        items = listOf(
            BottomNavItem(label = "Home", icon = FinPulseIcons.Home, selected = selected == 0),
            BottomNavItem(label = "Scan", icon = FinPulseIcons.Scan, selected = selected == 1),
            BottomNavItem(label = "Ask", icon = FinPulseIcons.Ask, selected = selected == 2),
            BottomNavItem(label = "Settings", icon = FinPulseIcons.Settings, selected = selected == 3),
        ),
        onSelect = { selected = it },
        containerColor = if (primary) scheme.primaryContainer else scheme.surfaceContainer,
        selectedColor = if (primary) scheme.onPrimaryContainer else scheme.onSecondaryContainer,
        unselectedColor = scheme.onSurfaceVariant,
        indicatorColor = if (primary) scheme.primary else scheme.secondaryContainer,
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    )
    Text(text = "Navigation rail", style = MaterialTheme.typography.titleMedium)
    NavigationRail(modifier = Modifier.height(railHeight)) {
        listOf("Home", "Scan").forEachIndexed { index, label ->
            NavigationRailItem(
                selected = selected == index,
                onClick = { selected = index },
                icon = { Text(text = label.take(1)) },
                label = { Text(text = label) },
            )
        }
    }
    Text(text = "Tabs", style = MaterialTheme.typography.titleMedium)
    CatalogTabs(selected = tab, onSelect = { tab = it })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTabs(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    PrimaryTabRow(selectedTabIndex = selected, modifier = Modifier.fillMaxWidth()) {
        tabLabels.forEachIndexed { index, label ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(text = label) },
            )
        }
    }
    SecondaryTabRow(selectedTabIndex = selected, modifier = Modifier.fillMaxWidth()) {
        tabLabels.forEachIndexed { index, label ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(text = label) },
            )
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
