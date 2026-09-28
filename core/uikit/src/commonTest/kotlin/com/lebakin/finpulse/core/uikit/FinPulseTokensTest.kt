package com.lebakin.finpulse.core.uikit

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
