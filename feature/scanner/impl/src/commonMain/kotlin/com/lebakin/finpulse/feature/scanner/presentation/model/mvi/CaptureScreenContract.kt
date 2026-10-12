package com.lebakin.finpulse.feature.scanner.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.feature.scanner.presentation.mapper.ReceiptSampleUiModel

data class CaptureContent(
    val samples: List<ReceiptSampleUiModel>,
    val reading: Boolean,
)

sealed interface CaptureSheet

data class CaptureState(
    val content: Loadable<CaptureContent>,
    val message: UserMessage?,
    val sheet: CaptureSheet?,
) : ScreenState

sealed interface CaptureAction : ScreenAction {
    data object TakePhoto : CaptureAction

    data object PickFromGallery : CaptureAction

    data class ChooseSample(val id: String) : CaptureAction

    data object Back : CaptureAction

    data object DismissMessage : CaptureAction

    data object DismissSheet : CaptureAction
}

object CaptureReducer {
    fun initial(samples: List<ReceiptSampleUiModel>): CaptureState = CaptureState(
        content = Loadable.Ready(CaptureContent(samples = samples, reading = false)),
        message = null,
        sheet = null,
    )

    fun CaptureState.withReading(reading: Boolean): CaptureState {
        val ready = content as? Loadable.Ready ?: return this
        return copy(content = Loadable.Ready(ready.value.copy(reading = reading)))
    }

    fun CaptureState.withError(error: AppError): CaptureState = copy(content = Loadable.Failed(error))

    fun CaptureState.withMessage(message: UserMessage?): CaptureState = copy(message = message)

    fun CaptureState.withSheet(sheet: CaptureSheet?): CaptureState = copy(sheet = sheet)
}
