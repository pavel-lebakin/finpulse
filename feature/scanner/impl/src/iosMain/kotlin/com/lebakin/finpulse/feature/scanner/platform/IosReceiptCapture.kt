package com.lebakin.finpulse.feature.scanner.platform

import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

private const val JPEG_QUALITY = 0.85

@OptIn(ExperimentalForeignApi::class)
internal class IosReceiptCapture : ReceiptCapture {
    private var pickerDelegate: NSObject? = null

    override suspend fun takePhoto(): CaptureOutcome = pick(
        source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
        camera = true,
    )

    override suspend fun pickFromGallery(): CaptureOutcome = pick(
        source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
        camera = false,
    )

    private suspend fun pick(
        source: UIImagePickerControllerSourceType,
        camera: Boolean,
    ): CaptureOutcome = suspendCancellableCoroutine { continuation ->
        if (camera && !UIImagePickerController.isSourceTypeAvailable(source)) {
            continuation.resume(CaptureOutcome.Unavailable)
            return@suspendCancellableCoroutine
        }
        val host = foregroundController()
        if (host == null) {
            continuation.resume(CaptureOutcome.Unavailable)
            return@suspendCancellableCoroutine
        }
        val picker = UIImagePickerController()
        picker.sourceType = source
        val delegate = PickerDelegate { image ->
            picker.dismissViewControllerAnimated(true, null)
            pickerDelegate = null
            val outcome = image.toOutcome()
            if (continuation.isActive) {
                continuation.resume(outcome)
            }
        }
        pickerDelegate = delegate
        picker.delegate = delegate
        continuation.invokeOnCancellation {
            pickerDelegate = null
            picker.dismissViewControllerAnimated(true, null)
        }
        host.presentViewController(picker, true, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate(
    private val onImage: (UIImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        onImage(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onImage(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun foregroundController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val scene = application.connectedScenes.mapNotNull { it as? UIWindowScene }.firstOrNull { windowScene ->
        windowScene.activationState == UISceneActivationStateForegroundActive
    } ?: application.connectedScenes.mapNotNull { it as? UIWindowScene }.firstOrNull()
    val window = scene?.keyWindow ?: scene?.windows?.filterIsInstance<UIWindow>()?.firstOrNull()
    var controller = window?.rootViewController ?: return null
    while (controller.presentedViewController != null) {
        val presented = controller.presentedViewController ?: break
        controller = presented
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage?.toOutcome(): CaptureOutcome {
    val bytes = this?.let { image -> UIImageJPEGRepresentation(image, JPEG_QUALITY)?.toByteArray() }
    return if (bytes == null || bytes.isEmpty()) {
        CaptureOutcome.Cancelled
    } else {
        CaptureOutcome.Image(ReceiptImage(bytes))
    }
}
