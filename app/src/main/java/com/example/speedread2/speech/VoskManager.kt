package com.example.speedread2.speech

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

class VoskManager(
    private val model: Model,
    expectedText: String,
    private val sampleRate: Int = 16_000,
    private val externalScope: CoroutineScope? = null
) {

    interface Callback {
        fun onPartialText(text: String)
        fun onFinalText(text: String)
        fun onError(error: String)
    }

    private val scope = externalScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var recognizer: Recognizer = Recognizer(
        model,
        sampleRate.toFloat(),
        buildGrammarJson(expectedText)
    )

    private var audioRecord: AudioRecord? = null
    private var readJob: Job? = null

    @Volatile
    private var listening: Boolean = false

    fun start(callback: Callback) {
        if (listening) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBufferSize <= 0) {
            callback.onError("AudioRecord buffer init failed: $minBufferSize")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                callback.onError("AudioRecord init failed")
                stop()
                return
            }

            audioRecord?.startRecording()
            listening = true

            readJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(minBufferSize)
                while (listening) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read <= 0) continue

                    try {
                        val isFinal = recognizer.acceptWaveForm(buffer, read)
                        if (isFinal) {
                            val finalText = JSONObject(recognizer.result).optString("text", "").trim()
                            if (finalText.isNotEmpty()) {
                                launch(Dispatchers.Main) { callback.onFinalText(finalText) }
                            }
                        } else {
                            val partialText = JSONObject(recognizer.partialResult).optString("partial", "").trim()
                            if (partialText.isNotEmpty()) {
                                launch(Dispatchers.Main) { callback.onPartialText(partialText) }
                            }
                        }
                    } catch (t: Throwable) {
                        launch(Dispatchers.Main) {
                            callback.onError("Vosk stream error: ${t.message.orEmpty()}")
                        }
                    }
                }

                try {
                    val tail = JSONObject(recognizer.finalResult).optString("text", "").trim()
                    if (tail.isNotEmpty()) {
                        launch(Dispatchers.Main) { callback.onFinalText(tail) }
                    }
                } catch (_: Throwable) {
                }
            }
        } catch (t: Throwable) {
            callback.onError("Vosk start failed: ${t.message.orEmpty()}")
            stop()
        }
    }

    fun stop() {
        listening = false
        readJob?.cancel()
        readJob = null

        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }

        try {
            audioRecord?.release()
        } catch (_: Throwable) {
        }
        audioRecord = null
    }

    fun close() {
        stop()
        try {
            recognizer.close()
        } catch (_: Throwable) {
        }
        if (externalScope == null) {
            scope.cancel()
        }
    }

    companion object {
        private fun buildGrammarJson(expectedText: String): String {
            val tokens = expectedText
                .lowercase()
                .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .distinct()
                .toMutableList()

            tokens.addAll(listOf("[unk]", "э", "мм", "ну"))
            return JSONArray(tokens).toString()
        }
    }
}
