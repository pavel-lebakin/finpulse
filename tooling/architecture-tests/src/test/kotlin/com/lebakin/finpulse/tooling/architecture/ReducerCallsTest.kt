package com.lebakin.finpulse.tooling.architecture

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReducerCallsTest {
    @Test
    fun glossaryCallInsideReduceIsAllowed() {
        val body = """
            reduce { TransactionsReducer.run { withItems(items) } }
            launchCatching(onError = { error ->
                reduce { TransactionsReducer.run { withError(error) } }
            }) {
                throw error
            }
        """.trimIndent()
        assertTrue(ReducerCalls.calledOnlyInsideReduce(body, NAMES))
    }

    @Test
    fun callOutsideReduceIsRejected() {
        val body = """
            val next = TransactionsReducer.run { withItems(items) }
            reduce { next }
        """.trimIndent()
        assertFalse(ReducerCalls.calledOnlyInsideReduce(body, NAMES))
    }

    @Test
    fun parenFormKeepsTheCallInsideReduce() {
        val body = "reduce({ TransactionsReducer.run { withError(error) } })"
        assertTrue(ReducerCalls.calledOnlyInsideReduce(body, NAMES))
    }

    @Test
    fun commentsAndStringsDoNotCountAsCalls() {
        val body = """
            // withItems(items)
            val label = "withError(error)"
            reduce { TransactionsReducer.run { withItems(items) } }
        """.trimIndent()
        assertTrue(ReducerCalls.calledOnlyInsideReduce(body, NAMES))
    }

    @Test
    fun initialOutsideReduceIsAllowed() {
        val body = """
            fun createInitialState() = TransactionsReducer.initial()
            reduce { TransactionsReducer.run { withItems(items) } }
        """.trimIndent()
        assertTrue(ReducerCalls.calledOnlyInsideReduce(body, NAMES))
    }

    private companion object {
        val NAMES = setOf("withItems", "withError")
    }
}
