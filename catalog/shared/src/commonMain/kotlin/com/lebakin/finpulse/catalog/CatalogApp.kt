package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lebakin.finpulse.core.uikit.AmountBars
import com.lebakin.finpulse.core.uikit.EmptyState
import com.lebakin.finpulse.core.uikit.FinPulseTheme
import com.lebakin.finpulse.core.uikit.MoneyAmount
import com.lebakin.finpulse.core.uikit.NoticeBanner
import com.lebakin.finpulse.core.uikit.TransactionRow

private val seeds = listOf(
    Color(0xFF1F7A4D),
    Color(0xFF1D4E89),
    Color(0xFFB85C38),
)

private fun seedName(index: Int): String = when (index) {
    0 -> "Green"
    1 -> "Blue"
    else -> "Rust"
}

@Composable
fun CatalogApp() {
    var seedIndex by rememberSaveable { mutableIntStateOf(0) }
    val seed = seeds[seedIndex]
    var dark by rememberSaveable { mutableStateOf(false) }
    var radius by rememberSaveable { mutableFloatStateOf(12f) }
    var spacing by rememberSaveable { mutableFloatStateOf(1f) }
    var fontScale by rememberSaveable { mutableFloatStateOf(1f) }
    var bannerClicks by rememberSaveable { mutableStateOf(0) }
    FinPulseTheme(
        seed = seed,
        dark = dark,
        baseRadius = radius.dp,
        spacingMultiplier = spacing,
        fontScale = fontScale,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Catalog", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Color")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                seeds.forEachIndexed { index, _ ->
                    Button(onClick = { seedIndex = index }) {
                        Text(text = seedName(index))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Dark theme", modifier = Modifier.weight(1f))
                Switch(checked = dark, onCheckedChange = { dark = it })
            }
            Text(text = "Radius ${radius.toInt()} dp")
            Slider(value = radius, onValueChange = { radius = it }, valueRange = 4f..28f)
            Text(text = "Spacing ${spacing}")
            Slider(value = spacing, onValueChange = { spacing = it }, valueRange = 0.5f..2f)
            Text(text = "Font ${fontScale}")
            Slider(value = fontScale, onValueChange = { fontScale = it }, valueRange = 0.8f..1.6f)
            MoneyAmount(amount = "1,240.50 ₽")
            TransactionRow(
                title = "Groceries",
                subtitle = "Oct 28",
                amount = "−860.00 ₽",
            )
            NoticeBanner(
                message = "This is sample data. It is removed when you add your first transaction.",
                actionLabel = "Clear now",
                onAction = { bannerClicks += 1 },
            )
            Text(text = "Taps: $bannerClicks")
            AmountBars(amounts = listOf(12f, 40f, 18f, 32f))
            Text(text = "Empty chart")
            AmountBars(amounts = emptyList(), modifier = Modifier.fillMaxWidth())
            EmptyState(
                title = "Nothing here yet",
                body = "Add your first transaction, or look at a sample.",
            )
        }
    }
}
