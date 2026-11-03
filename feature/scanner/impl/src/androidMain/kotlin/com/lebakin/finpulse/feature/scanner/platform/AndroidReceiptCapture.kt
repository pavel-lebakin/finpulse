package com.lebakin.finpulse.feature.scanner.platform

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.context.GlobalContext

private const val FILE_PROVIDER = ".files"
private const val CAPTURE_DIRECTORY = "receipts"
private const val CAPTURE_FILE = "capture.jpg"

internal class AndroidReceiptCapture : ReceiptCapture {
    private var activity: ComponentActivity? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var scanLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pending: CancellableContinuation<CaptureOutcome>? = null
    private var pendingFile: File? = null

    fun attach(host: ComponentActivity) {
        activity = host
        permissionLauncher = host.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraFile()
            } else {
                finish(CaptureOutcome.Denied)
            }
        }
        scanLauncher = host.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            finish(result.toScanOutcome(host))
        }
        cameraLauncher = host.registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val file = pendingFile
            pendingFile = null
            finish(if (saved && file != null) file.toOutcome() else CaptureOutcome.Cancelled)
        }
        galleryLauncher = host.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            finish(uri.toOutcome())
        }
    }

    override suspend fun takePhoto(): CaptureOutcome = suspendCancellableCoroutine { continuation ->
        arm(continuation)
        val host = activity
        val scan = scanLauncher
        if (host == null || scan == null) {
            finish(CaptureOutcome.Unavailable)
            return@suspendCancellableCoroutine
        }
        startDocumentScan(host)
            .addOnSuccessListener { sender ->
                scan.launch(IntentSenderRequest.Builder(sender).build())
            }
            .addOnFailureListener {
                launchFullCamera()
            }
    }

    private fun launchFullCamera() {
        val host = activity
        val granted = host?.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        when {
            host == null || cameraLauncher == null || permissionLauncher == null -> finish(CaptureOutcome.Unavailable)
            granted -> launchCameraFile()
            else -> permissionLauncher?.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraFile() {
        val host = activity
        val camera = cameraLauncher
        if (host == null || camera == null) {
            finish(CaptureOutcome.Unavailable)
            return
        }
        val directory = File(host.cacheDir, CAPTURE_DIRECTORY)
        directory.mkdirs()
        val file = File(directory, CAPTURE_FILE)
        pendingFile = file
        val uri = FileProvider.getUriForFile(host, host.packageName + FILE_PROVIDER, file)
        camera.launch(uri)
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

    private fun File.toOutcome(): CaptureOutcome {
        val bytes = if (isFile) readBytes() else ByteArray(0)
        delete()
        return if (bytes.isEmpty()) CaptureOutcome.Cancelled else CaptureOutcome.Image(ReceiptImage(bytes))
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

fun attachAndroidReceiptCapture(activity: ComponentActivity) {
    GlobalContext.get().get<AndroidReceiptCapture>().attach(activity)
}
