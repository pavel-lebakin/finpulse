package com.lebakin.finpulse.feature.assistant.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.feature.assistant.domain.PhraseUnreadable

data class UserMessage(
    val text: String,
)

internal fun AppError.toUserMessage(): UserMessage = UserMessage(
    text = when (this) {
        is AppError.NoConnection -> "No connection."
        is AppError.Server -> "The server could not be reached."
        is AppError.Unexpected -> when (val thrown = cause) {
            is PhraseUnreadable -> thrown.message ?: PhraseUnreadable.UNREADABLE
            else -> "Something went wrong."
        }
    },
)
