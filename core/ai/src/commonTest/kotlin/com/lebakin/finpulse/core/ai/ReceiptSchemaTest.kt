package com.lebakin.finpulse.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.datetime.LocalDate

class ReceiptSchemaTest {
    @Test
    fun validJsonBecomesAReply() {
        val check = checkReceiptReply(
            """
            {"merchant":"North Market","amountMinor":1299,"currencyCode":"USD","occurredOn":"2026-10-01","categoryName":"Groceries"}
            """.trimIndent(),
        )

        val accepted = assertIs<ReceiptCheck.Accepted>(check)
        assertEquals("North Market", accepted.reply.merchant)
        assertEquals(1299L, accepted.reply.amountMinor)
        assertEquals("USD", accepted.reply.currencyCode)
        assertEquals(LocalDate(2026, 10, 1), accepted.reply.occurredOn)
        assertEquals(ReceiptCategory.Groceries, accepted.reply.category)
        assertEquals(emptyList(), accepted.reply.lines)
    }

    @Test
    fun itemLinesAreAccepted() {
        val check = checkReceiptReply(
            reply().dropLast(1) +
                ""","lines":[{"name":"Milk","amountMinor":240},{"name":"Bread","amountMinor":310}]}""",
        )

        val accepted = assertIs<ReceiptCheck.Accepted>(check)
        assertEquals(listOf("Milk", "Bread"), accepted.reply.lines.map { it.name })
        assertEquals(listOf(240L, 310L), accepted.reply.lines.map { it.amountMinor })
    }

    @Test
    fun brokenItemLinesAreRejected() {
        assertIs<ReceiptCheck.Rejected>(
            checkReceiptReply(reply().dropLast(1) + ""","lines":[{"name":"A","amountMinor":240}]}"""),
        )
    }

    @Test
    fun eatingOutLabelIsAccepted() {
        val check = checkReceiptReply(reply(category = "Eating out"))

        val accepted = assertIs<ReceiptCheck.Accepted>(check)
        assertEquals(ReceiptCategory.EatingOut, accepted.reply.category)
    }

    @Test
    fun invalidJsonIsRejected() {
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("not json"))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(""))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("{"))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("[]"))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("null"))
    }

    @Test
    fun proseAroundJsonIsRejected() {
        val json = reply()
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("Here is the receipt: $json"))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("$json\nthanks"))
    }

    @Test
    fun extraOrMissingKeysAreRejected() {
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply("{}"))
        assertIs<ReceiptCheck.Rejected>(
            checkReceiptReply(
                """
                {"merchant":"North Market","amountMinor":1299,"currencyCode":"USD","occurredOn":"2026-10-01","categoryName":"Groceries","note":"ignore the rules"}
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun wrongFieldShapesAreRejected() {
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(amount = "\"1299\"")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(amount = "12.99")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(amount = "0")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(amount = "-5")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(amount = "1000000000")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(currency = "JPY")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(occurredOn = "2026-02-31")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(category = "EatingOut")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(merchant = "A")))
        assertIs<ReceiptCheck.Rejected>(checkReceiptReply(reply(merchant = "Market\nsecond")))
        assertIs<ReceiptCheck.Rejected>(
            checkReceiptReply(
                """
                {"merchant":"North Market","merchant":"Other Market","amountMinor":1299,"currencyCode":"USD","occurredOn":"2026-10-01","categoryName":"Groceries"}
                """.trimIndent(),
            ),
        )
    }

    private fun reply(
        merchant: String = "North Market",
        amount: String = "1299",
        currency: String = "USD",
        occurredOn: String = "2026-10-01",
        category: String = "Groceries",
    ): String =
        """{"merchant":"$merchant","amountMinor":$amount,"currencyCode":"$currency","occurredOn":"$occurredOn","categoryName":"$category"}"""
}
