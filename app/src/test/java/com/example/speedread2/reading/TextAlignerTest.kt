package com.example.speedread2.reading

import org.junit.Assert.assertTrue
import org.junit.Test

class TextAlignerTest {

    private val normalizer = TextNormalizer()

    @Test
    fun aligner_progressesWithExactAndWeakMatches() {
        val expected = normalizer.tokenize("мама мыла раму потом окно")
        val aligner = TextAligner(expected, normalizer)

        val s1 = aligner.onRecognizedChunk("мама мила") // "мила" weak for "мыла"
        val s2 = aligner.onRecognizedChunk("раму")
        val s3 = aligner.onRecognizedChunk("потом окно")

        assertTrue(s1.matchedWords >= 1)
        assertTrue(s2.currentTokenIndex >= s1.currentTokenIndex)
        assertTrue(s3.coveragePercent >= 80)
        assertTrue(s3.isCompleted)
    }

    @Test
    fun aligner_toleratesSingleSkippedWord() {
        val expected = normalizer.tokenize("я очень люблю читать книги")
        val aligner = TextAligner(expected, normalizer)

        val state = aligner.onRecognizedChunk("я люблю читать книги")

        assertTrue(state.coveragePercent >= 70)
        assertTrue(state.skippedWords >= 0)
    }
}
