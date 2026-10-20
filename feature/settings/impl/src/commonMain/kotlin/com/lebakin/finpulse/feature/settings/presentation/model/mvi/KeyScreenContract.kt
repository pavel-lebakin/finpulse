package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

data class KeyContent(
    val saved: Boolean,
    val granted: Boolean,
)

sealed interface KeySheet

data class KeyState(
    val content: Loadable<KeyContent>,
    val message: UserMessage?,
    val sheet: KeySheet?,
) : ScreenState

sealed interface KeyAction : ScreenAction {
    class Save(val key: String) : KeyAction {
        override fun toString(): String = "Save"
    }

    data object Delete : KeyAction

    data object OpenConsent : KeyAction

    data object Retry : KeyAction

    data object DismissMessage : KeyAction

    data object DismissSheet : KeyAction
}

object KeyReducer {
    fun initial(): KeyState = KeyState(
        content = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun KeyState.withContent(content: KeyContent): KeyState = copy(content = Loadable.Ready(content))

    fun KeyState.withError(error: AppError): KeyState = copy(content = Loadable.Failed(error))

    fun KeyState.withMessage(message: UserMessage?): KeyState = copy(message = message)

    fun KeyState.withSheet(sheet: KeySheet?): KeyState = copy(sheet = sheet)
}
