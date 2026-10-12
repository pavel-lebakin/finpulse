package com.lebakin.finpulse.feature.scanner.domain

internal class ReceiptUnreadable(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    internal companion object {
        const val UNREADABLE = "The receipt could not be read."
        const val UNAVAILABLE = "Text recognition is not available on this device."
        const val CAMERA_DENIED = "Camera permission is required."
        const val CAMERA_UNAVAILABLE = "The camera is not available."
    }
}
