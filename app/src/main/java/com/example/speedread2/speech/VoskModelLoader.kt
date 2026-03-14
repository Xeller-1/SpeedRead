package com.example.speedread2.speech

import android.content.Context
import org.vosk.Model
import org.vosk.android.StorageService

sealed class VoskModelState {
    data object Loading : VoskModelState()
    data class Success(val model: Model) : VoskModelState()
    data class Error(val throwable: Throwable) : VoskModelState()
}

fun interface VoskModelStateListener {
    fun onState(state: VoskModelState)
}

object VoskModelLoader {
    @JvmStatic
    fun load(context: Context, modelAssetPath: String, listener: VoskModelStateListener) {
        listener.onState(VoskModelState.Loading)
        StorageService.unpack(
            context,
            modelAssetPath,
            "vosk-model",
            { model -> listener.onState(VoskModelState.Success(model)) },
            { exception -> listener.onState(VoskModelState.Error(exception)) }
        )
    }
}
