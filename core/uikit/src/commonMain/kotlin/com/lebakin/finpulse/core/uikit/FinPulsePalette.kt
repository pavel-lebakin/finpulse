package com.lebakin.finpulse.core.uikit

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

data class FinPulsePalette(
    val name: String,
    val seed: Color,
)

object FinPulsePalettes {
    val all: List<FinPulsePalette> = listOf(
        FinPulsePalette(name = "Green", seed = FinPulseTokens.seed),
        FinPulsePalette(name = "Blue", seed = Color(0xFF1D4E89)),
        FinPulsePalette(name = "Rust", seed = Color(0xFFB85C38)),
        FinPulsePalette(name = "Plum", seed = Color(0xFF5C4B8A)),
    )
}

fun finPulseColorScheme(
    seed: Color,
    dark: Boolean,
): ColorScheme {
    val hue = seed.hue()
    val secondaryHue = (hue + SECONDARY_HUE_SHIFT).mod(HUE_CIRCLE)
    val tertiaryHue = (hue + TERTIARY_HUE_SHIFT).mod(HUE_CIRCLE)
    return if (dark) {
        darkColorScheme(
            primary = color(hue, DARK_PRIMARY_SATURATION, DARK_PRIMARY_LIGHTNESS),
            onPrimary = color(hue, DARK_ON_PRIMARY_SATURATION, DARK_ON_PRIMARY_LIGHTNESS),
            primaryContainer = color(hue, DARK_CONTAINER_SATURATION, DARK_CONTAINER_LIGHTNESS),
            onPrimaryContainer = color(hue, DARK_ON_CONTAINER_SATURATION, DARK_ON_CONTAINER_LIGHTNESS),
            secondary = color(secondaryHue, DARK_SECONDARY_SATURATION, DARK_SECONDARY_LIGHTNESS),
            onSecondary = color(secondaryHue, DARK_ON_PRIMARY_SATURATION, DARK_ON_PRIMARY_LIGHTNESS),
            secondaryContainer = color(secondaryHue, DARK_CONTAINER_SATURATION, DARK_CONTAINER_LIGHTNESS),
            onSecondaryContainer = color(secondaryHue, DARK_ON_CONTAINER_SATURATION, DARK_ON_CONTAINER_LIGHTNESS),
            tertiary = color(tertiaryHue, DARK_SECONDARY_SATURATION, DARK_SECONDARY_LIGHTNESS),
            onTertiary = color(tertiaryHue, DARK_ON_PRIMARY_SATURATION, DARK_ON_PRIMARY_LIGHTNESS),
            tertiaryContainer = color(tertiaryHue, DARK_CONTAINER_SATURATION, DARK_CONTAINER_LIGHTNESS),
            onTertiaryContainer = color(tertiaryHue, DARK_ON_CONTAINER_SATURATION, DARK_ON_CONTAINER_LIGHTNESS),
            background = color(hue, DARK_BACKGROUND_SATURATION, DARK_BACKGROUND_LIGHTNESS),
            onBackground = color(hue, DARK_ON_BACKGROUND_SATURATION, DARK_ON_BACKGROUND_LIGHTNESS),
            surface = color(hue, DARK_SURFACE_SATURATION, DARK_SURFACE_LIGHTNESS),
            onSurface = color(hue, DARK_ON_BACKGROUND_SATURATION, DARK_ON_BACKGROUND_LIGHTNESS),
            surfaceVariant = color(hue, DARK_VARIANT_SATURATION, DARK_VARIANT_LIGHTNESS),
            onSurfaceVariant = color(hue, DARK_ON_VARIANT_SATURATION, DARK_ON_VARIANT_LIGHTNESS),
            outline = color(hue, DARK_OUTLINE_SATURATION, DARK_OUTLINE_LIGHTNESS),
        )
    } else {
        lightColorScheme(
            primary = seed,
            onPrimary = color(hue, LIGHT_ON_PRIMARY_SATURATION, LIGHT_ON_PRIMARY_LIGHTNESS),
            primaryContainer = color(hue, LIGHT_CONTAINER_SATURATION, LIGHT_CONTAINER_LIGHTNESS),
            onPrimaryContainer = color(hue, LIGHT_ON_CONTAINER_SATURATION, LIGHT_ON_CONTAINER_LIGHTNESS),
            secondary = color(secondaryHue, LIGHT_SECONDARY_SATURATION, LIGHT_SECONDARY_LIGHTNESS),
            onSecondary = color(secondaryHue, LIGHT_ON_PRIMARY_SATURATION, LIGHT_ON_PRIMARY_LIGHTNESS),
            secondaryContainer = color(secondaryHue, LIGHT_CONTAINER_SATURATION, LIGHT_CONTAINER_LIGHTNESS),
            onSecondaryContainer = color(secondaryHue, LIGHT_ON_CONTAINER_SATURATION, LIGHT_ON_CONTAINER_LIGHTNESS),
            tertiary = color(tertiaryHue, LIGHT_SECONDARY_SATURATION, LIGHT_SECONDARY_LIGHTNESS),
            onTertiary = color(tertiaryHue, LIGHT_ON_PRIMARY_SATURATION, LIGHT_ON_PRIMARY_LIGHTNESS),
            tertiaryContainer = color(tertiaryHue, LIGHT_CONTAINER_SATURATION, LIGHT_CONTAINER_LIGHTNESS),
            onTertiaryContainer = color(tertiaryHue, LIGHT_ON_CONTAINER_SATURATION, LIGHT_ON_CONTAINER_LIGHTNESS),
            background = color(hue, LIGHT_BACKGROUND_SATURATION, LIGHT_BACKGROUND_LIGHTNESS),
            onBackground = color(hue, LIGHT_ON_BACKGROUND_SATURATION, LIGHT_ON_BACKGROUND_LIGHTNESS),
            surface = color(hue, LIGHT_SURFACE_SATURATION, LIGHT_SURFACE_LIGHTNESS),
            onSurface = color(hue, LIGHT_ON_BACKGROUND_SATURATION, LIGHT_ON_BACKGROUND_LIGHTNESS),
            surfaceVariant = color(hue, LIGHT_VARIANT_SATURATION, LIGHT_VARIANT_LIGHTNESS),
            onSurfaceVariant = color(hue, LIGHT_ON_VARIANT_SATURATION, LIGHT_ON_VARIANT_LIGHTNESS),
            outline = color(hue, LIGHT_OUTLINE_SATURATION, LIGHT_OUTLINE_LIGHTNESS),
        )
    }
}

