package com.jarvis.android.wakeword

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineActivationException
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import android.util.Log

class WakeWordManager(
    private val context: Context,
    private val accessKey: String,
    private val onWakeWordDetected: () -> Unit,
    private val onError: (String) -> Unit
) {
    private var porcupineManager: PorcupineManager? = null
    private var isListening = false

    companion object {
        private const val TAG = "WakeWordManager"
    }

    fun start() {
        if (isListening) {
            Log.d(TAG, "Already listening")
            return
        }

        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeyword(Porcupine.BuiltInKeyword.JARVIS)
                .setSensitivity(0.7f)
                .build(context, object : PorcupineManagerCallback {
                    override fun invoke(keywordIndex: Int) {
                        Log.d(TAG, "Wake word detected!")
                        onWakeWordDetected()
                    }
                })

            porcupineManager?.start()
            isListening = true
            Log.d(TAG, "Started listening for wake word")
        } catch (e: PorcupineActivationException) {
            Log.e(TAG, "Porcupine activation error", e)
            onError("Invalid Porcupine API key: ${e.message}")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Porcupine error", e)
            onError("Porcupine error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting wake word detection", e)
            onError("Error: ${e.message}")
        }
    }

    fun stop() {
        try {
            porcupineManager?.stop()
            porcupineManager?.delete()
            porcupineManager = null
            isListening = false
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping wake word detection", e)
        }
    }

    fun isActive(): Boolean = isListening
}
