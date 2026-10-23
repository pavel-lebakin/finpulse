package com.lebakin.finpulse.feature.assistant.presentation.viewmodel

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.assistant.domain.PhraseIds
import com.lebakin.finpulse.feature.assistant.domain.PhraseSession
import com.lebakin.finpulse.feature.assistant.domain.toUserPhrase
import com.lebakin.finpulse.feature.assistant.presentation.mapper.toUiModel
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmAction
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmReducer
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmState
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.UserMessage
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigator
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter

internal class ConfirmScreenViewModel(
    private val session: PhraseSession,
    private val ledgerWriter: LedgerWriter,
    private val insights: InsightsEntry,
    private val navigator: AssistantNavigator,
    private val ids: PhraseIds,
) : ScreenViewModel<ConfirmState, ConfirmAction>() {
    init {
        val draft = session.current()
        if (draft == null) {
            reduce {
                ConfirmReducer.run {
                    withError(AppError.Unexpected(IllegalStateException("Missing phrase draft")))
                }
            }
        } else {
            reduce { ConfirmReducer.run { withDraft(draft.toUiModel()) } }
        }
    }

    override fun createInitialState(): ConfirmState = ConfirmReducer.initial()

    override fun onAction(action: ConfirmAction) {
        when (action) {
            ConfirmAction.Confirm -> confirm()
            ConfirmAction.Done -> done()
            ConfirmAction.Back -> navigator.back()
            ConfirmAction.DismissMessage -> reduce { ConfirmReducer.run { withMessage(null) } }
            ConfirmAction.DismissSheet -> reduce { ConfirmReducer.run { withSheet(null) } }
        }
    }

    private fun confirm() {
        if (currentState.saved || currentState.saving) {
            return
        }
        val draft = session.current() ?: return
        reduce { ConfirmReducer.run { withSaving(true) } }
        launchCatching(onError = { error ->
            reduce { ConfirmReducer.run { withSaving(false).withMessage(error.toUserMessage()) } }
        }) {
            val write = draft.toUserPhrase(ids)
            val removed = ledgerWriter.addUserTransaction(write.category, write.transaction)
            session.clear()
            val text = if (removed) SAMPLE_REMOVED else SAVED
            reduce { ConfirmReducer.run { withSaved().withMessage(UserMessage(text)) } }
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
