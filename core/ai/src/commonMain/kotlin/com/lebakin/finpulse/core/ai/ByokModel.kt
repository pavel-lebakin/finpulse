package com.lebakin.finpulse.core.ai

import com.lebakin.finpulse.core.network.HttpsClient
import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.security.SecretStore
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ByokModel {
    suspend fun complete(prompt: String): String
}

class ByokFailed(cause: Throwable? = null) : Exception("BYOK model failed", cause)

internal const val GEMINI_GENERATE_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"

internal const val BYOK_API_KEY_HEADER = "x-goog-api-key"

private const val MAX_KEY_CHARS = 512
private const val CONTENT_TYPE = "Content-Type"
private const val JSON = "application/json"

private val parser = Json

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
suspend fun openByokModel(
    store: SecretStore,
    client: HttpsClient,
    consent: ConsentPreferences,
): ByokModel? {
    if (!consent.isGranted()) return null
    val key = store.read()?.takeIf(::acceptedByokKey)
    return if (key == null) null else GeminiByokModel(store, client, consent)
}

private fun acceptedByokKey(key: String): Boolean {
    if (key.length !in 1..MAX_KEY_CHARS) return false
    return key.all { it in '!'..'~' }
}

private class GeminiByokModel(
    private val store: SecretStore,
    private val client: HttpsClient,
    private val consent: ConsentPreferences,
) : ByokModel {
    override suspend fun complete(prompt: String): String {
        require(prompt.isNotBlank()) { "Prompt is empty." }
        val key = currentKey()?.takeIf { consent.isGranted() } ?: throw ByokFailed()
        val reply = client.post(
            url = GEMINI_GENERATE_URL,
            body = geminiRequestBody(prompt),
            headers = mapOf(
                CONTENT_TYPE to JSON,
                BYOK_API_KEY_HEADER to key,
            ),
        )
        return replyText(reply.body) ?: throw ByokFailed()
    }

    private suspend fun currentKey(): String? = store.read()?.takeIf(::acceptedByokKey)
}

private fun geminiRequestBody(prompt: String): String = buildJsonObject {
    put(
        "contents",
        buildJsonArray {
            add(
                buildJsonObject {
                    put(
                        "parts",
                        buildJsonArray {
                            add(buildJsonObject { put("text", prompt) })
                        },
                    )
                },
            )
        },
    )
}.toString()

private fun replyText(body: String): String? {
    val text = candidateText(parseObject(body)).trim()
    return text.ifEmpty { null }
}

private fun parseObject(body: String): JsonObject? = try {
    parser.parseToJsonElement(body) as? JsonObject
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}

private fun candidateText(root: JsonObject?): String {
    val candidate = (root?.get("candidates") as? JsonArray)?.firstOrNull() as? JsonObject
    val content = candidate?.get("content") as? JsonObject
    val parts = content?.get("parts") as? JsonArray
    return parts?.mapNotNull(::partText)?.joinToString(separator = "").orEmpty()
}

private fun partText(part: JsonElement): String? {
    val obj = part as? JsonObject ?: return null
    val text = obj["text"] as? JsonPrimitive
    return if (text != null && text.isString && !isThought(obj)) text.content else null
}

private fun isThought(obj: JsonObject): Boolean {
    val flag = obj["thought"] as? JsonPrimitive ?: return false
    return flag.booleanOrNull == true
}
