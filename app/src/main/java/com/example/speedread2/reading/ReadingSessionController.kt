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
        val progress: TextAligner.AlignmentState? = null,
        val microphoneState: MicrophoneState = MicrophoneState.IDLE,
        val finalReadingScore: Int = 0,
        val lastPartialText: String = "",
        val lastFinalText: String = "",
        val errorMessage: String? = null
    )

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var aligner: TextAligner? = null
    private var currentConfig: SpeechEngine.Config = SpeechEngine.Config()
    private var started = false

    private val _state = MutableStateFlow(ReadingSessionState())
    val state: StateFlow<ReadingSessionState> = _state.asStateFlow()

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

        aligner = TextAligner(tokens, normalizer)
        _state.value = ReadingSessionState(
            currentText = rawText,
            tokens = tokens,
            progress = aligner?.currentState(),
            microphoneState = MicrophoneState.IDLE,
            finalReadingScore = 0
        )
    }

    fun initialize(context: Context, config: SpeechEngine.Config = SpeechEngine.Config()) {
        currentConfig = config
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
            val computedScore = computeFinalScore(progress)

            _state.update {
                it.copy(
                    progress = progress,
                    finalReadingScore = computedScore,
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

    private fun computeFinalScore(progress: TextAligner.AlignmentState): Int {
        val base = progress.coveragePercent
        val weakPenalty = progress.weakMatches * 2
        val skipPenalty = progress.skippedWords
        return (base - weakPenalty - skipPenalty).coerceIn(0, 100)
    }
}
