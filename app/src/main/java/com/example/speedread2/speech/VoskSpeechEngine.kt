package com.example.speedread2.speech

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream

/**
 * Streaming PCM speech engine via Vosk + AudioRecord.
 *
 * Expects a Russian model in assets/<modelAssetPath>, e.g. assets/model-ru.
 */
class VoskSpeechEngine : SpeechEngine {

    private var appContext: Context? = null
    private var callback: SpeechEngine.Callback? = null
    private var config: SpeechEngine.Config = SpeechEngine.Config()

    @Volatile
    private var listening = false

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var readerThread: Thread? = null

    override fun initialize(context: Context, callback: SpeechEngine.Callback, config: SpeechEngine.Config) {
        this.appContext = context.applicationContext
        this.callback = callback
        this.config = config

        try {
            val modelPath = unpackModelIfNeeded(context.applicationContext, config.modelAssetPath)
            model = Model(modelPath.absolutePath)
            recognizer = Recognizer(model, config.sampleRateHz.toFloat())
        } catch (t: Throwable) {
            callback.onError("Vosk init failed: ${t.message}")
        }
    }

    @SuppressLint("MissingPermission")
    override fun startListening() {
        val context = appContext
        val cb = callback
        val rec = recognizer
        if (context == null || cb == null || rec == null) {
            callback?.onError("VoskSpeechEngine is not initialized")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            cb.onError("RECORD_AUDIO permission is missing")
            return
        }

        if (listening) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            config.sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize <= 0) {
            cb.onError("Invalid AudioRecord minBufferSize: $minBufferSize")
            return
        }

        try {
            val localAudioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                config.sampleRateHz,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (localAudioRecord.state != AudioRecord.STATE_INITIALIZED) {
                cb.onError("AudioRecord init failed")
                localAudioRecord.release()
                return
            }

            audioRecord = localAudioRecord
            listening = true
            localAudioRecord.startRecording()

            readerThread = Thread {
                val buffer = ByteArray(minBufferSize)
                while (listening) {
                    val read = localAudioRecord.read(buffer, 0, buffer.size)
                    if (read <= 0) {
                        continue
                    }
                    try {
                        val acceptedAsFinal = rec.acceptWaveForm(buffer, read)
                        if (acceptedAsFinal) {
                            extractText(rec.result, "text")?.takeIf { it.isNotBlank() }?.let {
                                cb.onFinalText(it)
                            }
                        } else {
                            extractText(rec.partialResult, "partial")?.takeIf { it.isNotBlank() }?.let {
                                cb.onPartialText(it)
                            }
                        }
                    } catch (t: Throwable) {
                        cb.onError("Vosk stream error: ${t.message}")
                    }
                }

                try {
                    extractText(rec.finalResult, "text")?.takeIf { it.isNotBlank() }?.let {
                        cb.onFinalText(it)
                    }
                } catch (_: Throwable) {
                }
            }.apply {
                name = "VoskPcmReader"
                start()
            }
        } catch (t: Throwable) {
            listening = false
            callback?.onError("Vosk start failed: ${t.message}")
        }
    }

    override fun stopListening() {
        listening = false
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
        try {
            audioRecord?.release()
        } catch (_: Throwable) {
        }
        audioRecord = null

        try {
            readerThread?.join(500)
        } catch (_: InterruptedException) {
        }
        readerThread = null
    }

    override fun release() {
        stopListening()
        try {
            recognizer?.close()
        } catch (_: Throwable) {
        }
        try {
            model?.close()
        } catch (_: Throwable) {
        }
        recognizer = null
        model = null
        callback = null
        appContext = null
    }

    private fun unpackModelIfNeeded(context: Context, modelAssetPath: String): File {
        val outDir = File(context.filesDir, "vosk/$modelAssetPath")
        if (outDir.exists() && outDir.isDirectory && outDir.list()?.isNotEmpty() == true) {
            return outDir
        }

        copyAssetFolder(context, modelAssetPath, outDir)
        return outDir
    }

    private fun copyAssetFolder(context: Context, assetPath: String, outDir: File) {
        val assets = context.assets
        val entries = assets.list(assetPath) ?: emptyArray()

        if (entries.isEmpty()) {
            outDir.parentFile?.mkdirs()
            assets.open(assetPath).use { input ->
                FileOutputStream(outDir).use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        if (!outDir.exists()) outDir.mkdirs()
        for (entry in entries) {
            val childAssetPath = "$assetPath/$entry"
            val childOut = File(outDir, entry)
            copyAssetFolder(context, childAssetPath, childOut)
        }
    }

    private fun extractText(json: String?, field: String): String? {
        if (json.isNullOrBlank()) return null
        val marker = "\"$field\""
        val markerIndex = json.indexOf(marker)
        if (markerIndex < 0) return null
        val colon = json.indexOf(':', markerIndex)
        if (colon < 0) return null
        val firstQuote = json.indexOf('"', colon + 1)
        if (firstQuote < 0) return null
        val secondQuote = json.indexOf('"', firstQuote + 1)
        if (secondQuote < 0) return null
        return json.substring(firstQuote + 1, secondQuote)
    }
}
