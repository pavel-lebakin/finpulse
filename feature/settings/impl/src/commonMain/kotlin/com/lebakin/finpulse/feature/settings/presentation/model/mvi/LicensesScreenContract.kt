package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.mikepenz.aboutlibraries.Libs

data class LicensesContent(
    val libraries: Libs,
)

sealed interface LicensesSheet

data class LicensesState(
    val content: Loadable<LicensesContent>,
    val message: UserMessage?,
    val sheet: LicensesSheet?,
) : ScreenState

sealed interface LicensesAction : ScreenAction {
    data object Retry : LicensesAction

    data object DismissMessage : LicensesAction

    data object DismissSheet : LicensesAction
}

object LicensesReducer {
    fun initial(): LicensesState = LicensesState(
        content = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun LicensesState.withLibraries(libraries: Libs): LicensesState =
        copy(content = Loadable.Ready(LicensesContent(libraries)))

    fun LicensesState.withError(error: AppError): LicensesState = copy(content = Loadable.Failed(error))

    fun LicensesState.withMessage(message: UserMessage?): LicensesState = copy(message = message)

    fun LicensesState.withSheet(sheet: LicensesSheet?): LicensesState = copy(sheet = sheet)
}
