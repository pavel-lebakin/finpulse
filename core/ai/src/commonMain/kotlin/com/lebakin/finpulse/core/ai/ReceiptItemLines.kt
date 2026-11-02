package com.lebakin.finpulse.core.ai

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val NAME = "name"
private const val AMOUNT = "amountMinor"
private const val MAX_LINES = 40
private const val NAME_LIMIT = 80
private const val MIN_NAME_LETTERS = 2

private val itemKeys = setOf(NAME, AMOUNT)
private val minorDigits = Regex("[1-9]\\d{0,8}")

internal fun itemLines(obj: JsonObject): List<ReceiptItemReply>? {
    val array = obj["lines"] as? JsonArray
    val parsed = if (array == null || array.size > MAX_LINES) null else array.map { itemOrNull(it) }
    return if (parsed == null || parsed.any { it == null }) null else parsed.filterNotNull()
}

private fun itemOrNull(element: JsonElement): ReceiptItemReply? {
    val obj = element as? JsonObject ?: return null
    val name = obj[NAME] as? JsonPrimitive
    val amount = obj[AMOUNT] as? JsonPrimitive
    val label = name?.content
    val minor = amount?.content?.toLongOrNull()
    val nameOk = named(obj, name, label)
    val amountOk = priced(amount, minor)
    return if (nameOk && amountOk) {
        ReceiptItemReply(name = checkNotNull(label), amountMinor = checkNotNull(minor))
    } else {
        null
    }
}

private fun named(obj: JsonObject, name: JsonPrimitive?, label: String?): Boolean {
    val shape = obj.keys == itemKeys && name != null && name.isString
    return shape && label != null && validName(label)
}

private fun priced(amount: JsonPrimitive?, minor: Long?): Boolean {
    val digits = amount?.content
    val numeric = amount != null && !amount.isString && minor != null
    return numeric && digits != null && minorDigits.matches(digits)
}

private fun validName(label: String): Boolean =
    label.count { it.isLetter() } >= MIN_NAME_LETTERS &&
        label.length <= NAME_LIMIT &&
        label.none { it.isISOControl() }
