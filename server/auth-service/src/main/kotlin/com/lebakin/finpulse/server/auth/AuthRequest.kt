package com.lebakin.finpulse.server.auth

internal fun parseSignIn(body: String): ParsedSignIn? {
    val values = BodyReader(body).objectStrings()
    val email = values?.get("email")
    val password = values?.get("password")
    val parsed = email != null && password != null && values.size == 2
    return if (parsed && email != null && password != null) {
        ParsedSignIn(email, password.toCharArray())
    } else {
        null
    }
}

private class BodyReader(private val text: String) {
    private var index = 0
    private var broken = false

    fun objectStrings(): Map<String, String>? {
        skipSpace()
        eat('{')
        val values = linkedMapOf<String, String>()
        skipSpace()
        if (!broken && !peek('}')) {
            readFields(values)
        }
        eat('}')
        skipSpace()
        return if (!broken && index == text.length) values else null
    }

    private fun readFields(values: MutableMap<String, String>) {
        var more = true
        while (more && !broken) {
            val key = readString()
            skipSpace()
            eat(':')
            skipSpace()
            val value = readString()
            val duplicate = key != null && values.containsKey(key)
            if (key == null || value == null || duplicate) {
                broken = true
            } else {
                values[key] = value
            }
            skipSpace()
            more = !broken && eatOptional(',')
            if (more) {
                skipSpace()
            }
        }
    }

    private fun readString(): String? {
        eat('"')
        val start = index
        while (!broken && index < text.length && text[index] != '"') {
            if (text[index] == '\\' || text[index] < ' ') {
                broken = true
            } else {
                index++
            }
        }
        val closed = !broken && eat('"')
        return if (closed) text.substring(start, index - 1) else null
    }

    private fun skipSpace() {
        while (index < text.length && text[index].isWhitespace()) {
            index++
        }
    }

    private fun eat(char: Char): Boolean {
        val found = index < text.length && text[index] == char
        if (found) {
            index++
        } else {
            broken = true
        }
        return found
    }

    private fun eatOptional(char: Char): Boolean {
        val found = index < text.length && text[index] == char
        if (found) {
            index++
        }
        return found
    }

    private fun peek(char: Char): Boolean = index < text.length && text[index] == char
}
