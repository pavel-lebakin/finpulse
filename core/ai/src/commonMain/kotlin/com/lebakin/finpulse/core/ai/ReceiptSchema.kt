package com.lebakin.finpulse.core.ai

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val parser = Json

fun checkReceiptReply(text: String): ReceiptCheck {
    val reply = readReply(text)
    return if (reply == null) ReceiptCheck.Rejected else ReceiptCheck.Accepted(reply)
}

private fun readReply(text: String): ReceiptReply? {
    val obj = jsonObject(text) ?: return null
    return replyOf(obj)
}

private fun jsonObject(text: String): JsonObject? = try {
    val trimmed = text.trim()
    if (trimmed.isEmpty() || repeatsTopLevelKey(trimmed)) {
        null
    } else {
        parser.parseToJsonElement(trimmed) as? JsonObject
    }
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}
