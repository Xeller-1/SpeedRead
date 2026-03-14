package com.example.speedread2.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Legacy / fallback engine over Android SpeechRecognizer.
 */
class AndroidSpeechEngine : SpeechEngine {

    private var context: Context? = null
    private var callback: SpeechEngine.Callback? = null
    private var config: SpeechEngine.Config = SpeechEngine.Config()

    private var speechRecognizer: SpeechRecognizer? = null
    private var speechIntent: Intent? = null
    private var isListening = false

    override fun initialize(context: Context, callback: SpeechEngine.Callback, config: SpeechEngine.Config) {
        this.context = context.applicationContext
        this.callback = callback
        this.config = config

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("Android speech recognition is not available on this device")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    callback.onError("AndroidSpeechEngine error code: $error")
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (text.isNotBlank()) {
                        callback.onFinalText(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val text = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (text.isNotBlank()) {
                        callback.onPartialText(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
    }

    override fun startListening() {
        val recognizer = speechRecognizer
        val intent = speechIntent
        if (recognizer == null || intent == null) {
            callback?.onError("AndroidSpeechEngine is not initialized")
            return
        }

        try {
            isListening = true
            recognizer.startListening(intent)
        } catch (t: Throwable) {
            isListening = false
            callback?.onError("AndroidSpeechEngine start failed: ${t.message}")
        }
    }

    override fun stopListening() {
        isListening = false
        try {
            speechRecognizer?.stopListening()
        } catch (_: Throwable) {
        }
    }

    override fun release() {
        isListening = false
        try {
            speechRecognizer?.cancel()
        } catch (_: Throwable) {
        }
        try {
            speechRecognizer?.destroy()
        } catch (_: Throwable) {
        }
        speechRecognizer = null
        speechIntent = null
        callback = null
        context = null
    }
}
