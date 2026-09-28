package com.lebakin.finpulse.core.uikit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun AmountBars(
    amounts: List<Float>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        if (amounts.isEmpty()) {
            return@Canvas
        }
        val maxAmount = amounts.max().coerceAtLeast(1f)
        val gap = 8.dp.toPx()
        val barWidth = (size.width - gap * (amounts.size - 1).coerceAtLeast(0)) / amounts.size
        amounts.forEachIndexed { index, amount ->
            val barHeight = size.height * (amount / maxAmount)
            drawRect(
                color = color,
                topLeft = Offset(x = index * (barWidth + gap), y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight),
            )
        }
    }
}
