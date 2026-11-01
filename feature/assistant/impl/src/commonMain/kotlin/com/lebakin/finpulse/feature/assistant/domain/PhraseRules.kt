package com.lebakin.finpulse.feature.assistant.domain

private const val MERCHANT_LIMIT = 80
private const val MIN_MERCHANT_LETTERS = 2

private val englishMonthSpend = Regex(
    "\\b(?:spend|spent|spending|expense|expenses)\\b.*\\bmonth\\b|" +
        "\\bmonth\\b.*\\b(?:spend|spent|spending|expense|expenses)\\b",
)

private val russianMonthSpend = Regex(
    "(?:сколько|потратил|потратила|потратили|потрачено|расходы|расход).{0,48}месяц|" +
        "месяц.{0,48}(?:потратил|потратила|потратили|потрачено|расход)",
)

private val questionWord = Regex(
    "\\?|\\b(?:how|what|when|why|which|who|did i|have i|do i)\\b|сколько|когда|почему|зачем",
)

private val filler = Regex("(?i)\\b(?:spent|paid|at|on|for|a|the)\\b")

private val groceries = listOf("market", "grocery", "groceries", "supermarket")
private val transport = listOf("metro", "taxi", "fuel", "petrol", "uber", "bus")
private val eatingOut = listOf("cafe", "coffee", "restaurant")

internal sealed interface PhraseRead {
    data class Draft(val draft: PhraseDraft) : PhraseRead

    data object MonthSpend : PhraseRead

    data object UnsupportedQuestion : PhraseRead
}

internal class PhraseRules(
    private val clock: PhraseClock,
) {
    fun read(phrase: String): PhraseRead? {
        val trimmed = phrase.trim()
        val folded = trimmed.lowercase()
        return when {
            findPhraseAmount(trimmed) == null && isMonthSpend(folded) -> PhraseRead.MonthSpend
            questionWord.containsMatchIn(folded) -> PhraseRead.UnsupportedQuestion
            else -> draftOrNull(trimmed)
        }
    }

    private fun draftOrNull(phrase: String): PhraseRead.Draft? {
        val amount = findPhraseAmount(phrase)
        val merchant = if (amount == null) null else merchantOf(phrase)
        return if (amount == null || merchant == null) {
            null
        } else {
            PhraseRead.Draft(
                PhraseDraft(
                    merchant = merchant,
                    amountMinor = amount.amountMinor,
                    currencyCode = amount.currencyCode,
                    occurredOn = findPhraseDate(phrase) ?: clock.today(),
                    categoryName = categoryName(merchant, phrase),
                ),
            )
        }
    }
}

private fun isMonthSpend(folded: String): Boolean {
    if (englishMonthSpend.containsMatchIn(folded) || russianMonthSpend.containsMatchIn(folded)) {
        return true
    }
    val bare = folded.trimEnd('.', '?', '!', ' ')
    return bare == "how much did i spend" || bare == "how much have i spent" || bare == "what did i spend"
}

private fun merchantOf(phrase: String): String? {
    val stripped = filler.replace(phraseDateShape.replace(withoutPhraseMoney(phrase), " "), " ")
    val name = stripped.split(Regex("\\s+")).filter { it.any(Char::isLetter) }.joinToString(" ")
    val letters = name.count(Char::isLetter)
    return if (letters < MIN_MERCHANT_LETTERS) null else name.take(MERCHANT_LIMIT)
}

private fun categoryName(merchant: String, phrase: String): String {
    val haystack = "$merchant\n$phrase"
    return when {
        containsWord(haystack, groceries) -> "Groceries"
        containsWord(haystack, transport) -> "Transport"
        containsWord(haystack, eatingOut) -> "Eating out"
        else -> "General"
    }
}

private fun containsWord(haystack: String, words: List<String>): Boolean =
    words.any { word -> Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(haystack) }
