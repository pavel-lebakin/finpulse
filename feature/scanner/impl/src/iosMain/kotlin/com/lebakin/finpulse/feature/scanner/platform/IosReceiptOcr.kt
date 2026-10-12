package com.lebakin.finpulse.feature.scanner.platform

import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import platform.Foundation.NSError
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate

private const val TOP_CANDIDATE = 1UL

@OptIn(ExperimentalForeignApi::class)
internal class IosReceiptOcr : ReceiptOcr {
    override suspend fun read(image: ReceiptImage): String {
        val text = if (image.bytes.isEmpty()) null else recognize(image)
        if (text.isNullOrBlank()) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
        }
        return text
    }

    private fun recognize(image: ReceiptImage): String? {
        val request = VNRecognizeTextRequest()
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        val handler = VNImageRequestHandler(image.bytes.toNSData(), emptyMap<Any?, Any?>())
        val performed = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            handler.performRequests(listOf(request), error.ptr)
        }
        if (!performed) {
            return null
        }
        val observations = request.results.orEmpty().mapNotNull { it as? VNRecognizedTextObservation }
        return observations
            .sortedByDescending { observation -> observation.boundingBox.useContents { origin.y } }
            .mapNotNull { observation ->
                val candidate = observation.topCandidates(TOP_CANDIDATE).firstOrNull() as? VNRecognizedText
                candidate?.string
            }
            .joinToString(separator = "\n")
            .trim()
            .ifEmpty { null }
    }
}
