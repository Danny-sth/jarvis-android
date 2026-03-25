# Skill: Troubleshooting

> Common errors and their solutions.

## When to Use

- App crashes
- Build fails
- Features not working
- "не работает", "ошибка", "крашится"

---

## Build Errors

### Gradle Sync Failed

**Symptoms**: Android Studio shows sync error

**Solutions**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Invalidate caches (in Android Studio)
File → Invalidate Caches → Invalidate and Restart

# Delete .gradle folder
rm -rf .gradle/
rm -rf app/build/
./gradlew assembleDebug
```

### Hilt Compilation Error

**Symptoms**: `@HiltViewModel` or `@Inject` not recognized

**Solutions**:
1. Check `@HiltAndroidApp` on Application class
2. Check `@AndroidEntryPoint` on Activity
3. Verify KSP plugin in `build.gradle.kts`
4. Rebuild: `./gradlew clean assembleDebug`

### Compose Compiler Version Mismatch

**Symptoms**: Error about Compose compiler version

**Solution**: Check `gradle/libs.versions.toml`:
```toml
kotlin = "2.2.0"
# Compose compiler version must match Kotlin version
```

---

## Runtime Errors

### App Crashes on Start

**Debug**:
```bash
adb logcat -s AndroidRuntime:E
```

**Common Causes**:
1. Missing Hilt injection
2. Manifest not updated
3. Missing permission

### Wake Word Not Detecting

**Debug**:
```bash
adb logcat -s WakeWordManager:D
```

**Checklist**:
- [ ] Porcupine API key is set
- [ ] Microphone permission granted
- [ ] Service is running (check notification)
- [ ] Volume is up (some devices mute mic)

**Solution**:
```kotlin
// Check API key in WakeWordManager.kt
Log.d(TAG, "Access key: ${accessKey.take(10)}...")
```

### Recording Not Working

**Debug**:
```bash
adb logcat -s VoiceActivityDetector:D AudioRecorder:D
```

**Checklist**:
- [ ] RECORD_AUDIO permission granted
- [ ] No other app using mic
- [ ] VAD model loaded (check logs)

### API Calls Failing

**Debug**:
```bash
adb logcat -s JarvisApiClient:D
```

**Checklist**:
- [ ] Bearer token is set
- [ ] Network available
- [ ] Gateway is running
- [ ] Correct endpoint URL

**Test endpoint**:
```bash
curl -X POST https://on-za-menya.online/api/voice \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@test.wav"
```

### Audio Not Playing

**Debug**:
```bash
adb logcat -s AudioPlayer:D ExoPlayerImpl:D
```

**Checklist**:
- [ ] Response contains audio data
- [ ] Audio format is MP3/OGG
- [ ] Volume not muted
- [ ] ExoPlayer initialized

---

## Permission Errors

### Permission Denied

**Debug**:
```bash
adb logcat -s PermissionManager:D
```

**Reset permissions**:
```bash
adb shell pm revoke com.jarvis.android android.permission.RECORD_AUDIO
adb shell pm grant com.jarvis.android android.permission.RECORD_AUDIO
```

### Background Restrictions (MIUI/Samsung)

**Symptoms**: Service killed when app minimized

**Solutions**:
1. Disable battery optimization for app
2. Enable "Allow background activity"
3. Add to "Protected apps" (MIUI)

---

## Service Issues

### Service Not Starting

**Debug**:
```bash
adb logcat -s JarvisListenerService:D
```

**Check service state**:
```bash
adb shell dumpsys activity services com.jarvis.android
```

### Service Killed by System

**Symptoms**: Notification disappears, wake word stops

**Solution**: App-only mode is by design. Service only runs while app is visible.

---

## Memory Issues

### OutOfMemoryError

**Debug**:
```bash
adb logcat -s dalvikvm:E art:E
```

**Solutions**:
1. Check for memory leaks (ViewModel not clearing)
2. Release resources in `onDestroy()`
3. Use `largeHeap="true"` in Manifest (last resort)

---

## Device-Specific Issues

### Xiaomi/MIUI

- Auto-start permission required
- Battery saver kills services
- "Display pop-up windows" may be needed

### Samsung

- Device care may kill app
- Add to "Unmonitored apps"

### Huawei

- Battery optimization aggressive
- Add to "Protected apps"

---

## Useful Commands

```bash
# Full crash log
adb logcat -b crash

# App-specific logs
adb logcat --pid=$(adb shell pidof com.jarvis.android)

# Clear app data
adb shell pm clear com.jarvis.android

# Force stop
adb shell am force-stop com.jarvis.android

# Start app
adb shell am start -n com.jarvis.android/.MainActivity

# Check running services
adb shell dumpsys activity services | grep jarvis
```

---

## Quick Reference

| Problem | First Check | Log Tag |
|---------|-------------|---------|
| Wake word | API key, mic permission | `WakeWordManager` |
| Recording | RECORD_AUDIO permission | `VoiceActivityDetector` |
| API calls | Token, network | `JarvisApiClient` |
| Playback | Response format | `AudioPlayer` |
| Service | Notification visible | `JarvisListenerService` |
| Crash | Hilt, Manifest | `AndroidRuntime` |

---

*See also: `debug-voice.md` for voice pipeline issues*
