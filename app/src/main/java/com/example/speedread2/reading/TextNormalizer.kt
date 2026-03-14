package com.example.speedread2.reading

import java.util.Locale

class TextNormalizer {

    fun normalize(raw: String): String {
        if (raw.isBlank()) return ""

        val lower = raw.lowercase(Locale("ru", "RU"))
            .replace('ё', 'е')
            .replace('Ё', 'е')

        val normalizedQuotesAndDashes = lower
            .replace('—', '-')
            .replace('–', '-')
            .replace('‑', '-')
            .replace('«', '"')
            .replace('»', '"')
            .replace('“', '"')
            .replace('”', '"')
            .replace('„', '"')
            .replace('’', '\'')
            .replace('`', '\'')

        val noPunctuation = buildString(normalizedQuotesAndDashes.length) {
            normalizedQuotesAndDashes.forEach { ch ->
                when {
                    ch.isLetterOrDigit() -> append(ch)
                    ch.isWhitespace() -> append(' ')
                    ch == '-' || ch == '"' || ch == '\'' || ch == '/' -> append(' ')
                    else -> append(' ')
                }
            }
        }

        return noPunctuation
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun tokenize(raw: String): List<TokenUnit> {
        val normalized = normalize(raw)
        if (normalized.isBlank()) return emptyList()

        return normalized
            .split(' ')
            .filter { it.isNotBlank() }
            .mapIndexed { index, token ->
                TokenUnit(
                    raw = token,
                    normalized = token,
                    tokenIndex = index,
                    lineIndex = 0
                )
            }
    }
}
