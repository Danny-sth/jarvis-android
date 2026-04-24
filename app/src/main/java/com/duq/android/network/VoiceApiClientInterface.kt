package com.duq.android.network

import java.io.File

interface VoiceApiClientInterface {
    suspend fun sendVoiceCommand(
        serverUrl: String,
        authToken: String,
        audioFile: File,
        userId: String = ""  // Keycloak sub or telegram_id
    ): DuqApiClient.ApiResult
}
