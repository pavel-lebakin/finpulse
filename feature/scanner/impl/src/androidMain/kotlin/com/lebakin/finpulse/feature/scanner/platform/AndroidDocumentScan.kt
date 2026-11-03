package com.lebakin.finpulse.feature.scanner.platform

import android.app.Activity
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage

private const val PAGE_LIMIT = 3
private const val JPEG_QUALITY = 85

internal fun startDocumentScan(activity: ComponentActivity): Task<IntentSender> {
    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(PAGE_LIMIT)
        .setResultFormats(RESULT_FORMAT_JPEG)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()
    return GmsDocumentScanning.getClient(options).getStartScanIntent(activity)
}

internal fun ActivityResult.toScanOutcome(activity: ComponentActivity): CaptureOutcome {
    val pages = if (resultCode == Activity.RESULT_OK) {
        GmsDocumentScanningResult.fromActivityResultIntent(data)?.pages.orEmpty()
    } else {
        emptyList()
    }
    val bitmaps = pages.mapNotNull { page -> decodePage(activity, page.imageUri) }
    val bytes = stitchPages(bitmaps)
    bitmaps.forEach { bitmap -> bitmap.recycle() }
    return if (bytes == null || bytes.isEmpty()) CaptureOutcome.Cancelled else CaptureOutcome.Image(ReceiptImage(bytes))
}

private fun decodePage(activity: ComponentActivity, uri: Uri): Bitmap? =
    activity.contentResolver.openInputStream(uri)?.use { stream -> BitmapFactory.decodeStream(stream) }

private fun stitchPages(pages: List<Bitmap>): ByteArray? {
    val first = pages.firstOrNull()
    val joined = if (first == null || pages.size == 1) {
        null
    } else {
        val width = pages.maxOf { page -> page.width }
        val height = pages.sumOf { page -> page.height }
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            var top = 0f
            pages.forEach { page ->
                canvas.drawBitmap(page, 0f, top, null)
                top += page.height
            }
        }
    }
    val bytes = when {
        first == null -> null
        joined == null -> first.toJpegBytes()
        else -> joined.toJpegBytes()
    }
    joined?.recycle()
    return bytes
}

private fun Bitmap.toJpegBytes(): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
    return stream.toByteArray()
}
