package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.settings.api.KeySave
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyContent
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyReducer
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.UserMessage
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.yield

internal class KeyScreenViewModel(
    private val reader: SettingsReader,
    private val writer: SettingsWriter,
    private val navigator: SettingsNavigator,
) : ScreenViewModel<KeyState, KeyAction>() {
    init {
        observe()
    }

    override fun createInitialState(): KeyState = KeyReducer.initial()

    override fun onAction(action: KeyAction) {
        when (action) {
            is KeyAction.Save -> save(action.key)
            KeyAction.Delete -> delete()
            KeyAction.OpenConsent -> navigator.openConsent()
            KeyAction.Retry -> observe()
            KeyAction.DismissMessage -> reduce { KeyReducer.run { withMessage(null) } }
            KeyAction.DismissSheet -> reduce { KeyReducer.run { withSheet(null) } }
        }
    }

    private fun observe() {
        launchCatching(onError = { error -> reduce { KeyReducer.run { withError(error) } } }) {
            yield()
            combine(reader.observeConsent(), reader.observeKeySaved()) { granted, saved ->
                KeyContent(saved = saved, granted = granted)
            }.collect { content ->
                reduce { KeyReducer.run { withContent(content) } }
            }
        }
    }

    private fun save(key: String) {
        launchCatching(onError = { error ->
            reduce { KeyReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            val message = when (writer.saveKey(key)) {
                KeySave.Saved -> KEY_SAVED
                KeySave.NeedsConsent -> NEEDS_CONSENT
                KeySave.Rejected -> KEY_REJECTED
            }
            reduce { KeyReducer.run { withMessage(UserMessage(message)) } }
        }
    }

    private fun delete() {
        launchCatching(onError = { error ->
            reduce { KeyReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            writer.deleteKey()
            reduce { KeyReducer.run { withMessage(UserMessage(KEY_DELETED)) } }
        }
    }

    private companion object {
        const val KEY_SAVED = "Gemini key saved."
        const val NEEDS_CONSENT = "Agree before a Gemini key can be saved."
        const val KEY_REJECTED = "That key cannot be saved."
        const val KEY_DELETED = "Gemini key deleted."
    }
}
