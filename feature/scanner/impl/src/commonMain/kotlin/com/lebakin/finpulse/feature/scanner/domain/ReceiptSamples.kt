package com.lebakin.finpulse.feature.scanner.domain

internal data class ReceiptSample(
    val id: String,
    val title: String,
    val text: String,
)

internal object ReceiptSamples {
    val all: List<ReceiptSample> = listOf(
        ReceiptSample(
            id = "groceries",
            title = "Groceries",
            text = """
                Market
                2026-10-02
                Milk 2.40
                Bread 3.10
                TOTAL 12.50 USD
            """.trimIndent(),
        ),
        ReceiptSample(
            id = "cafe",
            title = "Cafe",
            text = """
                North Cafe
                01.10.2026
                Coffee 4.50 EUR
                TOTAL 4.50 EUR
            """.trimIndent(),
        ),
        ReceiptSample(
            id = "metro",
            title = "Metro",
            text = """
                City Metro
                02/13/2026
                Single ride 2.75 RUB
                Day pass 8.00 RUB
            """.trimIndent(),
        ),
    )

    fun text(id: String): String? = all.firstOrNull { it.id == id }?.text
}
