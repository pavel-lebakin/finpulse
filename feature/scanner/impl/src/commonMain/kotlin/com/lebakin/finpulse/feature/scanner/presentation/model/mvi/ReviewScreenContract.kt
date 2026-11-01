package com.lebakin.finpulse.feature.scanner.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.feature.scanner.presentation.mapper.ReceiptDraftUiModel

sealed interface ReviewSheet

data class ReviewState(
    val content: Loadable<ReceiptDraftUiModel>,
    val saving: Boolean,
    val saved: Boolean,
    val message: UserMessage?,
    val sheet: ReviewSheet?,
) : ScreenState

sealed interface ReviewAction : ScreenAction {
    data object Confirm : ReviewAction

    data object Done : ReviewAction

    data object Back : ReviewAction

    data object DismissMessage : ReviewAction

    data object DismissSheet : ReviewAction

    data class SelectCurrency(val code: String) : ReviewAction
}

object ReviewReducer {
    fun initial(): ReviewState = ReviewState(
        content = Loadable.Loading,
        saving = false,
        saved = false,
        message = null,
        sheet = null,
    )

    fun ReviewState.withDraft(draft: ReceiptDraftUiModel): ReviewState = copy(
        content = Loadable.Ready(draft),
        saving = false,
        saved = false,
        message = null,
    )

    fun ReviewState.withError(error: AppError): ReviewState = copy(content = Loadable.Failed(error))

    fun ReviewState.withSaving(saving: Boolean): ReviewState = copy(saving = saving)

    fun ReviewState.withSaved(): ReviewState = copy(saved = true, saving = false)

    fun ReviewState.withMessage(message: UserMessage?): ReviewState = copy(message = message)

    fun ReviewState.withSheet(sheet: ReviewSheet?): ReviewState = copy(sheet = sheet)
}
