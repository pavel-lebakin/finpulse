package com.lebakin.finpulse.feature.onboarding.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

sealed interface HomeContent {
    data object Showcase : HomeContent

    data object Empty : HomeContent

    data object Ready : HomeContent
}

sealed interface HomeSheet

data class HomeState(
    val content: Loadable<HomeContent>,
    val message: UserMessage?,
    val sheet: HomeSheet?,
) : ScreenState

sealed interface HomeAction : ScreenAction {
    data object ClearSample : HomeAction

    data object ShowOnboardingAgain : HomeAction

    data object Retry : HomeAction

    data object DismissMessage : HomeAction

    data object DismissSheet : HomeAction
}

object HomeReducer {
    fun initial(): HomeState = HomeState(
        content = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun HomeState.withContent(content: HomeContent): HomeState = copy(content = Loadable.Ready(content))

    fun HomeState.withError(error: AppError): HomeState = copy(content = Loadable.Failed(error))

    fun HomeState.withMessage(message: UserMessage?): HomeState = copy(message = message)

    fun HomeState.withSheet(sheet: HomeSheet?): HomeState = copy(sheet = sheet)
}
