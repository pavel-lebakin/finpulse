package com.lebakin.finpulse.core.ai

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlin.coroutines.cancellation.CancellationException

private object GeminiNanoModel : OnDeviceModel {
    override suspend fun complete(prompt: String): String {
        require(prompt.isNotBlank()) { "Prompt is empty." }
        val client = Generation.getClient()
        try {
            return readReply(client, prompt)
        } finally {
            client.close()
        }
    }
}

private suspend fun generatedText(client: GenerativeModel, prompt: String): String {
    if (client.checkStatus() != FeatureStatus.AVAILABLE) {
        return ""
    }
    return client.generateContent(prompt).candidates.firstOrNull()?.text?.trim().orEmpty()
}

private suspend fun readReply(client: GenerativeModel, prompt: String): String {
    val failure = try {
        val text = generatedText(client, prompt)
        if (text.isEmpty()) OnDeviceFailed() else return text
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: GenAiException) {
        error
    }
    throw if (failure is OnDeviceFailed) failure else OnDeviceFailed(failure)
}

private suspend fun engineReady(): Boolean {
    val client = Generation.getClient()
    try {
        return client.checkStatus() == FeatureStatus.AVAILABLE
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: GenAiException) {
        return false
    } finally {
        client.close()
    }
}

actual suspend fun openOnDeviceModel(): OnDeviceModel? = onDeviceModelFor(engineReady(), GeminiNanoModel)
