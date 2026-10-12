package com.lebakin.finpulse.feature.scanner.domain

private const val MERCHANT_LIMIT = 80
private const val MIN_MERCHANT_LETTERS = 2

private val groceries = listOf("market", "grocery", "groceries", "supermarket")
private val transport = listOf("metro", "taxi", "fuel", "petrol", "uber", "bus")
private val eatingOut = listOf("cafe", "coffee", "restaurant")

internal class ReceiptParser(
    private val clock: ReceiptClock,
) {
    fun parse(text: String): ReceiptDraft {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val amount = findAmount(lines)
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
        )
    }
}

internal fun findMerchant(lines: List<String>): String? {
    val line = lines.firstOrNull { isMerchantLine(it) } ?: return null
    return line.trim().take(MERCHANT_LIMIT)
}

private fun isMerchantLine(line: String): Boolean {
    if (isTotalLine(line)) {
        return false
    }
    val stripped = dateShape.replace(withoutMoney(line), " ")
    return stripped.count { it.isLetter() } >= MIN_MERCHANT_LETTERS
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
