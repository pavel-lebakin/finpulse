package com.lebakin.finpulse.core.uikit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FinPulseTokens {
    val seed: Color = Color(0xFF3C4F9A)
    val baseRadius: Dp = 12.dp
}

class FinPulseSpacing(
    val multiplier: Float = 1f,
) {
    val step: Dp = (4f * multiplier).dp
    val s: Dp = step * 2
    val m: Dp = step * 4
    val l: Dp = step * 6
}

class FinPulseRadii(
    val base: Dp,
) {
    val small: Dp = base / 2
    val medium: Dp = base
    val large: Dp = base * 2
}
