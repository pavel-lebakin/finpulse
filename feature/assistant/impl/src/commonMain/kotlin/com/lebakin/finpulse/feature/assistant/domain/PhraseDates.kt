package com.lebakin.finpulse.feature.assistant.domain

import kotlinx.datetime.LocalDate

private const val MONTHS_IN_YEAR = 12
private const val FIRST = 1
private const val SECOND = 2
private const val THIRD = 3

private val isoDate = Regex("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b")
private val dottedDate = Regex("\\b(\\d{1,2})[./](\\d{1,2})[./](\\d{4})\\b")

internal val phraseDateShape = Regex("\\b\\d{4}-\\d{2}-\\d{2}\\b|\\b\\d{1,2}[./]\\d{1,2}[./]\\d{4}\\b")

internal fun findPhraseDate(phrase: String): LocalDate? {
    val iso = isoDate.find(phrase)
    val dotted = if (iso == null) dottedDate.find(phrase) else null
    val dayAndMonth = if (dotted == null) {
        null
    } else {
        dayAndMonth(dotted.groupValues[FIRST].toInt(), dotted.groupValues[SECOND].toInt())
    }
    return when {
        iso != null -> localDateOrNull(
            year = iso.groupValues[FIRST].toInt(),
            month = iso.groupValues[SECOND].toInt(),
            day = iso.groupValues[THIRD].toInt(),
        )
        dayAndMonth != null && dotted != null -> localDateOrNull(
            year = dotted.groupValues[THIRD].toInt(),
            month = dayAndMonth.second,
            day = dayAndMonth.first,
        )
        else -> null
    }
}

private fun dayAndMonth(first: Int, second: Int): Pair<Int, Int> = when {
    first > MONTHS_IN_YEAR -> first to second
    second > MONTHS_IN_YEAR -> second to first
    else -> first to second
}

private fun localDateOrNull(year: Int, month: Int, day: Int): LocalDate? = try {
    LocalDate(year, month, day)
} catch (invalid: IllegalArgumentException) {
    invalid.toString().let { null }
}
