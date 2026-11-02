package com.lebakin.finpulse.feature.insights.presentation.viewmodel

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.insights.presentation.mapper.toDetail
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.ReceiptAction
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.ReceiptReducer
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.ReceiptState
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsNavigator
import com.lebakin.finpulse.feature.ledger.api.LedgerReader

internal class ReceiptScreenViewModel(
    private val transactionId: String,
    private val ledgerReader: LedgerReader,
    private val navigator: InsightsNavigator,
) : ScreenViewModel<ReceiptState, ReceiptAction>() {
    init {
        launchCatching(onError = { error -> reduce { ReceiptReducer.run { withError(error) } } }) {
            ledgerReader.observeTransactions().collect { rows ->
                val found = rows.firstOrNull { it.id == transactionId }
                val missing = AppError.Unexpected(IllegalStateException("Missing receipt"))
                reduce {
                    ReceiptReducer.run {
                        if (found == null) withError(missing) else withReceipt(found.toDetail())
                    }
                }
            }
        }
    }

    override fun createInitialState(): ReceiptState = ReceiptReducer.initial()

    override fun onAction(action: ReceiptAction) {
        when (action) {
            ReceiptAction.Back -> navigator.back()
        }
    }
}
