package com.example.speedread2.reading

import kotlin.math.max
import kotlin.math.min

class TextAligner(
    private val expectedTokens: List<TokenUnit>,
    private val normalizer: TextNormalizer = TextNormalizer(),
    private val mode: ReadingMode = ReadingMode.STORY
) {

    data class AlignmentState(
        val currentTokenIndex: Int,
        val currentLineIndex: Int,
        val coveragePercent: Int,
        val isCompleted: Boolean,
        val matchedTokenIndices: Set<Int>,
        val activeTokenIndex: Int,
        val lastStableIndex: Int,
        val matchedWords: Int,
        val skippedWords: Int,
        val weakMatches: Int
    )

    private var currentIndex: Int = 0
    private var lastStableIndex: Int = 0
    private var matchedWords: Int = 0
    private var skippedWords: Int = 0
    private var weakMatches: Int = 0
    private val matchedIndices = linkedSetOf<Int>()

    private val profile = mode.profile

    fun onRecognizedChunk(rawText: String): AlignmentState {
        if (expectedTokens.isEmpty()) return buildState()

        val recognizedTokens = normalizer.tokenize(rawText).map { it.normalized }
        if (recognizedTokens.isEmpty()) return buildState()

        recognizedTokens.forEach { recognized ->
            val searchStart = max(0, currentIndex - profile.windowBack)
            val searchEnd = min(expectedTokens.lastIndex, currentIndex + profile.windowForward)

            var bestIndex = -1
            var bestScore = Int.MIN_VALUE
            var bestWeak = false

            for (i in searchStart..searchEnd) {
                if (profile.strictLineRespect) {
                    val currentLine = expectedTokens[currentIndex.coerceIn(0, expectedTokens.lastIndex)].lineIndex
                    if (expectedTokens[i].lineIndex < currentLine - 1 || expectedTokens[i].lineIndex > currentLine + 1) {
                        continue
                    }
                }

                val expected = expectedTokens[i].normalized
                val (score, weak) = scoreMatch(expected, recognized)
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = i
                    bestWeak = weak
                }
            }

            if (bestIndex == -1 || bestScore < profile.minAcceptScore) return@forEach

            // Repetition is not critical.
            if (matchedIndices.contains(bestIndex)) {
                currentIndex = max(currentIndex, bestIndex)
                return@forEach
            }

            val gap = bestIndex - currentIndex
            if (gap > 1) {
                val effectiveSkip = max(0, gap - 1 - profile.allowedSoftSkips)
                skippedWords += effectiveSkip
            }

            matchedIndices += bestIndex
            matchedWords += 1
            if (bestWeak) weakMatches += 1

            currentIndex = min(expectedTokens.lastIndex, bestIndex + 1)
            lastStableIndex = max(lastStableIndex, bestIndex)
        }

        return buildState()
    }

    fun currentState(): AlignmentState = buildState()

    private fun buildState(): AlignmentState {
        val total = expectedTokens.size.coerceAtLeast(1)
        val coverage = ((matchedIndices.size * 100.0) / total).toInt().coerceIn(0, 100)
        val activeIndex = currentIndex.coerceIn(0, expectedTokens.lastIndex.coerceAtLeast(0))
        val lineIndex = if (expectedTokens.isEmpty()) 0 else expectedTokens[activeIndex].lineIndex

        return AlignmentState(
            currentTokenIndex = currentIndex,
            currentLineIndex = lineIndex,
            coveragePercent = coverage,
            isCompleted = matchedIndices.size >= expectedTokens.size && expectedTokens.isNotEmpty(),
            matchedTokenIndices = matchedIndices.toSet(),
            activeTokenIndex = activeIndex,
            lastStableIndex = lastStableIndex,
            matchedWords = matchedWords,
            skippedWords = skippedWords,
            weakMatches = weakMatches
        )
    }

    private fun scoreMatch(expected: String, recognized: String): Pair<Int, Boolean> {
        if (expected == recognized) return 100 to false

        val distance = levenshtein(expected, recognized)
        val maxLen = max(expected.length, recognized.length).coerceAtLeast(1)
        val similarity = 1.0 - (distance.toDouble() / maxLen)

        return when {
            similarity >= profile.weakSimilarityThresholdHigh -> 76 to true
            similarity >= profile.weakSimilarityThresholdLow -> 48 to true
            else -> 0 to false
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost
                )
            }
            for (j in prev.indices) prev[j] = curr[j]
        }

        return prev[b.length]
    }
}
