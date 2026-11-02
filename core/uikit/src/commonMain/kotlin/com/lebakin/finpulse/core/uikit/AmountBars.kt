package com.lebakin.finpulse.core.uikit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun AmountBars(
    amounts: List<Float>,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.primaryContainer
    val fill = MaterialTheme.colorScheme.primary
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
        val cap = min(12.dp.toPx(), min(barWidth / 2f, size.height / 2f))
        amounts.forEachIndexed { index, amount ->
            val left = index * (barWidth + gap)
            val barHeight = size.height * (amount / maxAmount)
            val valueCap = min(cap, barHeight / 2f)
            drawRoundRect(
                color = track,
                topLeft = Offset(x = left, y = 0f),
                size = Size(width = barWidth, height = size.height),
                cornerRadius = CornerRadius(cap, cap),
            )
            drawRoundRect(
                color = fill,
                topLeft = Offset(x = left, y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = CornerRadius(valueCap, valueCap),
            )
        }
    }
}
