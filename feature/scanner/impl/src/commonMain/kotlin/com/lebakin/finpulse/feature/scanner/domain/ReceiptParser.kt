package com.lebakin.finpulse.feature.scanner.domain

private val groceries = listOf("market", "grocery", "groceries", "supermarket")
private val transport = listOf("metro", "taxi", "fuel", "petrol", "uber", "bus")
private val eatingOut = listOf("cafe", "coffee", "restaurant")

internal class ReceiptParser(
    private val clock: ReceiptClock,
) {
    fun parse(text: String): ReceiptDraft {
        val lines = receiptLines(text)
        val items = findItems(lines)
        val amount = findAmount(lines, items)?.let { token -> withLanguageCurrency(token, text) }
        val merchant = findMerchant(lines)
        if (lines.isEmpty() || amount == null || merchant == null) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
        }
        return ReceiptDraft(
            merchant = merchant,
            amountMinor = amount.amountMinor,
            currencyCode = amount.currencyCode,
            occurredOn = findDate(lines) ?: clock.today(),
            categoryName = categoryName(merchant, text),
            lines = items,
        )
    }
}

internal fun findMerchant(lines: List<String>): String? {
    val candidates = lines.filter { line -> !isSummaryLine(line) && labelOf(line) != null }
    val line = candidates.firstOrNull { monies(it).isEmpty() } ?: candidates.firstOrNull() ?: return null
    return labelOf(line)
}

private fun categoryName(merchant: String, text: String): String {
    val haystack = "$merchant\n$text"
    return when {
        containsWord(haystack, groceries) -> "Groceries"
        containsWord(haystack, transport) -> "Transport"
        containsWord(haystack, eatingOut) -> "Eating out"
        else -> "General"
    }
}

private fun containsWord(haystack: String, words: List<String>): Boolean =
    words.any { word -> Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(haystack) }
