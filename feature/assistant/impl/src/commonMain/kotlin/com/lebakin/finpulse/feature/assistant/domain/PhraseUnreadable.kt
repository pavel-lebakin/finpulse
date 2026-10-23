package com.lebakin.finpulse.feature.assistant.domain

internal class PhraseUnreadable(
    message: String,
) : Exception(message) {
    internal companion object {
        const val UNREADABLE = "That phrase could not be read."
    }
}
