package com.lebakin.finpulse.feature.scanner.platform

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.context.GlobalContext

private const val JPEG_QUALITY = 85

internal class AndroidReceiptCapture : ReceiptCapture {
    private var activity: ComponentActivity? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pending: CancellableContinuation<CaptureOutcome>? = null

    fun attach(host: ComponentActivity) {
        activity = host
        permissionLauncher = host.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraLauncher?.launch(null)
            } else {
                finish(CaptureOutcome.Denied)
            }
        }
        cameraLauncher = host.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            finish(bitmap.toOutcome())
        }
        galleryLauncher = host.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            finish(uri.toOutcome())
        }
    }

    override suspend fun takePhoto(): CaptureOutcome = suspendCancellableCoroutine { continuation ->
        arm(continuation)
        val host = activity
        val permissions = permissionLauncher
        val camera = cameraLauncher
        if (host == null || permissions == null || camera == null) {
            finish(CaptureOutcome.Unavailable)
            return@suspendCancellableCoroutine
        }
        val granted = host.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            camera.launch(null)
        } else {
            permissions.launch(Manifest.permission.CAMERA)
        }
    }

    override suspend fun pickFromGallery(): CaptureOutcome = suspendCancellableCoroutine { continuation ->
        arm(continuation)
        val gallery = galleryLauncher
        if (gallery == null) {
            finish(CaptureOutcome.Unavailable)
            return@suspendCancellableCoroutine
        }
        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun arm(continuation: CancellableContinuation<CaptureOutcome>) {
        pending = continuation
        continuation.invokeOnCancellation { pending = null }
    }

    private fun finish(outcome: CaptureOutcome) {
        val current = pending ?: return
        pending = null
        if (current.isActive) {
            current.resume(outcome)
        }
    }

    private fun Bitmap?.toOutcome(): CaptureOutcome {
        val bytes = this?.toJpegBytes()
        return if (bytes == null) CaptureOutcome.Cancelled else CaptureOutcome.Image(ReceiptImage(bytes))
    }

    private fun Uri?.toOutcome(): CaptureOutcome {
        val host = activity
        val bytes = if (this == null || host == null) {
            null
        } else {
            host.contentResolver.openInputStream(this)?.use { stream -> stream.readBytes() }
        }
        val missingHost = this != null && host == null
        return when {
            missingHost -> CaptureOutcome.Unavailable
            bytes == null || bytes.isEmpty() -> CaptureOutcome.Cancelled
            else -> CaptureOutcome.Image(ReceiptImage(bytes))
        }
    }
}

private fun Bitmap.toJpegBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
    return stream.toByteArray()
}

fun attachAndroidReceiptCapture(activity: ComponentActivity) {
    GlobalContext.get().get<AndroidReceiptCapture>().attach(activity)
}
