package com.lebakin.finpulse.feature.assistant.domain

private const val MINOR_PER_MAJOR = 100L
private const val CODE_BEFORE = 1
private const val SYMBOL_BEFORE = 2
private const val INTEGER = 3
private const val FRACTION = 4
private const val CODE_AFTER = 5
private const val SYMBOL_AFTER = 6
private const val DEFAULT_CURRENCY = "USD"

private val moneyPattern = Regex(
    "(?i)(?:(USD|EUR|GBP|RUB)|([$€£₽]))?\\s*" +
        "(\\d{1,7})[.,](\\d{2})" +
        "(?:\\s*(?:(USD|EUR|GBP|RUB)|([$€£₽])))?",
)

private val symbols = mapOf(
    "$" to "USD",
    "€" to "EUR",
    "£" to "GBP",
    "₽" to "RUB",
)

internal data class PhraseMoney(
    val amountMinor: Long,
    val currencyCode: String,
)

internal fun findPhraseAmount(phrase: String): PhraseMoney? =
    moneyPattern.findAll(phrase).mapNotNull { match -> token(match) }.maxByOrNull { it.amountMinor }

internal fun withoutPhraseMoney(phrase: String): String = moneyPattern.replace(phrase, " ")

private fun token(match: MatchResult): PhraseMoney? {
    val minor = match.groupValues[INTEGER].toLong() * MINOR_PER_MAJOR + match.groupValues[FRACTION].toLong()
    if (minor <= 0L) {
        return null
    }
    return PhraseMoney(amountMinor = minor, currencyCode = currency(match))
}

private fun currency(match: MatchResult): String {
    val code = match.groupValues[CODE_AFTER].ifEmpty { match.groupValues[CODE_BEFORE] }
    if (code.isNotEmpty()) {
        return code.uppercase()
    }
    val symbol = match.groupValues[SYMBOL_AFTER].ifEmpty { match.groupValues[SYMBOL_BEFORE] }
    return symbols[symbol] ?: DEFAULT_CURRENCY
}
