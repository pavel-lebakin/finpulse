package com.lebakin.finpulse.feature.insights.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.feature.insights.presentation.mapper.ReceiptDetailUiModel

data class ReceiptState(
    val content: Loadable<ReceiptDetailUiModel>,
    val message: UserMessage?,
    val sheet: ReceiptSheet?,
) : ScreenState

sealed interface ReceiptSheet

sealed interface ReceiptAction : ScreenAction {
    data object Back : ReceiptAction
}

object ReceiptReducer {
    fun initial(): ReceiptState = ReceiptState(
        content = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun ReceiptState.withReceipt(receipt: ReceiptDetailUiModel): ReceiptState =
        copy(content = Loadable.Ready(receipt))

    fun ReceiptState.withError(error: AppError): ReceiptState = copy(content = Loadable.Failed(error))
}
