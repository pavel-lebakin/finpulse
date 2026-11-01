package com.lebakin.finpulse.feature.scanner.domain

private val cyrillic = Regex("[а-яёіїєґ]")

private val euroMarkers = Regex(
    "ção|ã|õ|ñ|ß|ä|ö|ü|ą|ę|ł|ż|ś|ź|ş|ğ|" +
        "multibanco|fatura|importe|obrigad|cantidad|precio|efectivo|espèces|especes|monnaie|mwst|" +
        "scontrino|paragon|toplam|kdv|danke|grazie|merci|euro|(?<!\\p{L})iva(?!\\p{L})",
)

private val poundMarkers = Regex("\\bpound|\\bgbp\\b")

private const val MIN_CYRILLIC = 3

internal fun withLanguageCurrency(token: MoneyToken, text: String): MoneyToken =
    if (token.explicit) token else token.copy(currencyCode = hintedCurrency(text.lowercase()))

private fun hintedCurrency(folded: String): String = when {
    cyrillic.findAll(folded).count() >= MIN_CYRILLIC -> "RUB"
    euroMarkers.containsMatchIn(folded) -> "EUR"
    poundMarkers.containsMatchIn(folded) -> "GBP"
    else -> "USD"
}
