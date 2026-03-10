package com.jarvis.android.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarvis.android.JarvisState
import com.jarvis.android.R
import com.jarvis.android.service.JarvisListenerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }
    var service by remember { mutableStateOf<JarvisListenerService?>(null) }

    val state by service?.state?.collectAsState() ?: remember { mutableStateOf(JarvisState.IDLE) }
    val errorMessage by service?.errorMessage?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as JarvisListenerService.LocalBinder).getService()
                isServiceRunning = true
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                isServiceRunning = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startService(context)
            context.bindService(
                Intent(context, JarvisListenerService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, JarvisListenerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            try { context.unbindService(serviceConnection) } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jarvis") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatusIndicator(state = if (isServiceRunning) state else null)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = getStatusText(isServiceRunning, state),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    if (isServiceRunning) {
                        stopService(context)
                        isServiceRunning = false
                        service = null
                    } else {
                        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                },
                colors = if (isServiceRunning) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Text(text = stringResource(if (isServiceRunning) R.string.stop_service else R.string.start_service))
            }
        }
    }
}

@Composable
private fun StatusIndicator(state: JarvisState?) {
    val color by animateColorAsState(
        targetValue = when (state) {
            JarvisState.IDLE -> Color(0xFF4CAF50)
            JarvisState.LISTENING, JarvisState.RECORDING -> Color(0xFF2196F3)
            JarvisState.PROCESSING -> Color(0xFFFF9800)
            JarvisState.PLAYING -> Color(0xFF9C27B0)
            JarvisState.ERROR -> Color(0xFFF44336)
            null -> Color.Gray
        },
        animationSpec = tween(300), label = "color"
    )
    val scale by animateFloatAsState(
        targetValue = when (state) {
            JarvisState.LISTENING, JarvisState.RECORDING -> 1.2f
            JarvisState.PROCESSING -> 1.1f
            else -> 1.0f
        },
        animationSpec = tween(300), label = "scale"
    )
    Box(
        modifier = Modifier.size(120.dp).scale(scale).background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (state) {
                JarvisState.IDLE -> "J"
                JarvisState.LISTENING, JarvisState.RECORDING, JarvisState.PROCESSING -> "..."
                JarvisState.PLAYING -> ">"
                JarvisState.ERROR -> "!"
                null -> "-"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
    }
}

@Composable
private fun getStatusText(isRunning: Boolean, state: JarvisState): String {
    return if (!isRunning) stringResource(R.string.status_service_stopped)
    else when (state) {
        JarvisState.IDLE -> stringResource(R.string.status_idle)
        JarvisState.LISTENING, JarvisState.RECORDING -> stringResource(R.string.status_listening)
        JarvisState.PROCESSING -> stringResource(R.string.status_processing)
        JarvisState.PLAYING -> stringResource(R.string.status_playing)
        JarvisState.ERROR -> stringResource(R.string.status_error)
    }
}

private fun startService(context: Context) {
    val intent = Intent(context, JarvisListenerService::class.java).apply { action = JarvisListenerService.ACTION_START }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
    else context.startService(intent)
}

private fun stopService(context: Context) {
    val intent = Intent(context, JarvisListenerService::class.java).apply { action = JarvisListenerService.ACTION_STOP }
    context.startService(intent)
}
