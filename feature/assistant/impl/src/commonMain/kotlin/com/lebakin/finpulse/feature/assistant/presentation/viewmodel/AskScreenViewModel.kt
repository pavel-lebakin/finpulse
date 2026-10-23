package com.lebakin.finpulse.feature.assistant.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.assistant.domain.PhraseClock
import com.lebakin.finpulse.feature.assistant.domain.PhraseEngines
import com.lebakin.finpulse.feature.assistant.domain.PhraseResult
import com.lebakin.finpulse.feature.assistant.domain.PhraseSession
import com.lebakin.finpulse.feature.assistant.domain.monthSpendAnswer
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskAction
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskReducer
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskState
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigator
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import kotlinx.coroutines.flow.first

internal class AskScreenViewModel(
    private val engines: PhraseEngines,
    private val ledgerReader: LedgerReader,
    private val clock: PhraseClock,
    private val session: PhraseSession,
    private val navigator: AssistantNavigator,
) : ScreenViewModel<AskState, AskAction>() {
    override fun createInitialState(): AskState = AskReducer.initial()

    override fun onAction(action: AskAction) {
        when (action) {
            is AskAction.Send -> send(action.phrase)
            AskAction.Back -> navigator.back()
            AskAction.Retry -> reduce { AskReducer.run { initial() } }
            AskAction.DismissMessage -> reduce { AskReducer.run { withMessage(null) } }
            AskAction.DismissSheet -> reduce { AskReducer.run { withSheet(null) } }
        }
    }

    private fun send(phrase: String) {
        if (isSending() || phrase.isBlank()) {
            return
        }
        reduce { AskReducer.run { withSending(true).withMessage(null) } }
        launchCatching(onError = { error ->
            reduce { AskReducer.run { withSending(false).withMessage(error.toUserMessage()) } }
        }) {
            when (val result = engines.read(phrase)) {
                is PhraseResult.Draft -> openDraft(result)
                PhraseResult.MonthSpend -> answerMonth()
                PhraseResult.UnsupportedQuestion -> show(ONLY_MONTH)
            }
        }
    }

    private fun openDraft(result: PhraseResult.Draft) {
        session.store(result.draft)
        reduce { AskReducer.run { withSending(false) } }
        navigator.openConfirm()
    }

    private suspend fun answerMonth() {
        val transactions = ledgerReader.observeTransactions().first()
        show(monthSpendAnswer(transactions, clock.today()))
    }

    private fun show(answer: String) {
        reduce { AskReducer.run { withAnswer(answer) } }
    }

    private fun isSending(): Boolean {
        val ready = currentState.content as? Loadable.Ready ?: return false
        return ready.value.sending
    }

    private companion object {
        const val ONLY_MONTH = "I can tell you how much you spent this month."
    }
}
