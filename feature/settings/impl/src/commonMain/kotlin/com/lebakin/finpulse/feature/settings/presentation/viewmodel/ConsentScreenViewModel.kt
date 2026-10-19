package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentReducer
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlinx.coroutines.yield

internal class ConsentScreenViewModel(
    private val reader: SettingsReader,
    private val writer: SettingsWriter,
    private val navigator: SettingsNavigator,
) : ScreenViewModel<ConsentState, ConsentAction>() {
    init {
        observe()
    }

    override fun createInitialState(): ConsentState = ConsentReducer.initial()

    override fun onAction(action: ConsentAction) {
        when (action) {
            ConsentAction.Agree -> choose(granted = true)
            ConsentAction.Decline -> choose(granted = false)
            ConsentAction.Retry -> observe()
            ConsentAction.DismissMessage -> reduce { ConsentReducer.run { withMessage(null) } }
            ConsentAction.DismissSheet -> reduce { ConsentReducer.run { withSheet(null) } }
        }
    }

    private fun observe() {
        launchCatching(onError = { error -> reduce { ConsentReducer.run { withError(error) } } }) {
            yield()
            reader.observeConsent().collect { granted ->
                reduce { ConsentReducer.run { withGranted(granted) } }
            }
        }
    }

    private fun choose(granted: Boolean) {
        launchCatching(onError = { error ->
            reduce { ConsentReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            if (granted) {
                writer.grantConsent()
            } else {
                writer.declineConsent()
            }
            navigator.back()
        }
    }
}
