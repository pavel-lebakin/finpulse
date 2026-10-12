package com.lebakin.finpulse.feature.scanner.domain

class ReceiptImage(
    val bytes: ByteArray,
)

sealed interface CaptureOutcome {
    data class Image(val image: ReceiptImage) : CaptureOutcome

    data object Cancelled : CaptureOutcome

    data object Denied : CaptureOutcome

    data object Unavailable : CaptureOutcome
}

internal interface ReceiptCapture {
    suspend fun takePhoto(): CaptureOutcome

    suspend fun pickFromGallery(): CaptureOutcome
}

internal const val MAX_RECEIPT_BYTES = 8 * 1024 * 1024
