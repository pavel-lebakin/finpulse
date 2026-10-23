package com.lebakin.finpulse.feature.assistant.domain

import com.lebakin.finpulse.core.ai.ByokFailed
import com.lebakin.finpulse.core.ai.OnDeviceFailed
import com.lebakin.finpulse.core.ai.ReceiptCheck
import com.lebakin.finpulse.core.ai.checkReceiptReply
import com.lebakin.finpulse.core.ai.phrasePrompt
import kotlin.coroutines.cancellation.CancellationException

internal fun interface PhraseModel {
    suspend fun complete(prompt: String): String
}

internal sealed interface PhraseResult {
    data class Draft(val draft: PhraseDraft) : PhraseResult

    data object MonthSpend : PhraseResult

    data object UnsupportedQuestion : PhraseResult
}

// A known question never opens a model. A rule draft does not open BYOK.
internal class PhraseEngines(
    private val rules: PhraseRules,
    private val onDevice: suspend () -> PhraseModel?,
    private val byok: suspend () -> PhraseModel?,
) {
    suspend fun read(phrase: String): PhraseResult {
        val trimmed = phrase.trim()
        if (trimmed.isEmpty()) {
            throw PhraseUnreadable(PhraseUnreadable.UNREADABLE)
        }
        return when (val ruled = rules.read(trimmed)) {
            PhraseRead.MonthSpend -> PhraseResult.MonthSpend
            PhraseRead.UnsupportedQuestion -> PhraseResult.UnsupportedQuestion
            is PhraseRead.Draft -> PhraseResult.Draft(ruled.draft)
            null -> PhraseResult.Draft(modelDraft(trimmed))
        }
    }

    private suspend fun modelDraft(phrase: String): PhraseDraft {
        if (phrase.none(Char::isDigit)) {
            throw PhraseUnreadable(PhraseUnreadable.UNREADABLE)
        }
        val draft = draftOrNull(onDevice, phrase) ?: draftOrNull(byok, phrase)
        return draft ?: throw PhraseUnreadable(PhraseUnreadable.UNREADABLE)
    }

    private suspend fun draftOrNull(
        open: suspend () -> PhraseModel?,
        phrase: String,
    ): PhraseDraft? {
        val prompt = promptOrNull(phrase) ?: return null
        val reply = open()?.let { model -> replyOrNull(model, prompt) }
        return reply?.let(::acceptedDraft)
    }

    // The phrase and the model reply are not logged.
    private suspend fun replyOrNull(model: PhraseModel, prompt: String): String? = try {
        model.complete(prompt)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: OnDeviceFailed) {
        null
    } catch (_: ByokFailed) {
        null
    }

    private fun promptOrNull(phrase: String): String? = try {
        phrasePrompt(phrase)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun acceptedDraft(reply: String): PhraseDraft? = when (val check = checkReceiptReply(reply)) {
        is ReceiptCheck.Accepted -> PhraseDraft(
            merchant = check.reply.merchant,
            amountMinor = check.reply.amountMinor,
            currencyCode = check.reply.currencyCode,
            occurredOn = check.reply.occurredOn,
            categoryName = check.reply.category.label,
        )
        ReceiptCheck.Rejected -> null
    }
}
