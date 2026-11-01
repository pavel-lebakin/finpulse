package com.lebakin.finpulse.feature.scanner.domain

private const val MERCHANT_LIMIT = 80
private const val MIN_ITEM_LETTERS = 2

private val totalLine = Regex(
    "\\b(total|amount due|sum)\\b|итого|к оплате|(?<!\\p{L})(importe|totale|gesamt|summe|razem|toplam)(?!\\p{L})",
)

private val summaryLine = Regex(
    "\\b(subtotal|sub-total|total|amount due|balance|tax|vat|gst|change|cash|tender|payment|discount|tip|" +
        "rounding|grand|net|sum)\\b|итого|сумма|ндс|сдача|наличн|безнал|скидка|к оплате|оплата|налог|" +
        "(?<!\\p{L})(iva|imposto|troco|multibanco|desconto|importe|efectivo|tarjeta|cambio|tva|monnaie|" +
        "especes|mwst|summe|gesamt|rabatt|totale|scontrino|contanti|razem|paragon|gotowka|toplam|kdv|" +
        "indirim)(?!\\p{L})",
)

internal fun receiptLines(text: String): List<String> {
    val raw = text.lines().map { repairLine(it).trim() }.filter { it.isNotEmpty() }
    return mergeOrphanPrices(raw)
}

internal fun findItems(lines: List<String>): List<ReceiptLine> = lines.mapNotNull { line -> itemOrNull(line) }

internal fun findAmount(lines: List<String>, items: List<ReceiptLine>): MoneyToken? {
    val labeled = lines.mapNotNull { line ->
        if (isTotalLine(line)) monies(line).lastOrNull() else null
    }
    val summed = if (labeled.isEmpty()) sumItems(items) else null
    val largest = if (labeled.isEmpty() && summed == null) {
        lines.flatMap { monies(it) }.maxByOrNull { it.amountMinor }
    } else {
        null
    }
    return labeled.lastOrNull() ?: summed ?: largest
}

internal fun isTotalLine(line: String): Boolean = totalLine.containsMatchIn(line.lowercase())

internal fun isSummaryLine(line: String): Boolean = summaryLine.containsMatchIn(line.lowercase())

internal fun labelOf(line: String): String? {
    val stripped = dateShape.replace(withoutMoney(line), " ")
    val name = stripped.split(Regex("\\s+")).filter { it.any(Char::isLetter) }.joinToString(" ")
    if (name.count(Char::isLetter) < MIN_ITEM_LETTERS) {
        return null
    }
    return name.take(MERCHANT_LIMIT)
}

private fun itemOrNull(line: String): ReceiptLine? {
    val name = if (isSummaryLine(line)) null else labelOf(line)
    val money = name?.let { monies(line).lastOrNull() }
    return if (name == null || money == null) {
        null
    } else {
        ReceiptLine(
            name = name,
            amountMinor = money.amountMinor,
            currencyCode = money.currencyCode,
            explicit = money.explicit,
        )
    }
}

private fun sumItems(items: List<ReceiptLine>): MoneyToken? {
    val currency = items.lastOrNull()?.currencyCode
    val matched = if (currency == null) emptyList() else items.filter { it.currencyCode == currency }
    val minor = matched.sumOf { it.amountMinor }
    val explicit = matched.isNotEmpty() && matched.all { it.explicit }
    return if (currency == null || minor <= 0L) {
        null
    } else {
        MoneyToken(amountMinor = minor, currencyCode = currency, explicit = explicit)
    }
}

private fun mergeOrphanPrices(lines: List<String>): List<String> {
    val merged = mutableListOf<String>()
    lines.forEach { line ->
        val previous = merged.lastOrNull()
        if (previous != null && attachesPrice(previous, line)) {
            merged[merged.lastIndex] = "$previous $line"
        } else {
            merged += line
        }
    }
    return merged
}

private fun attachesPrice(previous: String, line: String): Boolean {
    if (monies(previous).isNotEmpty() || monies(line).isEmpty() || labelOf(line) != null) {
        return false
    }
    return isSummaryLine(previous) || labelOf(previous) != null
}
