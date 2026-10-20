package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class KeyReducerTest {
    @Test
    fun initialIsLoadingWithoutAMessage() {
        val start = KeyReducer.initial()
        assertIs<Loadable.Loading>(start.content)
        assertNull(start.message)
        assertNull(start.sheet)
    }

    @Test
    fun contentReplacesLoading() {
        val ready = KeyReducer.run {
            KeyReducer.initial().withContent(KeyContent(saved = true, granted = true))
        }
        val content = assertIs<Loadable.Ready<KeyContent>>(ready.content)
        assertEquals(true, content.value.saved)
        assertEquals(true, content.value.granted)
    }

    @Test
    fun saveActionDoesNotPrintTheKey() {
        val action = KeyAction.Save("token-ok")
        assertFalse(action.toString().contains("token-ok"))
    }
}
