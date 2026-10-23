package com.lebakin.finpulse.feature.assistant.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AskReducerTest {
    @Test
    fun sendingClearsTheAnswer() {
        val start = AskReducer.initial()
        val answered = AskReducer.run { start.withAnswer("You spent 1.00 USD this month.") }
        val sending = AskReducer.run { answered.withSending(true) }
        val ready = assertIs<Loadable.Ready<AskContent>>(sending.content)

        assertEquals(true, ready.value.sending)
        assertNull(ready.value.answer)
        assertEquals("Send", AskAction.Send("Coffee 4.50").toString())
    }

    @Test
    fun anErrorReplacesTheContent() {
        val failed = AskReducer.run {
            initial().withError(AppError.Unexpected(IllegalStateException("missing")))
        }

        assertIs<Loadable.Failed>(failed.content)
    }
}
