package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ConsentReducerTest {
    @Test
    fun initialIsLoadingWithoutAMessage() {
        val start = ConsentReducer.initial()
        assertIs<Loadable.Loading>(start.choice)
        assertNull(start.message)
        assertNull(start.sheet)
    }

    @Test
    fun grantedChoiceReplacesLoading() {
        val ready = ConsentReducer.run { ConsentReducer.initial().withGranted(true) }
        val choice = assertIs<Loadable.Ready<Boolean>>(ready.choice)
        assertEquals(true, choice.value)
        val revoked = ConsentReducer.run { ready.withGranted(false) }
        val next = assertIs<Loadable.Ready<Boolean>>(revoked.choice)
        assertEquals(false, next.value)
    }
}
