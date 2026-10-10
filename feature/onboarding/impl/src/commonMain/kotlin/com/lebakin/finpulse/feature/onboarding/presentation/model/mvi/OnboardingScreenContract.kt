package com.lebakin.finpulse.feature.onboarding.presentation.model.mvi

import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState

data class OnboardingPageUiModel(
    val title: String,
    val body: String,
)

sealed interface OnboardingSheet

data class OnboardingState(
    val pages: List<OnboardingPageUiModel>,
    val index: Int,
    val message: UserMessage?,
    val sheet: OnboardingSheet?,
) : ScreenState

sealed interface OnboardingAction : ScreenAction {
    data object Next : OnboardingAction

    data object Back : OnboardingAction

    data object SeeSample : OnboardingAction

    data object StartEmpty : OnboardingAction

    data object DismissMessage : OnboardingAction

    data object DismissSheet : OnboardingAction
}

object OnboardingReducer {
    fun initial(): OnboardingState = OnboardingState(
        pages = pages,
        index = 0,
        message = null,
        sheet = null,
    )

    fun OnboardingState.withNext(): OnboardingState =
        if (index < pages.lastIndex) copy(index = index + 1) else this

    fun OnboardingState.withBack(): OnboardingState =
        if (index > 0) copy(index = index - 1) else this

    fun OnboardingState.withMessage(message: UserMessage?): OnboardingState = copy(message = message)

    fun OnboardingState.withSheet(sheet: OnboardingSheet?): OnboardingState = copy(sheet = sheet)

    private val pages = listOf(
        OnboardingPageUiModel(
            title = "Offline and private",
            body = "Your data stays on this device until you turn the cloud on.",
        ),
        OnboardingPageUiModel(
            title = "Scanner",
            body = "A photo of a receipt becomes a transaction draft.",
        ),
        OnboardingPageUiModel(
            title = "Assistant",
            body = "A phrase becomes a draft. Nothing is saved until you confirm it.",
        ),
        OnboardingPageUiModel(
            title = "Sample data",
            body = "Look through three months of history, or start from an empty list.",
        ),
    )
}
