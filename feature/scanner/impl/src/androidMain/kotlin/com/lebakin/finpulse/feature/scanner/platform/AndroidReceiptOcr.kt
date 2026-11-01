package com.lebakin.finpulse.feature.scanner.platform

import android.graphics.BitmapFactory
import android.media.ExifInterface
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private const val ROTATE_90 = 90
private const val ROTATE_180 = 180
private const val ROTATE_270 = 270

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
            recognizer.process(InputImage.fromBitmap(bitmap, rotationDegrees(bytes))).await().text
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: MlKitException) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE, failure)
        }
    }
}


private fun rotationDegrees(bytes: ByteArray): Int = try {
    val orientation = ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
    )
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> ROTATE_90
        ExifInterface.ORIENTATION_ROTATE_180 -> ROTATE_180
        ExifInterface.ORIENTATION_ROTATE_270 -> ROTATE_270
        else -> 0
    }
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: IOException) {
    0
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
