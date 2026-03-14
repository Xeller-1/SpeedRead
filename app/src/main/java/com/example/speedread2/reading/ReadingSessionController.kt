package com.example.speedread2.reading

import android.content.Context
import com.example.speedread2.speech.SpeechEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadingSessionController(
    private val speechEngine: SpeechEngine,
    private val normalizer: TextNormalizer = TextNormalizer()
) {

    enum class MicrophoneState {
        IDLE,
        LISTENING,
        STOPPED,
        ERROR
    }

    data class ReadingSessionState(
        val currentText: String = "",
        val tokens: List<TokenUnit> = emptyList(),
        val mode: ReadingMode = ReadingMode.STORY,
        val progress: TextAligner.AlignmentState? = null,
        val microphoneState: MicrophoneState = MicrophoneState.IDLE,
        val finalReadingScore: Int = 0,
        val coveragePercent: Int = 0,
        val accuracyPercent: Int = 0,
        val fluencyScore: Int = 0,
        val completionScore: Int = 0,
        val readingScore: Int = 0,
        val lastPartialText: String = "",
        val lastFinalText: String = "",
        val errorMessage: String? = null
    )

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var aligner: TextAligner? = null
    private var started = false
    private var mode: ReadingMode = ReadingMode.STORY

    private val _state = MutableStateFlow(ReadingSessionState())
    val state: StateFlow<ReadingSessionState> = _state.asStateFlow()

    fun setMode(readingMode: ReadingMode) {
        mode = readingMode
        _state.update { it.copy(mode = readingMode) }

        if (itHasTokens()) {
            aligner = TextAligner(_state.value.tokens, normalizer, mode)
            _state.update { it.copy(progress = aligner?.currentState()) }
        }
    }

    fun loadExerciseText(rawText: String) {
        val lines = rawText.lines()
        val tokens = mutableListOf<TokenUnit>()
        var tokenIndex = 0

        lines.forEachIndexed { lineIndex, line ->
            val normalizedLine = normalizer.normalize(line)
            if (normalizedLine.isBlank()) return@forEachIndexed

            normalizedLine.split(' ')
                .filter { it.isNotBlank() }
                .forEach { token ->
                    tokens += TokenUnit(
                        raw = token,
                        normalized = token,
                        tokenIndex = tokenIndex++,
                        lineIndex = lineIndex
                    )
                }
        }

        aligner = TextAligner(tokens, normalizer, mode)
        _state.value = ReadingSessionState(
            currentText = rawText,
            tokens = tokens,
            mode = mode,
            progress = aligner?.currentState(),
            microphoneState = MicrophoneState.IDLE,
            finalReadingScore = 0,
            readingScore = 0
        )
    }

    fun initialize(context: Context, config: SpeechEngine.Config = SpeechEngine.Config()) {
        speechEngine.initialize(
            context = context,
            config = config,
            callback = object : SpeechEngine.Callback {
                override fun onPartialText(text: String) {
                    processRecognizedText(text, isFinal = false)
                }

                override fun onFinalText(text: String) {
                    processRecognizedText(text, isFinal = true)
                }

                override fun onError(error: String) {
                    _state.update {
                        it.copy(
                            microphoneState = MicrophoneState.ERROR,
                            errorMessage = error
                        )
                    }
                }
            }
        )
    }

    fun start() {
        if (started) return
        started = true
        speechEngine.startListening()
        _state.update { it.copy(microphoneState = MicrophoneState.LISTENING, errorMessage = null) }
    }

    fun stop() {
        if (!started) return
        started = false
        speechEngine.stopListening()
        _state.update { it.copy(microphoneState = MicrophoneState.STOPPED) }
    }

    fun release() {
        started = false
        speechEngine.release()
        val job = scope.coroutineContext[Job]
        job?.cancel()
        _state.update { it.copy(microphoneState = MicrophoneState.IDLE) }
    }

    private fun processRecognizedText(rawText: String, isFinal: Boolean) {
        scope.launch {
            val normalized = normalizer.normalize(rawText)
            val localAligner = aligner ?: return@launch
            val progress = localAligner.onRecognizedChunk(normalized)
            val score = computeScore(progress)

            _state.update {
                it.copy(
                    progress = progress,
                    finalReadingScore = score.readingScore,
                    coveragePercent = score.coveragePercent,
                    accuracyPercent = score.accuracyPercent,
                    fluencyScore = score.fluencyScore,
                    completionScore = score.completionScore,
                    readingScore = score.readingScore,
                    lastPartialText = if (isFinal) it.lastPartialText else normalized,
                    lastFinalText = if (isFinal) normalized else it.lastFinalText,
                    microphoneState = when {
                        progress.isCompleted -> MicrophoneState.STOPPED
                        else -> it.microphoneState
                    }
                )
            }

            if (progress.isCompleted && started) {
                started = false
                speechEngine.stopListening()
            }
        }
    }

    private fun computeScore(progress: TextAligner.AlignmentState): ReadingScore {
        val coveragePercent = progress.coveragePercent.coerceIn(0, 100)

        val recognized = progress.matchedWords.coerceAtLeast(1)
        val exactEstimate = (progress.matchedWords - progress.weakMatches).coerceAtLeast(0)
        val accuracyPercent = ((exactEstimate * 100.0) / recognized).toInt().coerceIn(0, 100)

        val fluencyPenalty = progress.skippedWords * 5 + progress.weakMatches * 2
        val fluencyScore = (100 - fluencyPenalty).coerceIn(0, 100)

        val completionScore = if (progress.isCompleted) 100 else coveragePercent

        // Requested weighted approach: 50% coverage, 30% accuracy, 20% fluency.
        val readingScore = (
            coveragePercent * 0.5 +
                accuracyPercent * 0.3 +
                fluencyScore * 0.2
            ).toInt().coerceIn(0, 100)

        return ReadingScore(
            coveragePercent = coveragePercent,
            accuracyPercent = accuracyPercent,
            fluencyScore = fluencyScore,
            completionScore = completionScore,
            readingScore = readingScore
        )
    }

    private fun itHasTokens(): Boolean = _state.value.tokens.isNotEmpty()
}
