package com.jarvis.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jarvis.android.R
import com.jarvis.android.data.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onSettingsSaved: () -> Unit) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val savedServerUrl by settingsRepository.serverUrl.collectAsState(initial = "")
    val savedAuthToken by settingsRepository.authToken.collectAsState(initial = "")
    val savedApiKey by settingsRepository.porcupineApiKey.collectAsState(initial = "")

    var serverUrl by remember { mutableStateOf("") }
    var authToken by remember { mutableStateOf("") }
    var porcupineApiKey by remember { mutableStateOf("") }

    LaunchedEffect(savedServerUrl) {
        if (serverUrl.isEmpty() && savedServerUrl.isNotEmpty()) serverUrl = savedServerUrl
    }
    LaunchedEffect(savedAuthToken) {
        if (authToken.isEmpty() && savedAuthToken.isNotEmpty()) authToken = savedAuthToken
    }
    LaunchedEffect(savedApiKey) {
        if (porcupineApiKey.isEmpty() && savedApiKey.isNotEmpty()) porcupineApiKey = savedApiKey
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.server_url)) },
                placeholder = { Text(stringResource(R.string.server_url_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = authToken,
                onValueChange = { authToken = it },
                label = { Text(stringResource(R.string.auth_token)) },
                placeholder = { Text(stringResource(R.string.auth_token_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = porcupineApiKey,
                onValueChange = { porcupineApiKey = it },
                label = { Text(stringResource(R.string.porcupine_api_key)) },
                placeholder = { Text(stringResource(R.string.porcupine_api_key_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get free API key at console.picovoice.ai",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    scope.launch {
                        settingsRepository.saveAllSettings(serverUrl, authToken, porcupineApiKey)
                        onSettingsSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serverUrl.isNotBlank() && authToken.isNotBlank() && porcupineApiKey.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
