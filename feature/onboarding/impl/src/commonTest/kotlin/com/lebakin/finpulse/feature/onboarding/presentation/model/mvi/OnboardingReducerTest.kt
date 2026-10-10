package com.lebakin.finpulse.feature.onboarding.presentation.model.mvi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnboardingReducerTest {
    @Test
    fun fourPromisesEndOnTheSampleChoice() {
        val start = OnboardingReducer.initial()
        assertEquals(
            listOf(
                "Offline and private",
                "Scanner",
                "Assistant",
                "Sample data",
            ),
            start.pages.map { it.title },
        )
        assertEquals(
            "Your data stays on this device until you turn the cloud on.",
            start.pages.first().body,
        )
        assertEquals(
            "Look through three months of history, or start from an empty list.",
            start.pages.last().body,
        )
        val last = (0 until start.pages.lastIndex).fold(start) { state, _ ->
            OnboardingReducer.run { state.withNext() }
        }
        assertEquals(start.pages.lastIndex, last.index)
        val stuck = OnboardingReducer.run { last.withNext() }
        assertEquals(start.pages.lastIndex, stuck.index)
        val back = OnboardingReducer.run { stuck.withBack() }
        assertEquals(start.pages.lastIndex - 1, back.index)
        assertNull(start.message)
        assertNull(start.sheet)
    }
}
