package com.lebakin.finpulse.feature.scanner.presentation.viewmodel

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.scanner.domain.ReceiptIds
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSession
import com.lebakin.finpulse.feature.scanner.domain.toUserReceipt
import com.lebakin.finpulse.feature.scanner.presentation.mapper.toUiModel
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewAction
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewReducer
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewState
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.UserMessage
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigator

internal class ReviewScreenViewModel(
    private val session: ReceiptSession,
    private val ledgerWriter: LedgerWriter,
    private val insights: InsightsEntry,
    private val navigator: ScannerNavigator,
    private val ids: ReceiptIds,
) : ScreenViewModel<ReviewState, ReviewAction>() {
    init {
        val draft = session.current()
        if (draft == null) {
            reduce {
                ReviewReducer.run {
                    withError(AppError.Unexpected(IllegalStateException("Missing receipt draft")))
                }
            }
        } else {
            reduce { ReviewReducer.run { withDraft(draft.toUiModel()) } }
        }
    }

    override fun createInitialState(): ReviewState = ReviewReducer.initial()

    override fun onAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Confirm -> confirm()
            ReviewAction.Done -> done()
            ReviewAction.Back -> navigator.back()
            ReviewAction.DismissMessage -> reduce { ReviewReducer.run { withMessage(null) } }
            ReviewAction.DismissSheet -> reduce { ReviewReducer.run { withSheet(null) } }
        }
    }

    private fun confirm() {
        if (currentState.saved || currentState.saving) {
            return
        }
        val draft = session.current() ?: return
        reduce { ReviewReducer.run { withSaving(true) } }
        launchCatching(onError = { error ->
            reduce { ReviewReducer.run { withSaving(false).withMessage(error.toUserMessage()) } }
        }) {
            val write = draft.toUserReceipt(ids)
            val removed = ledgerWriter.addUserTransaction(write.category, write.transaction)
            session.clear()
            val text = if (removed) SAMPLE_REMOVED else SAVED
            reduce { ReviewReducer.run { withSaved().withMessage(UserMessage(text)) } }
        }
    }

    private fun done() {
        if (currentState.saved) {
            insights.openDashboard()
        }
    }

    private companion object {
        const val SAVED = "Saved."
        const val SAMPLE_REMOVED = "Saved. The sample was removed."
    }
}