private fun color(
    hue: Float,
    saturation: Float,
    lightness: Float,
): Color = Color.hsl(hue = hue, saturation = saturation, lightness = lightness)

private fun Color.hue(): Float {
    val maxChannel = max(red, max(green, blue))
    val minChannel = min(red, min(green, blue))
    val delta = maxChannel - minChannel
    if (delta == 0f) {
        return 0f
    }
    val sector = when (maxChannel) {
        red -> (green - blue) / delta
        green -> (blue - red) / delta + GREEN_SECTOR
        else -> (red - green) / delta + BLUE_SECTOR
    }
    return (sector * DEGREES_PER_SECTOR).mod(HUE_CIRCLE)
}

private const val HUE_CIRCLE = 360f
private const val DEGREES_PER_SECTOR = 60f
private const val GREEN_SECTOR = 2f
private const val BLUE_SECTOR = 4f
private const val SECONDARY_HUE_SHIFT = 32f
private const val TERTIARY_HUE_SHIFT = 210f

private const val LIGHT_ON_PRIMARY_SATURATION = 0.15f
private const val LIGHT_ON_PRIMARY_LIGHTNESS = 0.98f
private const val LIGHT_CONTAINER_SATURATION = 0.42f
private const val LIGHT_CONTAINER_LIGHTNESS = 0.88f
private const val LIGHT_ON_CONTAINER_SATURATION = 0.35f
private const val LIGHT_ON_CONTAINER_LIGHTNESS = 0.18f
private const val LIGHT_SECONDARY_SATURATION = 0.38f
private const val LIGHT_SECONDARY_LIGHTNESS = 0.36f
private const val LIGHT_BACKGROUND_SATURATION = 0.28f
private const val LIGHT_BACKGROUND_LIGHTNESS = 0.96f
private const val LIGHT_ON_BACKGROUND_SATURATION = 0.18f
private const val LIGHT_ON_BACKGROUND_LIGHTNESS = 0.14f
private const val LIGHT_SURFACE_SATURATION = 0.22f
private const val LIGHT_SURFACE_LIGHTNESS = 0.98f
private const val LIGHT_VARIANT_SATURATION = 0.24f
private const val LIGHT_VARIANT_LIGHTNESS = 0.90f
private const val LIGHT_ON_VARIANT_SATURATION = 0.14f
private const val LIGHT_ON_VARIANT_LIGHTNESS = 0.32f
private const val LIGHT_OUTLINE_SATURATION = 0.12f
private const val LIGHT_OUTLINE_LIGHTNESS = 0.52f

private const val DARK_PRIMARY_SATURATION = 0.42f
private const val DARK_PRIMARY_LIGHTNESS = 0.74f
private const val DARK_ON_PRIMARY_SATURATION = 0.35f
private const val DARK_ON_PRIMARY_LIGHTNESS = 0.14f
private const val DARK_CONTAINER_SATURATION = 0.28f
private const val DARK_CONTAINER_LIGHTNESS = 0.26f
private const val DARK_ON_CONTAINER_SATURATION = 0.30f
private const val DARK_ON_CONTAINER_LIGHTNESS = 0.90f
private const val DARK_SECONDARY_SATURATION = 0.28f
private const val DARK_SECONDARY_LIGHTNESS = 0.74f
private const val DARK_BACKGROUND_SATURATION = 0.18f
private const val DARK_BACKGROUND_LIGHTNESS = 0.08f
private const val DARK_ON_BACKGROUND_SATURATION = 0.12f
private const val DARK_ON_BACKGROUND_LIGHTNESS = 0.92f
private const val DARK_SURFACE_SATURATION = 0.16f
private const val DARK_SURFACE_LIGHTNESS = 0.12f
private const val DARK_VARIANT_SATURATION = 0.14f
private const val DARK_VARIANT_LIGHTNESS = 0.20f
private const val DARK_ON_VARIANT_SATURATION = 0.10f
private const val DARK_ON_VARIANT_LIGHTNESS = 0.78f
private const val DARK_OUTLINE_SATURATION = 0.10f
private const val DARK_OUTLINE_LIGHTNESS = 0.46f
