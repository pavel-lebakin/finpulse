package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebakin.finpulse.core.uikit.FinPulseIcons
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing

private val dividerHeight = 32.dp

@Composable
internal fun CatalogSurfaces() {
    val space = LocalFinPulseSpacing.current
    Text(text = "Cards", style = MaterialTheme.typography.titleMedium)
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Filled card", modifier = Modifier.fillMaxWidth().padding(space.m))
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Elevated card", modifier = Modifier.fillMaxWidth().padding(space.m))
    }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Outlined card", modifier = Modifier.fillMaxWidth().padding(space.m))
    }
    Text(text = "Lists", style = MaterialTheme.typography.titleMedium)
    ListItem(
        headlineContent = { Text(text = "Groceries") },
        supportingContent = { Text(text = "Oct 28") },
        trailingContent = { Text(text = "−860") },
    )
    HorizontalDivider()
    ListItem(
        headlineContent = { Text(text = "Rent") },
        overlineContent = { Text(text = "Home") },
        supportingContent = { Text(text = "Monthly") },
    )
    Row(
        modifier = Modifier.height(dividerHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Left")
        VerticalDivider()
        Text(text = "Right")
    }
    Text(text = "Badge", style = MaterialTheme.typography.titleMedium)
    BadgedBox(badge = { Badge { Text(text = "3") } }) {
        Icon(imageVector = FinPulseIcons.Home, contentDescription = "Home")
    }
}
