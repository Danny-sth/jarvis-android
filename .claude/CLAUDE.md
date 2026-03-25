# Jarvis Android

> Mobile voice assistant client for Vtoroy AI.

## Critical Rules

1. **ALWAYS build and test** after code changes
2. **CHECK LOGS** before saying "done" — `adb logcat -s JarvisListenerService:D`
3. **Wake word is "JARVIS"** — Porcupine built-in, do not change
4. **App-only mode** — mic disabled when minimized (by design)

## Quick Deploy

```bash
# One-liner: build + install + logs
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk && adb logcat -s JarvisListenerService:D WakeWordManager:D
```

## Architecture

```
Wake Word (Porcupine "JARVIS")
       ↓
JarvisListenerService (foreground)
       ↓
VoiceCommandProcessor
       ↓ record
VoiceActivityDetector (Silero VAD, 2s silence)
       ↓
JarvisApiClient (POST /api/voice)
       ↓
AudioPlayer (ExoPlayer)
```

## Key Files

| File | Purpose |
|------|---------|
| `service/JarvisListenerService.kt` | Main foreground service, lifecycle |
| `service/VoiceCommandProcessor.kt` | Recording → API → Playback flow |
| `wakeword/WakeWordManager.kt` | Porcupine wrapper, wake word detection |
| `audio/VoiceActivityDetector.kt` | Silero VAD, silence detection |
| `network/JarvisApiClient.kt` | HTTP client, /api/voice endpoint |
| `ui/MainScreen.kt` | Compose UI, state display |
| `ui/SettingsScreen.kt` | QR scan, API key input |

## Package Structure

```
com.jarvis.android/
├── JarvisApplication.kt      # Hilt app entry
├── audio/                    # VAD, AudioPlayer, AudioRecorder
├── di/                       # Hilt modules
├── model/                    # JarvisState enum
├── network/                  # JarvisApiClient
├── service/                  # Foreground services
├── ui/                       # Compose screens
├── util/                     # Constants, permissions
└── wakeword/                 # Porcupine manager
```

## Backend Integration

**Gateway**: `vtoroy-gateway` on VPS
**Endpoint**: `POST /api/voice`
**Auth**: Bearer token (from QR code)

## Detailed Docs

| Doc | Content |
|-----|---------|
| `docs/architecture.md` | Full tech stack, state machine, voice pipeline |
| `docs/services.md` | All Android services explained |
| `docs/dependencies.md` | Libraries, versions, purpose |

## Skills

| Skill | When to use |
|-------|-------------|
| `build-deploy.md` | Build APK, install, view logs |
| `debug-voice.md` | Wake word or voice pipeline issues |
| `add-feature.md` | Adding new functionality |
| `troubleshooting.md` | Common errors and fixes |

## Common Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Logs (all relevant tags)
adb logcat -s JarvisListenerService:D WakeWordManager:D VoiceActivityDetector:D JarvisApiClient:D VoiceCommandProcessor:D

# Clear app data
adb shell pm clear com.jarvis.android

# Uninstall
adb uninstall com.jarvis.android
```

## Environment

- **Kotlin**: 2.2.0
- **Compose**: Material3
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

---

_See NOTES.md for session memory and decisions._
