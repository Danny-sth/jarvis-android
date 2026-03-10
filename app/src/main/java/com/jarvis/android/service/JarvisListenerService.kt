package com.jarvis.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jarvis.android.JarvisApplication
import com.jarvis.android.JarvisState
import com.jarvis.android.MainActivity
import com.jarvis.android.R
import com.jarvis.android.audio.AudioPlayer
import com.jarvis.android.audio.AudioRecorder
import com.jarvis.android.data.SettingsRepository
import com.jarvis.android.network.JarvisApiClient
import com.jarvis.android.wakeword.WakeWordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class JarvisListenerService : Service() {

    companion object {
        private const val TAG = "JarvisListenerService"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.jarvis.android.START"
        const val ACTION_STOP = "com.jarvis.android.STOP"
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var settingsRepository: SettingsRepository
    private var wakeWordManager: WakeWordManager? = null
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private val apiClient = JarvisApiClient()

    private var wakeLock: PowerManager.WakeLock? = null

    private val _state = MutableStateFlow(JarvisState.IDLE)
    val state: StateFlow<JarvisState> = _state

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    inner class LocalBinder : Binder() {
        fun getService(): JarvisListenerService = this@JarvisListenerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        settingsRepository = SettingsRepository(this)
        audioRecorder = AudioRecorder(this)
        audioPlayer = AudioPlayer(this)
        audioPlayer.initialize()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForegroundServiceWithNotification()
                initializeWakeWord()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, JarvisListenerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, JarvisApplication.CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getNotificationText())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(0, getString(R.string.stop_service), stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun getNotificationText(): String {
        return when (_state.value) {
            JarvisState.IDLE -> getString(R.string.notification_text)
            JarvisState.LISTENING, JarvisState.RECORDING -> getString(R.string.status_listening)
            JarvisState.PROCESSING -> getString(R.string.status_processing)
            JarvisState.PLAYING -> getString(R.string.status_playing)
            JarvisState.ERROR -> getString(R.string.status_error)
        }
    }

    private fun updateNotification() {
        val notification = createNotification()
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun initializeWakeWord() {
        serviceScope.launch {
            try {
                val apiKey = settingsRepository.porcupineApiKey.first()
                if (apiKey.isBlank()) {
                    _state.value = JarvisState.ERROR
                    _errorMessage.value = "Porcupine API key not configured"
                    return@launch
                }
                wakeWordManager = WakeWordManager(
                    context = this@JarvisListenerService,
                    accessKey = apiKey,
                    onWakeWordDetected = { onWakeWordDetected() },
                    onError = { error ->
                        _state.value = JarvisState.ERROR
                        _errorMessage.value = error
                    }
                )
                wakeWordManager?.start()
                _state.value = JarvisState.IDLE
                Log.d(TAG, "Wake word manager started")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing wake word", e)
                _state.value = JarvisState.ERROR
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun onWakeWordDetected() {
        Log.d(TAG, "Wake word detected!")
        serviceScope.launch { processVoiceCommand() }
    }

    private suspend fun processVoiceCommand() {
        try {
            wakeWordManager?.stop()
            _state.value = JarvisState.LISTENING
            updateNotification()

            val audioFile = File(cacheDir, "voice_command.wav")
            val success = audioRecorder.recordUntilSilence(audioFile)

            if (!success || !audioFile.exists() || audioFile.length() == 0L) {
                Log.e(TAG, "Recording failed")
                restartWakeWord()
                return
            }

            Log.d(TAG, "Recording complete: ${audioFile.length()} bytes")

            _state.value = JarvisState.PROCESSING
            updateNotification()

            val serverUrl = settingsRepository.serverUrl.first()
            val authToken = settingsRepository.authToken.first()
            val result = apiClient.sendVoiceCommand(serverUrl, authToken, audioFile)

            when (result) {
                is JarvisApiClient.ApiResult.Success -> {
                    Log.d(TAG, "Received response: ${result.audioData.size} bytes")
                    _state.value = JarvisState.PLAYING
                    updateNotification()
                    audioPlayer.playAudio(result.audioData)
                }
                is JarvisApiClient.ApiResult.Error -> {
                    Log.e(TAG, "API error: ${result.message}")
                    _errorMessage.value = result.message
                }
            }

            audioFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing voice command", e)
            _errorMessage.value = "Error: ${e.message}"
        } finally {
            restartWakeWord()
        }
    }

    private fun restartWakeWord() {
        serviceScope.launch {
            try {
                val apiKey = settingsRepository.porcupineApiKey.first()
                wakeWordManager = WakeWordManager(
                    context = this@JarvisListenerService,
                    accessKey = apiKey,
                    onWakeWordDetected = { onWakeWordDetected() },
                    onError = { error ->
                        _state.value = JarvisState.ERROR
                        _errorMessage.value = error
                    }
                )
                wakeWordManager?.start()
                _state.value = JarvisState.IDLE
                updateNotification()
            } catch (e: Exception) {
                Log.e(TAG, "Error restarting wake word", e)
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Jarvis::WakeWordLock")
        wakeLock?.acquire()
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        wakeWordManager?.stop()
        audioRecorder.stopRecording()
        audioPlayer.release()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }
}
