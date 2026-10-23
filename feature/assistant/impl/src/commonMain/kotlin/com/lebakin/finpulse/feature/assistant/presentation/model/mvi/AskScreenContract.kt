package com.lebakin.finpulse.feature.assistant.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

data class AskContent(
    val sending: Boolean,
    val answer: String?,
)

sealed interface AskSheet

data class AskState(
    val content: Loadable<AskContent>,
    val message: UserMessage?,
    val sheet: AskSheet?,
) : ScreenState

sealed interface AskAction : ScreenAction {
    class Send(val phrase: String) : AskAction {
        override fun toString(): String = "Send"
    }

    data object Back : AskAction

    data object Retry : AskAction

    data object DismissMessage : AskAction

    data object DismissSheet : AskAction
}

object AskReducer {
    fun initial(): AskState = AskState(
        content = Loadable.Ready(AskContent(sending = false, answer = null)),
        message = null,
        sheet = null,
    )

    fun AskState.withContent(content: AskContent): AskState = copy(content = Loadable.Ready(content))

    fun AskState.withError(error: AppError): AskState = copy(content = Loadable.Failed(error))

    fun AskState.withSending(sending: Boolean): AskState {
        val ready = content as? Loadable.Ready ?: return this
        val answer = if (sending) null else ready.value.answer
        return copy(content = Loadable.Ready(ready.value.copy(sending = sending, answer = answer)))
    }

    fun AskState.withAnswer(answer: String?): AskState {
        val ready = content as? Loadable.Ready ?: return this
        return copy(content = Loadable.Ready(ready.value.copy(answer = answer, sending = false)))
    }

    fun AskState.withMessage(message: UserMessage?): AskState = copy(message = message)

    fun AskState.withSheet(sheet: AskSheet?): AskState = copy(sheet = sheet)
}
