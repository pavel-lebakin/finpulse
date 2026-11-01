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
import kotlin.math.abs

private const val TOP_CANDIDATE = 1UL
private const val ROW_FRACTION = 0.6
private const val MIN_ROW_GAP = 0.012
private const val MIN_TEXT_HEIGHT = 0.01f

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
        request.usesLanguageCorrection = false
        request.minimumTextHeight = MIN_TEXT_HEIGHT
        request.automaticallyDetectsLanguage = true
        val handler = VNImageRequestHandler(image.bytes.toNSData(), emptyMap<Any?, Any?>())
        val performed = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            handler.performRequests(listOf(request), error.ptr)
        }
        if (!performed) {
            return null
        }
        val pieces = request.results.orEmpty().mapNotNull { result ->
            val observation = result as? VNRecognizedTextObservation ?: return@mapNotNull null
            val text = observation.topCandidates(TOP_CANDIDATE).firstOrNull() as? VNRecognizedText
            text?.string?.let { value -> piece(observation, value) }
        }
        return rows(pieces).joinToString(separator = "\n").trim().ifEmpty { null }
    }
}

private data class TextPiece(
    val text: String,
    val midY: Double,
    val minX: Double,
    val height: Double,
)

@OptIn(ExperimentalForeignApi::class)
private fun piece(observation: VNRecognizedTextObservation, text: String): TextPiece =
    observation.boundingBox.useContents {
        TextPiece(
            text = text,
            midY = origin.y + size.height / 2.0,
            minX = origin.x,
            height = size.height,
        )
    }

private fun rows(pieces: List<TextPiece>): List<String> {
    val groups = mutableListOf<MutableList<TextPiece>>()
    pieces.sortedByDescending { it.midY }.forEach { piece ->
        val group = groups.lastOrNull()
        val anchor = group?.firstOrNull()
        if (group == null || anchor == null || abs(anchor.midY - piece.midY) > rowGap(anchor, piece)) {
            groups += mutableListOf(piece)
        } else {
            group += piece
        }
    }
    return groups.map { row ->
        row.sortedBy { it.minX }.joinToString(separator = " ") { it.text.trim() }.trim()
    }.filter { it.isNotEmpty() }
}

private fun rowGap(anchor: TextPiece, piece: TextPiece): Double {
    val height = maxOf(anchor.height, piece.height)
    return maxOf(height * ROW_FRACTION, MIN_ROW_GAP)
}
