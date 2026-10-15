package com.lebakin.finpulse.core.ai

internal fun repeatsTopLevelKey(text: String): Boolean = KeyScan(text).repeats()

private class KeyScan(private val text: String) {
    private var index = 0

    fun repeats(): Boolean {
        skipWs()
        if (!consume('{')) {
            return false
        }
        val seen = mutableSetOf<String>()
        var state = Scan.Clear
        while (state == Scan.Clear) {
            state = field(seen)
        }
        return state == Scan.Duplicate
    }

    private fun field(seen: MutableSet<String>): Scan {
        val key = readString()
        skipWs()
        val parsed = key != null && consume(':') && skipValue()
        val duplicated = parsed && key in seen
        if (parsed && !duplicated) {
            seen.add(key)
        }
        skipWs()
        val continued = !consume('}') && consume(',')
        return when {
            duplicated -> Scan.Duplicate
            parsed && continued -> Scan.Clear
            else -> Scan.Stop
        }
    }

    private fun skipValue(): Boolean {
        skipWs()
        if (index >= text.length) {
            return false
        }
        return when (text[index]) {
            '"' -> readString() != null
            '{', '[' -> skipNested(text[index])
            else -> skipLiteral()
        }
    }

    private fun skipNested(open: Char): Boolean {
        val close = if (open == '{') '}' else ']'
        val other = if (open == '{') '[' else '{'
        var depth = if (consume(open)) 1 else 0
        var ok = depth == 1
        while (ok && depth > 0 && index < text.length) {
            when (text[index]) {
                '"' -> ok = readString() != null
                other -> ok = skipNested(other)
                open -> {
                    depth += 1
                    index += 1
                }
                close -> {
                    depth -= 1
                    index += 1
                }
                else -> index += 1
            }
        }
        return ok && depth == 0
    }

    private fun skipLiteral(): Boolean {
        val word = listOf("true", "false", "null").firstOrNull { text.startsWith(it, index) }
        val start = index
        if (word != null) {
            index += word.length
        } else {
            consume('-')
            while (index < text.length && text[index].isDigit()) {
                index += 1
            }
            if (consume('.')) {
                while (index < text.length && text[index].isDigit()) {
                    index += 1
                }
            }
            if (consume('e') || consume('E')) {
                if (!consume('+')) {
                    consume('-')
                }
                while (index < text.length && text[index].isDigit()) {
                    index += 1
                }
            }
        }
        return index > start
    }

    private fun readString(): String? {
        if (!consume('"')) {
            return null
        }
        val out = StringBuilder()
        var closed = false
        var broken = false
        while (index < text.length && !closed && !broken) {
            when (val char = text[index]) {
                '"' -> {
                    index += 1
                    closed = true
                }
                '\\' -> {
                    index += 1
                    broken = !appendEscape(out)
                }
                else -> {
                    index += 1
                    out.append(char)
                }
            }
        }
        return if (closed && !broken) out.toString() else null
    }

    private fun appendEscape(out: StringBuilder): Boolean {
        if (index >= text.length) {
            return false
        }
        val char = text[index]
        index += 1
        val mapped = when (char) {
            '"', '\\', '/' -> char
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> readHex()
            else -> null
        }
        return if (mapped == null) {
            false
        } else {
            out.append(mapped)
            true
        }
    }

    private fun readHex(): Char? {
        val available = index + HEX_DIGITS <= text.length
        val value = if (available) text.substring(index, index + HEX_DIGITS).toIntOrNull(HEX_RADIX) else null
        if (available) {
            index += HEX_DIGITS
        }
        return value?.toChar()
    }

    private fun consume(char: Char): Boolean {
        val match = index < text.length && text[index] == char
        if (match) {
            index += 1
        }
        return match
    }

    private fun skipWs() {
        while (index < text.length && text[index].isWhitespace()) {
            index += 1
        }
    }
}

private enum class Scan {
    Clear,
    Duplicate,
    Stop,
}

private const val HEX_DIGITS = 4
private const val HEX_RADIX = 16
