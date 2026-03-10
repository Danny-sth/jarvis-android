package com.jarvis.android.audio

import android.util.Log

class VoiceActivityDetector(
    private val silenceThreshold: Short = 500,
    private val silenceTimeoutMs: Long = 2000L
) {
    private var lastVoiceActivityTime: Long = 0
    private var isRecording = false

    companion object {
        private const val TAG = "VoiceActivityDetector"
    }

    fun startRecording() {
        lastVoiceActivityTime = System.currentTimeMillis()
        isRecording = true
        Log.d(TAG, "Started voice activity detection")
    }

    fun stopRecording() {
        isRecording = false
        Log.d(TAG, "Stopped voice activity detection")
    }

    fun processAudioBuffer(buffer: ShortArray, readSize: Int): Boolean {
        if (!isRecording) return false

        val maxAmplitude = buffer.take(readSize).maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 0

        if (maxAmplitude > silenceThreshold) {
            lastVoiceActivityTime = System.currentTimeMillis()
        }

        val silenceDuration = System.currentTimeMillis() - lastVoiceActivityTime

        if (silenceDuration >= silenceTimeoutMs) {
            Log.d(TAG, "Silence timeout reached after ${silenceDuration}ms")
            return true
        }

        return false
    }

    fun reset() {
        lastVoiceActivityTime = System.currentTimeMillis()
    }
}
