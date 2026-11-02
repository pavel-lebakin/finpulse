package com.lebakin.finpulse.feature.scanner.domain

import com.lebakin.finpulse.core.ai.ByokFailed
import com.lebakin.finpulse.core.ai.OnDeviceFailed
import com.lebakin.finpulse.core.ai.ReceiptCheck
import com.lebakin.finpulse.core.ai.checkReceiptReply
import com.lebakin.finpulse.core.ai.receiptPrompt
import kotlin.coroutines.cancellation.CancellationException

internal fun interface ReceiptModel {
    suspend fun complete(prompt: String): String
}

// On-device reads every receipt. Rules stay when they list at least as many items.
internal class ReceiptEngines(
    private val rules: ReceiptParser,
    private val onDevice: suspend () -> ReceiptModel?,
    private val byok: suspend () -> ReceiptModel?,
) {
    suspend fun parse(text: String): ReceiptDraft {
        val ruled = ruledOrNull(text)
        val modeled = draftOrNull(onDevice, text)
        val chosen = preferReceipt(ruled, modeled)
        if (chosen != null) {
            return chosen
        }
        return draftOrNull(byok, text) ?: throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
    }

    private fun ruledOrNull(text: String): ReceiptDraft? = try {
        rules.parse(text)
    } catch (_: ReceiptUnreadable) {
        null
    }

    private suspend fun draftOrNull(
        open: suspend () -> ReceiptModel?,
        text: String,
    ): ReceiptDraft? {
        val prompt = promptOrNull(text) ?: return null
        val reply = open()?.let { model -> replyOrNull(model, prompt) }
        return reply?.let(::acceptedDraft)
    }

    // Receipt text and the model reply are not logged.
    private suspend fun replyOrNull(model: ReceiptModel, prompt: String): String? = try {
        model.complete(prompt)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: OnDeviceFailed) {
        null
    } catch (_: ByokFailed) {
        null
    }

    private fun promptOrNull(text: String): String? = try {
        receiptPrompt(text)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun acceptedDraft(reply: String): ReceiptDraft? = when (val check = checkReceiptReply(reply)) {
        is ReceiptCheck.Accepted -> ReceiptDraft(
            merchant = check.reply.merchant,
            amountMinor = check.reply.amountMinor,
            currencyCode = check.reply.currencyCode,
            occurredOn = check.reply.occurredOn,
            categoryName = check.reply.category.label,
            lines = check.reply.lines.map { line ->
                ReceiptLine(name = line.name, amountMinor = line.amountMinor, currencyCode = check.reply.currencyCode)
            },
        )
        ReceiptCheck.Rejected -> null
    }
}

private fun preferReceipt(ruled: ReceiptDraft?, modeled: ReceiptDraft?): ReceiptDraft? = when {
    modeled != null && modeled.lines.size > (ruled?.lines?.size ?: -1) -> modeled
    ruled != null -> ruled
    else -> modeled
}
