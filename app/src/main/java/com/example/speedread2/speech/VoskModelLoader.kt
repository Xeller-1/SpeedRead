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

        val resolvedPath = resolveModelAssetPath(context, modelAssetPath)
        StorageService.unpack(
            context,
            resolvedPath,
            "vosk-model",
            { model -> listener.onState(VoskModelState.Success(model)) },
            { exception -> listener.onState(VoskModelState.Error(exception)) }
        )
    }

    private fun resolveModelAssetPath(context: Context, modelAssetPath: String): String {
        val assets = context.assets
        val rootEntries = assets.list(modelAssetPath)?.toList().orEmpty()

        // Стандартный случай: модель лежит прямо в model-ru (am/conf/graph/ivector)
        if (looksLikeModelRoot(rootEntries)) {
            return modelAssetPath
        }

        // Частый случай: модель вложена в подпапку, например model-ru/vosk-model-ru-0.22
        val nestedCandidate = rootEntries.firstOrNull { child ->
            val childPath = "$modelAssetPath/$child"
            val childEntries = assets.list(childPath)?.toList().orEmpty()
            looksLikeModelRoot(childEntries)
        }

        return if (nestedCandidate != null) "$modelAssetPath/$nestedCandidate" else modelAssetPath
    }

    private fun looksLikeModelRoot(entries: List<String>): Boolean {
        if (entries.isEmpty()) return false
        val required = setOf("am", "conf", "graph")
        return required.all { it in entries }
    }
}
