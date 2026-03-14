package com.example.speedread2.reading

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizerTest {

    private val normalizer = TextNormalizer()

    @Test
    fun normalize_handlesCasePunctuationAndSpaces() {
        val raw = "  ЁЖИК — сказал: «Привет,   мир!!»  "
        val normalized = normalizer.normalize(raw)

        assertEquals("ежик сказал привет мир", normalized)
    }

    @Test
    fun tokenize_returnsNormalizedTokens() {
        val tokens = normalizer.tokenize("Раз-два, три")

        assertEquals(listOf("раз", "два", "три"), tokens.map { it.normalized })
        assertTrue(tokens.indices.all { tokens[it].tokenIndex == it })
    }
}
