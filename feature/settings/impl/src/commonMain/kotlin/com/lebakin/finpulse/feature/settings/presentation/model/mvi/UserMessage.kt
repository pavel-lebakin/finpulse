package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError

data class UserMessage(
    val text: String,
)

internal fun AppError.toUserMessage(): UserMessage = UserMessage(
    text = when (this) {
        is AppError.NoConnection -> "No connection."
        is AppError.Server -> "The server could not be reached."
        is AppError.Unexpected -> "Something went wrong."
    },
)
