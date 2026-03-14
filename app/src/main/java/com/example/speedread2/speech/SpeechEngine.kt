package com.example.speedread2.speech

import android.content.Context

interface SpeechEngine {
    data class Config(
        val languageTag: String = "ru-RU",
        val sampleRateHz: Int = 16_000,
        val modelAssetPath: String = "model-ru"
    )

    interface Callback {
        fun onPartialText(text: String)
        fun onFinalText(text: String)
        fun onError(error: String)
    }

    fun initialize(context: Context, callback: Callback, config: Config = Config())
    fun startListening()
    fun stopListening()
    fun release()
}
