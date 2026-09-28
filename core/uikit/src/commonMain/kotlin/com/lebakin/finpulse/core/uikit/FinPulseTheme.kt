package com.lebakin.finpulse.core.uikit

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity

val LocalFinPulseSpacing = staticCompositionLocalOf { FinPulseSpacing() }

val LocalFinPulseRadii = staticCompositionLocalOf { FinPulseRadii(FinPulseTokens.baseRadius) }

@Composable
fun FinPulseTheme(
    seed: Color = FinPulseTokens.seed,
    dark: Boolean = isSystemInDarkTheme(),
    baseRadius: Dp = FinPulseTokens.baseRadius,
    spacingMultiplier: Float = 1f,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val colors = if (dark) {
        darkColorScheme(primary = seed, secondary = seed)
    } else {
        lightColorScheme(primary = seed, secondary = seed)
    }
    CompositionLocalProvider(
        LocalFinPulseSpacing provides FinPulseSpacing(spacingMultiplier),
        LocalFinPulseRadii provides FinPulseRadii(baseRadius),
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography(),
            content = content,
        )
    }
}
