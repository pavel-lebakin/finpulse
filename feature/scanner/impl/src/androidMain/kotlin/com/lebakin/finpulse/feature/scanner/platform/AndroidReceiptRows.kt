package com.lebakin.finpulse.feature.scanner.platform

import com.google.mlkit.vision.text.Text
import kotlin.math.abs

private const val ROW_FRACTION = 0.6
private const val MIN_ROW_GAP = 8.0

internal fun textRows(text: Text): String {
    val pieces = text.textBlocks.flatMap { block ->
        block.lines.mapNotNull { line ->
            val box = line.boundingBox
            if (box == null) {
                null
            } else {
                TextPiece(
                    text = line.text,
                    midY = box.exactCenterY().toDouble(),
                    minX = box.left.toDouble(),
                    height = box.height().toDouble(),
                )
            }
        }
    }
    val joined = if (pieces.isEmpty()) text.text else rows(pieces).joinToString(separator = "\n")
    return joined.trim()
}

private data class TextPiece(
    val text: String,
    val midY: Double,
    val minX: Double,
    val height: Double,
)

private fun rows(pieces: List<TextPiece>): List<String> {
    val groups = mutableListOf<MutableList<TextPiece>>()
    pieces.sortedBy { it.midY }.forEach { piece ->
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
