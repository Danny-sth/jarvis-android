package com.duq.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duq.android.audio.ChatAudioPlaybackManager
import com.duq.android.data.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
}

/**
 * Hilt entry point for accessing singletons in Composables
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DuqAppEntryPoint {
    fun chatAudioPlaybackManager(): ChatAudioPlaybackManager
}

@Composable
fun DuqApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    // Get ChatAudioPlaybackManager via Hilt entry point
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, DuqAppEntryPoint::class.java)
    }
    val audioPlaybackManager = remember { entryPoint.chatAudioPlaybackManager() }

    // Initialize audio player on start, release on dispose
    DisposableEffect(Unit) {
        audioPlaybackManager.initialize()
        onDispose {
            audioPlaybackManager.release()
        }
    }

    val hasValidSettings by settingsRepository.hasValidSettings.collectAsState(initial = null)

    val startDestination = when (hasValidSettings) {
        true -> Screen.Main.route
        false -> Screen.Settings.route
        null -> Screen.Settings.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                audioPlaybackManager = audioPlaybackManager
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onSettingsSaved = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
