package com.lebakin.finpulse.core.uikit

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinPulseTokensTest {
    @Test
    fun spacingStepIsFourDpTimesMultiplier() {
        assertEquals(8.dp, FinPulseSpacing(multiplier = 2f).step)
        assertEquals(16.dp, FinPulseSpacing(multiplier = 2f).s)
    }

    @Test
    fun radiiGrowFromTheBase() {
        val radii = FinPulseRadii(base = 12.dp)
        assertEquals(6.dp, radii.small)
        assertEquals(12.dp, radii.medium)
        assertEquals(24.dp, radii.large)
    }

    @Test
    fun darkPaletteBackgroundIsDarkerThanTheLightOne() {
        val light = finPulseColorScheme(FinPulseTokens.seed, dark = false)
        val dark = finPulseColorScheme(FinPulseTokens.seed, dark = true)
        assertTrue(dark.background.luminance() < light.background.luminance())
        assertTrue(light.onBackground.luminance() < light.background.luminance())
        assertTrue(dark.onBackground.luminance() > dark.background.luminance())
    }

    @Test
    fun eachPaletteHasItsOwnSeed() {
        val seeds = FinPulsePalettes.all.map { it.seed }
        assertEquals(seeds.toSet().size, seeds.size)
        assertEquals(FinPulseTokens.seed, FinPulsePalettes.all.first().seed)
    }
}
