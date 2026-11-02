package com.lebakin.finpulse.core.ai

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val MERCHANT = "merchant"
private const val AMOUNT = "amountMinor"
private const val CURRENCY = "currencyCode"
private const val OCCURRED_ON = "occurredOn"
private const val CATEGORY = "categoryName"
private const val LINES = "lines"
private const val MERCHANT_LIMIT = 80
private const val MIN_MERCHANT_LETTERS = 2

private val keys = setOf(MERCHANT, AMOUNT, CURRENCY, OCCURRED_ON, CATEGORY)
private val currencies = setOf("USD", "EUR", "GBP", "RUB")
private val minorDigits = Regex("[1-9]\\d{0,8}")
private val isoDate = Regex("\\d{4}-\\d{2}-\\d{2}")

internal fun replyOf(obj: JsonObject): ReceiptReply? {
    val allowed = obj.keys == keys || obj.keys == keys + LINES
    val lines = if (!allowed) null else if (LINES in obj.keys) itemLines(obj) else emptyList()
    val merchant = merchantOf(obj)
    val amountMinor = amountOf(obj)
    val currencyCode = currencyOf(obj)
    val occurredOn = dateOf(obj)
    val category = categoryOf(obj)
    return when {
        !allowed -> null
        lines == null -> null
        merchant == null -> null
        amountMinor == null -> null
        currencyCode == null -> null
        occurredOn == null -> null
        category == null -> null
        else -> ReceiptReply(
            merchant = merchant,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            occurredOn = occurredOn,
            category = category,
            lines = lines,
        )
    }
}

private fun merchantOf(obj: JsonObject): String? {
    val value = obj.text(MERCHANT) ?: return null
    val letters = value.count { it.isLetter() }
    val rejected = value.length > MERCHANT_LIMIT || letters < MIN_MERCHANT_LETTERS || value.any { it.isISOControl() }
    return if (rejected) null else value
}

private fun amountOf(obj: JsonObject): Long? {
    val primitive = obj.primitive(AMOUNT) ?: return null
    return minorOrNull(primitive)
}

private fun minorOrNull(primitive: JsonPrimitive): Long? {
    val content = primitive.content
    return if (primitive.isString || !minorDigits.matches(content)) null else content.toLong()
}

private fun currencyOf(obj: JsonObject): String? {
    val value = obj.text(CURRENCY) ?: return null
    return if (value in currencies) value else null
}

private fun dateOf(obj: JsonObject): LocalDate? {
    val value = obj.text(OCCURRED_ON)
    return if (value == null || !isoDate.matches(value)) {
        null
    } else {
        try {
            LocalDate.parse(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

private fun categoryOf(obj: JsonObject): ReceiptCategory? {
    val value = obj.text(CATEGORY) ?: return null
    return ReceiptCategory.entries.firstOrNull { it.label == value }
}

private fun JsonObject.text(key: String): String? {
    val primitive = primitive(key) ?: return null
    return if (primitive.isString) primitive.content else null
}

private fun JsonObject.primitive(key: String): JsonPrimitive? = this[key] as? JsonPrimitive
