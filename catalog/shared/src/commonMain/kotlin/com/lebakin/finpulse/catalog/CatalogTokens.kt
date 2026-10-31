package com.lebakin.finpulse.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lebakin.finpulse.core.uikit.AmountBars
import com.lebakin.finpulse.core.uikit.BackArrow
import com.lebakin.finpulse.core.uikit.EmptyState
import com.lebakin.finpulse.core.uikit.FinPulsePalettes
import com.lebakin.finpulse.core.uikit.LocalFinPulseRadii
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.MenuButton
import com.lebakin.finpulse.core.uikit.MoneyAmount
import com.lebakin.finpulse.core.uikit.NoticeBanner
import com.lebakin.finpulse.core.uikit.TransactionRow

private val sampleAmounts = listOf(12f, 40f, 18f, 32f)

@Composable
internal fun CatalogTokens(
    paletteIndex: Int,
    onPalette: (Int) -> Unit,
    dark: Boolean,
    onDark: (Boolean) -> Unit,
    radius: Float,
    onRadius: (Float) -> Unit,
    spacing: Float,
    onSpacing: (Float) -> Unit,
    fontScale: Float,
    onFontScale: (Float) -> Unit,
    radiusRange: ClosedFloatingPointRange<Float>,
    spacingRange: ClosedFloatingPointRange<Float>,
    fontRange: ClosedFloatingPointRange<Float>,
) {
    var bannerClicks by rememberSaveable { mutableIntStateOf(0) }
    Text(text = "Palette", style = MaterialTheme.typography.titleMedium)
    PalettePicker(selected = paletteIndex, onSelect = onPalette)
    PaletteSwatches()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Dark theme", modifier = Modifier.weight(1f))
        Switch(checked = dark, onCheckedChange = onDark)
    }
    Text(text = "Radius ${radius.toInt()} dp")
    Slider(value = radius, onValueChange = onRadius, valueRange = radiusRange)
    Text(text = "Spacing $spacing")
    Slider(value = spacing, onValueChange = onSpacing, valueRange = spacingRange)
    Text(text = "Font $fontScale")
    Slider(value = fontScale, onValueChange = onFontScale, valueRange = fontRange)
    ProductSamples(bannerClicks = bannerClicks, onClearSample = { bannerClicks += 1 })
}

@Composable
private fun PalettePicker(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val space = LocalFinPulseSpacing.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space.s),
        verticalArrangement = Arrangement.spacedBy(space.s),
    ) {
        FinPulsePalettes.all.forEachIndexed { index, palette ->
            FilterChip(
                selected = index == selected,
                onClick = { onSelect(index) },
                label = { Text(text = palette.name) },
                leadingIcon = { SeedDot(color = palette.seed) },
            )
        }
    }
}

@Composable
private fun SeedDot(color: Color) {
    val space = LocalFinPulseSpacing.current
    Box(
        modifier = Modifier
            .size(space.m)
            .background(color, CircleShape),
    )
}

@Composable
private fun PaletteSwatches() {
    val scheme = MaterialTheme.colorScheme
    val space = LocalFinPulseSpacing.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space.m),
        verticalArrangement = Arrangement.spacedBy(space.s),
    ) {
        Swatch(name = "Primary", color = scheme.primary)
        Swatch(name = "Secondary", color = scheme.secondary)
        Swatch(name = "Tertiary", color = scheme.tertiary)
        Swatch(name = "Surface", color = scheme.surface)
        Swatch(name = "Background", color = scheme.background)
    }
}

@Composable
private fun Swatch(
    name: String,
    color: Color,
) {
    val space = LocalFinPulseSpacing.current
    val radii = LocalFinPulseRadii.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(space.l * 2)
                .background(color, RoundedCornerShape(radii.small))
                .border(
                    width = space.step / 4,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(radii.small),
                ),
        )
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProductSamples(
    bannerClicks: Int,
    onClearSample: () -> Unit,
) {
    MoneyAmount(amount = "1,240.50 ₽")
    TransactionRow(
        title = "Groceries",
        subtitle = "Oct 28",
        amount = "−860.00 ₽",
    )
    NoticeBanner(
        message = "This is sample data. It is removed when you add your first transaction.",
        actionLabel = "Clear now",
        onAction = onClearSample,
    )
    Text(text = "Taps: $bannerClicks")
    AmountBars(amounts = sampleAmounts)
    Text(text = "Empty chart")
    AmountBars(amounts = emptyList(), modifier = Modifier.fillMaxWidth())
    EmptyState(
        title = "Nothing here yet",
        body = "Add your first transaction, or look at a sample.",
    )
    BackArrow(onBack = {})
    MenuButton(onMenu = {})
}
