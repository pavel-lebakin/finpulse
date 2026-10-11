package com.lebakin.finpulse.feature.insights.presentation.model.mvi

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.insights.presentation.mapper.SpendingUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DashboardReducerTest {
    @Test
    fun contentReplacesLoadingAndKeepsTheMessageClear() {
        val start = DashboardReducer.initial()
        assertEquals(Loadable.Loading, start.content)
        assertNull(start.message)
        assertNull(start.sheet)
        val spending = SpendingUiModel(
            monthExpense = "0.00 RUB",
            months = emptyList(),
            categories = emptyList(),
            recent = emptyList(),
        )
        val ready = DashboardReducer.run {
            start.withContent(DashboardContent.Ready(showcase = true, spending = spending))
        }
        val content = assertIs<Loadable.Ready<DashboardContent>>(ready.content)
        val shown = assertIs<DashboardContent.Ready>(content.value)
        assertEquals(true, shown.showcase)
        val messaged = DashboardReducer.run { ready.withMessage(UserMessage("The sample was removed.")) }
        assertEquals("The sample was removed.", messaged.message?.text)
        val dismissed = DashboardReducer.run { messaged.withMessage(null) }
        assertNull(dismissed.message)
    }
}
