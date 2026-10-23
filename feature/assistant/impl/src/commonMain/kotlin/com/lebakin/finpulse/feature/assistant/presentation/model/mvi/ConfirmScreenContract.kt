package com.lebakin.finpulse.feature.assistant.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.feature.assistant.presentation.mapper.PhraseDraftUiModel

sealed interface ConfirmSheet

data class ConfirmState(
    val content: Loadable<PhraseDraftUiModel>,
    val saving: Boolean,
    val saved: Boolean,
    val message: UserMessage?,
    val sheet: ConfirmSheet?,
) : ScreenState

sealed interface ConfirmAction : ScreenAction {
    data object Confirm : ConfirmAction

    data object Done : ConfirmAction

    data object Back : ConfirmAction

    data object DismissMessage : ConfirmAction

    data object DismissSheet : ConfirmAction
}

object ConfirmReducer {
    fun initial(): ConfirmState = ConfirmState(
        content = Loadable.Loading,
        saving = false,
        saved = false,
        message = null,
        sheet = null,
    )

    fun ConfirmState.withDraft(draft: PhraseDraftUiModel): ConfirmState = copy(content = Loadable.Ready(draft))

    fun ConfirmState.withError(error: AppError): ConfirmState = copy(content = Loadable.Failed(error))

    fun ConfirmState.withSaving(saving: Boolean): ConfirmState = copy(saving = saving)

    fun ConfirmState.withSaved(): ConfirmState = copy(saved = true, saving = false)

    fun ConfirmState.withMessage(message: UserMessage?): ConfirmState = copy(message = message)

    fun ConfirmState.withSheet(sheet: ConfirmSheet?): ConfirmState = copy(sheet = sheet)
}
