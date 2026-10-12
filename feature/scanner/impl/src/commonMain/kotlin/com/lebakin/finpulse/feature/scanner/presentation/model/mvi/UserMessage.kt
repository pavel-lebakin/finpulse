package com.lebakin.finpulse.feature.scanner.presentation.model.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable

data class UserMessage(
    val text: String,
)

internal fun AppError.toUserMessage(): UserMessage = UserMessage(
    text = when (this) {
        is AppError.NoConnection -> "No connection."
        is AppError.Server -> "The server could not be reached."
        is AppError.Unexpected -> when (val thrown = cause) {
            is ReceiptUnreadable -> thrown.message ?: ReceiptUnreadable.UNREADABLE
            else -> "Something went wrong."
        }
    },
)
