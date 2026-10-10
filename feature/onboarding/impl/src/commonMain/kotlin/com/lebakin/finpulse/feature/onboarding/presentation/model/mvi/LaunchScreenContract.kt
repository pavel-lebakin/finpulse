package com.lebakin.finpulse.feature.onboarding.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

sealed interface LaunchSheet

data class LaunchState(
    val phase: Loadable<Unit>,
    val message: UserMessage?,
    val sheet: LaunchSheet?,
) : ScreenState

sealed interface LaunchAction : ScreenAction {
    data object Retry : LaunchAction

    data object DismissMessage : LaunchAction

    data object DismissSheet : LaunchAction
}

object LaunchReducer {
    fun initial(): LaunchState = LaunchState(
        phase = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun LaunchState.withLoading(): LaunchState = copy(phase = Loadable.Loading)

    fun LaunchState.withReady(): LaunchState = copy(phase = Loadable.Ready(Unit))

    fun LaunchState.withError(error: AppError): LaunchState = copy(phase = Loadable.Failed(error))

    fun LaunchState.withMessage(message: UserMessage?): LaunchState = copy(message = message)

    fun LaunchState.withSheet(sheet: LaunchSheet?): LaunchState = copy(sheet = sheet)
}
