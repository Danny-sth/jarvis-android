# Session Notes

> Persistent memory between Claude Code sessions.

## Current Work

_Empty - add notes as you work_

## Decisions Made

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-03-25 | Renamed vtoroy-android → jarvis-android | Full rebrand, package name changed |
| 2026-03-25 | Using "JARVIS" wake word | Porcupine built-in, best quality vs custom |
| 2026-03-25 | App-only mode (no background) | Battery efficiency, user control |
| 2026-03-25 | Created full .claude/ structure | Aligned with vtoroy project standards |

## Known Issues

| Issue | Status | Notes |
|-------|--------|-------|
| MIUI battery optimization | Workaround | AccessibilityService helps |
| First wake word slow | Known | Porcupine model loading ~500ms |

## Architecture Decisions

- **Wake Word**: Porcupine SDK with built-in "JARVIS" keyword (best accuracy)
- **VAD**: Silero ONNX (2 second silence detection, 10s max recording)
- **Audio Playback**: ExoPlayer (Media3) for OGG support
- **Auth**: QR code scan → Bearer token stored in DataStore
- **DI**: Hilt (standard Android DI)
- **UI**: Jetpack Compose + Material3
- **Network**: OkHttp (simple, reliable)

## Documentation Structure

```
.claude/
├── CLAUDE.md              # Quick reference (start here)
├── NOTES.md               # This file
├── settings.json          # Hooks
├── docs/                  # Reference documentation
│   ├── architecture.md    # Tech stack, structure
│   ├── services.md        # Android services
│   └── dependencies.md    # Libraries
├── skills/                # How-to guides
│   ├── build-deploy.md    # Build and install
│   ├── debug-voice.md     # Voice pipeline
│   ├── add-feature.md     # Adding features
│   └── troubleshooting.md # Error solutions
└── specs/                 # Templates
    ├── plans/
    ├── requirements/
    └── tasks/
```

## Quick Commands

```bash
# Build + Install + Logs
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk && adb logcat -s JarvisListenerService:D

# Set auth token
adb shell am broadcast -a com.jarvis.android.SET_AUTH --es token "mob_xxx"

# Set Porcupine key
adb shell am broadcast -a com.jarvis.android.SET_AUTH --es porcupine_key "KEY"
```

---

_Update this file when making important decisions or discovering issues._
