# Dependencies

> All libraries used in Jarvis Android with versions and purposes.

## Core Android

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.2.0 | Programming language |
| Kotlin Coroutines | 1.7.3 | Async programming |
| AndroidX Core KTX | 1.12.0 | Kotlin extensions for Android |
| AndroidX Lifecycle | 2.7.0 | Lifecycle-aware components |

## UI

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2024.02.00 | Compose version management |
| Compose Material3 | (BOM) | Material Design 3 components |
| Compose UI | (BOM) | Core Compose UI |
| Compose Navigation | 2.7.6 | Screen navigation |
| Activity Compose | 1.8.2 | Compose integration with Activity |

## Dependency Injection

| Library | Version | Purpose |
|---------|---------|---------|
| Hilt Android | 2.48 | DI framework |
| Hilt Compiler | 2.48 | Annotation processor |
| Hilt Navigation Compose | 1.1.0 | Hilt + Navigation integration |

## Network

| Library | Version | Purpose |
|---------|---------|---------|
| Ktor Client | 2.3.7 | HTTP client |
| Ktor Client OkHttp | 2.3.7 | OkHttp engine for Ktor |
| Ktor Content Negotiation | 2.3.7 | JSON serialization |
| Ktor Serialization JSON | 2.3.7 | Kotlinx serialization |

## Audio

| Library | Version | Purpose |
|---------|---------|---------|
| ExoPlayer | 1.2.1 | Audio playback (MP3, OGG) |

## Wake Word

| Library | Version | Purpose |
|---------|---------|---------|
| Porcupine Android | 3.0.2 | Wake word detection |

## VAD (Voice Activity Detection)

| Library | Version | Purpose |
|---------|---------|---------|
| ONNX Runtime | 1.16.3 | ML inference for Silero VAD |

## QR Code

| Library | Version | Purpose |
|---------|---------|---------|
| ML Kit Barcode | 17.2.0 | QR code scanning |
| CameraX | 1.3.1 | Camera API |

## Data Storage

| Library | Version | Purpose |
|---------|---------|---------|
| DataStore Preferences | 1.0.0 | Key-value storage |
| Kotlinx Serialization | 1.6.2 | JSON serialization |

## Build Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| Android Gradle Plugin | 8.2.0 | Android build |
| Kotlin Android | 2.2.0 | Kotlin support |
| Hilt Android Plugin | 2.48 | Hilt code generation |
| KSP | 2.2.0-2.0.0 | Kotlin Symbol Processing |

---

## Updating Dependencies

1. Update version in `gradle/libs.versions.toml`
2. Sync Gradle
3. Build and test: `./gradlew assembleDebug`
4. Check for deprecation warnings

## Critical Dependencies

These should NOT be updated without testing:

- **Porcupine** — Wake word accuracy may change
- **ONNX Runtime** — VAD model compatibility
- **ExoPlayer** — Audio playback format support
- **Hilt** — DI setup may break

---

*Updated: 2026-03-25*
