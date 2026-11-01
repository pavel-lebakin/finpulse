package com.lebakin.finpulse.feature.scanner.domain

private const val MINOR_PER_MAJOR = 100L
private const val CODE_BEFORE = 1
private const val SYMBOL_BEFORE = 2
private const val AMOUNT = 3
private const val CODE_AFTER = 4
private const val SYMBOL_AFTER = 5
private const val WHOLE = 3
private const val WHOLE_AFTER_AMOUNT = 1
private const val WHOLE_AFTER_CODE = 2
private const val WHOLE_AFTER_SYMBOL = 3
private const val SPLIT_WHOLE = 1
private const val SPLIT_FRACTION = 2
private const val SPACED_DIGIT = 1
private const val SPACED_SEPARATOR = 2
private const val SPACED_FRACTION = 3
private const val FRACTION_DIGITS = 2
private const val DEFAULT_CURRENCY = "USD"

private const val CURRENCY_CODE = "USD|EUR|GBP|RUB|[Рр][Уу][Бб]\\.?"
private const val CURRENCY_SYMBOL = "[$€£₽]"

private val decimalPattern = Regex(
    "(?i)(?<![\\d.,])" +
        "(?:($CURRENCY_CODE)|($CURRENCY_SYMBOL))?\\s*" +
        "(\\d{1,3}(?:[ .]\\d{3})+,\\d{2}|\\d{1,3}(?:[ ,]\\d{3})+\\.\\d{2}|\\d{1,7}[.,]\\d{2})" +
        "\\s*(?:($CURRENCY_CODE)|($CURRENCY_SYMBOL))?(?![\\d.,])",
)

private val wholeBefore = Regex(
    "(?i)(?<![\\d.,])(?:($CURRENCY_CODE)|($CURRENCY_SYMBOL))\\s*(\\d{1,7})(?![\\d.,])",
)

private val wholeAfter = Regex(
    "(?i)(?<![\\d.,])(\\d{1,7})\\s*((?:$CURRENCY_CODE)|($CURRENCY_SYMBOL))(?!\\d)",
)

private val splitPrice = Regex("(?<![\\d.,])(\\d{1,5})\\s+(\\d{2})\\s*$")

private val spacedDecimal = Regex("(\\d)\\s*([.,])\\s*(\\d{2})(?!\\d)")

private val ocrZero = Regex("(?<=\\d)[Oo](?!\\p{L})")

private val symbols = mapOf(
    "$" to "USD",
    "€" to "EUR",
    "£" to "GBP",
    "₽" to "RUB",
)

internal fun repairLine(line: String): String = spacedDecimal.replace(ocrZero.replace(line, "0")) { match ->
    match.groupValues[SPACED_DIGIT] + match.groupValues[SPACED_SEPARATOR] + match.groupValues[SPACED_FRACTION]
}

internal fun monies(line: String): List<MoneyToken> {
    val cleaned = dateShape.replace(repairLine(line), " ")
    val decimal = decimalPattern.findAll(cleaned).mapNotNull { match ->
        token(
            minorOf(match.groupValues[AMOUNT]),
            currency(match),
            match.groupValues[CODE_BEFORE].isNotEmpty() ||
                match.groupValues[SYMBOL_BEFORE].isNotEmpty() ||
                match.groupValues[CODE_AFTER].isNotEmpty() ||
                match.groupValues[SYMBOL_AFTER].isNotEmpty(),
        )
    }.toList()
    val whole = if (decimal.isEmpty()) wholeAmounts(cleaned) else emptyList()
    val split = if (decimal.isEmpty() && whole.isEmpty()) listOfNotNull(splitAmount(cleaned)) else emptyList()
    return decimal.ifEmpty { whole.ifEmpty { split } }
}

internal fun withoutMoney(line: String): String {
    var text = dateShape.replace(repairLine(line), " ")
    text = decimalPattern.replace(text, " ")
    text = wholeBefore.replace(text, " ")
    text = wholeAfter.replace(text, " ")
    return splitPrice.replace(text, " ")
}

private fun wholeAmounts(line: String): List<MoneyToken> {
    val before = wholeBefore.findAll(line).mapNotNull { match ->
        token(minorOf(match.groupValues[WHOLE]), currencyBefore(match), explicit = true)
    }
    val after = wholeAfter.findAll(line).mapNotNull { match ->
        token(minorOf(match.groupValues[WHOLE_AFTER_AMOUNT]), currencyAfter(match), explicit = true)
    }
    return (before + after).toList()
}

private fun splitAmount(line: String): MoneyToken? {
    val match = if (line.count(Char::isLetter) < MIN_ITEM_LETTERS) null else splitPrice.find(line)
    val minor = match?.let { found ->
        found.groupValues[SPLIT_WHOLE].toLong() * MINOR_PER_MAJOR + found.groupValues[SPLIT_FRACTION].toLong()
    }
    return token(minor, DEFAULT_CURRENCY)
}

private fun token(minor: Long?, currencyCode: String, explicit: Boolean = false): MoneyToken? {
    if (minor == null || minor <= 0L) {
        return null
    }
    return MoneyToken(amountMinor = minor, currencyCode = currencyCode, explicit = explicit)
}

private fun minorOf(amount: String): Long? {
    val european = amount.lastIndexOf(',') > amount.lastIndexOf('.')
    val normalized = if (european) {
        amount.replace(" ", "").replace(".", "").replace(',', '.')
    } else {
        amount.replace(" ", "").replace(",", "")
    }
    val parts = normalized.split('.')
    val whole = parts.first().toLongOrNull()
    val fractionText = parts.getOrNull(1).orEmpty().padEnd(FRACTION_DIGITS, '0').take(FRACTION_DIGITS)
    val fraction = when {
        whole == null -> null
        parts.size == 1 -> 0L
        else -> fractionText.toLongOrNull()
    }
    return if (whole == null || fraction == null) null else whole * MINOR_PER_MAJOR + fraction
}

private fun currency(match: MatchResult): String {
    val code = match.groupValues[CODE_AFTER].ifEmpty { match.groupValues[CODE_BEFORE] }
    if (code.isNotEmpty()) {
        return normalizeCode(code)
    }
    val symbol = match.groupValues[SYMBOL_AFTER].ifEmpty { match.groupValues[SYMBOL_BEFORE] }
    return symbols[symbol] ?: DEFAULT_CURRENCY
}

private fun currencyBefore(match: MatchResult): String {
    val code = match.groupValues[CODE_BEFORE]
    if (code.isNotEmpty()) {
        return normalizeCode(code)
    }
    return symbols[match.groupValues[SYMBOL_BEFORE]] ?: DEFAULT_CURRENCY
}

private fun currencyAfter(match: MatchResult): String {
    val symbol = match.groupValues[WHOLE_AFTER_SYMBOL]
    val code = match.groupValues[WHOLE_AFTER_CODE]
    return when {
        symbol.isNotEmpty() -> symbols[symbol] ?: DEFAULT_CURRENCY
        code.isNotEmpty() -> normalizeCode(code)
        else -> DEFAULT_CURRENCY
    }
}

private fun normalizeCode(code: String): String = when {
    code.equals("руб", ignoreCase = true) || code.equals("руб.", ignoreCase = true) -> "RUB"
    else -> code.uppercase()
}

private const val MIN_ITEM_LETTERS = 2
