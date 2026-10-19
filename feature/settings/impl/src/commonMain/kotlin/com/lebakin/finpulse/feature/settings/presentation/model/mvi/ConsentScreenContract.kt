package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

sealed interface ConsentSheet

data class ConsentState(
    val choice: Loadable<Boolean>,
    val message: UserMessage?,
    val sheet: ConsentSheet?,
) : ScreenState

sealed interface ConsentAction : ScreenAction {
    data object Agree : ConsentAction

    data object Decline : ConsentAction

    data object Retry : ConsentAction

    data object DismissMessage : ConsentAction

    data object DismissSheet : ConsentAction
}

object ConsentReducer {
    fun initial(): ConsentState = ConsentState(
        choice = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun ConsentState.withGranted(granted: Boolean): ConsentState = copy(choice = Loadable.Ready(granted))

    fun ConsentState.withError(error: AppError): ConsentState = copy(choice = Loadable.Failed(error))

    fun ConsentState.withMessage(message: UserMessage?): ConsentState = copy(message = message)

    fun ConsentState.withSheet(sheet: ConsentSheet?): ConsentState = copy(sheet = sheet)
}
