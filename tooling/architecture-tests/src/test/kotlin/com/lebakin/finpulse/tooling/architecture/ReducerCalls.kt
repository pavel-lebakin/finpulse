package com.lebakin.finpulse.tooling.architecture

internal object ReducerCalls {
    private val reduceBlock = Regex("""\breduce\s*([{(])""")

    fun calledOnlyInsideReduce(
        functionText: String,
        extensionNames: Set<String>,
    ): Boolean {
        if (extensionNames.isEmpty()) {
            return true
        }
        val code = withoutCommentsAndStrings(functionText)
        val ranges = reduceBlockRanges(code)
        return extensionNames.all { name ->
            callSites(code, name).all { index -> ranges.any { index in it } }
        }
    }

    private fun reduceBlockRanges(code: String): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        for (match in reduceBlock.findAll(code)) {
            val opener = match.groupValues[1].single()
            val closeIndex = findCloser(code, match.range.last, opener) ?: continue
            ranges += match.range.last..closeIndex
        }
        return ranges
    }

    private fun callSites(code: String, name: String): List<Int> =
        Regex("""\b${Regex.escape(name)}\s*\(""").findAll(code).map { it.range.first }.toList()

    private fun findCloser(
        code: String,
        openIndex: Int,
        opener: Char,
    ): Int? {
        val closer = if (opener == '{') '}' else ')'
        var depth = 0
        for (index in openIndex until code.length) {
            when (code[index]) {
                opener -> depth++
                closer -> {
                    depth--
                    if (depth == 0) {
                        return index
                    }
                }
            }
        }
        return null
    }

    private fun withoutCommentsAndStrings(source: String): String {
        val blank = CharArray(source.length) { ' ' }
        var index = 0
        while (index < source.length) {
            val skip = skipLength(source, index)
            if (skip > 0) {
                index += skip
                continue
            }
            blank[index] = source[index]
            index++
        }
        return String(blank)
    }

    private fun skipLength(source: String, index: Int): Int = when {
        source.startsWith("//", index) -> lengthUntil(source, index, "\n", includeMarker = false)
        source.startsWith("/*", index) -> lengthUntil(source, index, "*/", includeMarker = true)
        source.startsWith("\"\"\"", index) -> lengthUntil(source, index, "\"\"\"", includeMarker = true)
        source[index] == '"' -> quotedLength(source, index, '"')
        source[index] == '\'' -> quotedLength(source, index, '\'')
        else -> 0
    }

    private fun lengthUntil(
        source: String,
        start: Int,
        marker: String,
        includeMarker: Boolean,
    ): Int {
        val found = source.indexOf(marker, start + marker.length)
        if (found == -1) {
            return source.length - start
        }
        val end = if (includeMarker) found + marker.length else found
        return end - start
    }

    private fun quotedLength(
        source: String,
        start: Int,
        quote: Char,
    ): Int {
        var index = start + 1
        while (index < source.length) {
            if (source[index] == '\\') {
                index += 2
                continue
            }
            if (source[index] == quote) {
                return index + 1 - start
            }
            index++
        }
        return source.length - start
    }
}
