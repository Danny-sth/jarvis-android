package com.jarvis.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val PORCUPINE_API_KEY = stringPreferencesKey("porcupine_api_key")
    }

    val serverUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SERVER_URL] ?: ""
        }

    val authToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] ?: ""
        }

    val porcupineApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PORCUPINE_API_KEY] ?: ""
        }

    val hasValidSettings: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val url = preferences[PreferencesKeys.SERVER_URL] ?: ""
            val token = preferences[PreferencesKeys.AUTH_TOKEN] ?: ""
            val apiKey = preferences[PreferencesKeys.PORCUPINE_API_KEY] ?: ""
            url.isNotBlank() && token.isNotBlank() && apiKey.isNotBlank()
        }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    suspend fun savePorcupineApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PORCUPINE_API_KEY] = apiKey
        }
    }

    suspend fun saveAllSettings(url: String, token: String, apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.PORCUPINE_API_KEY] = apiKey
        }
    }
}
