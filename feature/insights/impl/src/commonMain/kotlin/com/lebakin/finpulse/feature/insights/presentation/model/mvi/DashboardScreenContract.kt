package com.lebakin.finpulse.feature.insights.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.feature.insights.presentation.mapper.SpendingUiModel

sealed interface DashboardContent {
    data object Empty : DashboardContent

    data class Ready(
        val showcase: Boolean,
        val spending: SpendingUiModel,
    ) : DashboardContent
}

sealed interface DashboardSheet

data class DashboardState(
    val content: Loadable<DashboardContent>,
    val message: UserMessage?,
    val sheet: DashboardSheet?,
) : ScreenState

sealed interface DashboardAction : ScreenAction {
    data object ClearSample : DashboardAction

    data object ScanReceipt : DashboardAction

    data object AskPhrase : DashboardAction

    data object ShowOnboardingAgain : DashboardAction

    data object OpenConsent : DashboardAction

    data object OpenKey : DashboardAction

    data object OpenLicenses : DashboardAction

    data object Retry : DashboardAction

    data object DismissMessage : DashboardAction

    data object DismissSheet : DashboardAction
}

object DashboardReducer {
    fun initial(): DashboardState = DashboardState(
        content = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun DashboardState.withContent(content: DashboardContent): DashboardState =
        copy(content = Loadable.Ready(content))

    fun DashboardState.withError(error: AppError): DashboardState = copy(content = Loadable.Failed(error))

    fun DashboardState.withMessage(message: UserMessage?): DashboardState = copy(message = message)

    fun DashboardState.withSheet(sheet: DashboardSheet?): DashboardState = copy(sheet = sheet)
}
