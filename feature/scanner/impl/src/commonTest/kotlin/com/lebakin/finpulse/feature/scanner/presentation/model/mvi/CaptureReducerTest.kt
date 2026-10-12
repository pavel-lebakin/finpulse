package com.lebakin.finpulse.feature.scanner.presentation.model.mvi

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.scanner.presentation.mapper.ReceiptSampleUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CaptureReducerTest {
    @Test
    fun readingKeepsTheSamplesAndTheMessageClears() {
        val start = CaptureReducer.initial(listOf(ReceiptSampleUiModel(id = "groceries", title = "Groceries")))
        assertNull(start.message)
        assertNull(start.sheet)
        val reading = CaptureReducer.run { start.withReading(true) }
        val content = assertReady(reading)
        assertEquals(true, content.reading)
        assertEquals("groceries", content.samples.single().id)
        val messaged = CaptureReducer.run { reading.withMessage(UserMessage("Camera permission is required.")) }
        assertEquals("Camera permission is required.", messaged.message?.text)
        val idle = CaptureReducer.run { messaged.withReading(false).withMessage(null) }
        assertEquals(false, assertReady(idle).reading)
        assertNull(idle.message)
    }
}

private fun assertReady(state: CaptureState) = (state.content as Loadable.Ready).value
