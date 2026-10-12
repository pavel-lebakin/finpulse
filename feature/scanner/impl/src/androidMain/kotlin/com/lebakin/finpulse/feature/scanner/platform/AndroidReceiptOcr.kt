package com.lebakin.finpulse.feature.scanner.platform

import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal class AndroidReceiptOcr : ReceiptOcr {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun read(image: ReceiptImage): String {
        val text = recognize(image.bytes)
        if (text.isNullOrBlank()) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
        }
        return text
    }

    private suspend fun recognize(bytes: ByteArray): String? {
        val bitmap = if (bytes.isEmpty()) {
            null
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        if (bitmap == null) {
            return null
        }
        return try {
            recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: MlKitException) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE, failure)
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value ->
        if (continuation.isActive) {
            continuation.resume(value)
        }
    }
    addOnFailureListener { error ->
        if (continuation.isActive) {
            continuation.resumeWithException(error)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}
