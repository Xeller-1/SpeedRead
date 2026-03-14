package com.example.speedread2.speech

data class RecognitionResult(
    val text: String,
    val isFinal: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)
