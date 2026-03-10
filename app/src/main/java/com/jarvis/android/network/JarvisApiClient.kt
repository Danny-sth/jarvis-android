package com.jarvis.android.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class JarvisApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    sealed class ApiResult {
        data class Success(val audioData: ByteArray) : ApiResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as Success
                return audioData.contentEquals(other.audioData)
            }

            override fun hashCode(): Int = audioData.contentHashCode()
        }
        data class Error(val message: String, val code: Int? = null) : ApiResult()
    }

    suspend fun sendVoiceCommand(
        serverUrl: String,
        authToken: String,
        audioFile: File
    ): ApiResult = withContext(Dispatchers.IO) {
        try {
            val mediaType = "audio/wav".toMediaType()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "audio",
                    audioFile.name,
                    audioFile.asRequestBody(mediaType)
                )
                .build()

            val url = serverUrl.trimEnd('/') + "/api/voice"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.bytes()
                if (body != null && body.isNotEmpty()) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                ApiResult.Error(errorBody, response.code)
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }
}
