# Backend Requirements (jarvis-gateway)

## New endpoint: POST /api/voice

### Description
Endpoint for processing voice commands from Android app. Accepts audio file, processes through OpenClaw (STT -> Agent -> TTS) and returns audio response.

### Request

```
POST /api/voice
Authorization: Bearer <token>
Content-Type: multipart/form-data

Body:
  - audio: file (audio/wav or audio/ogg)
```

### Response

**Success (200 OK)**
```
Content-Type: audio/ogg
Body: audio file (OGG/Opus) - TTS response
```

**Errors**
- `401 Unauthorized` - invalid or missing token
- `400 Bad Request` - missing or invalid audio file
- `500 Internal Server Error` - processing error

### Processing Logic

```go
func handleVoice(w http.ResponseWriter, r *http.Request) {
    // 1. Check authorization (Bearer token)
    // 2. Get audio file from multipart/form-data
    // 3. Save to temp file
    // 4. Call STT (Whisper): /usr/local/bin/whisper-stt
    // 5. Send text to OpenClaw agent: openclaw agent -m "<text>"
    // 6. Call TTS (Edge TTS): edge-tts --voice ru-RU-DmitryNeural --text "<response>" --write-media <output>
    // 7. Return audio file
    // 8. Clean up temp files
}
```

### VPS Dependencies (already installed)
- `/usr/local/bin/whisper-stt` - STT script (whisper-ctranslate2)
- `edge-tts` - TTS (pip package)
- `openclaw` - CLI agent

### Add to main.go

```go
http.HandleFunc("/api/voice", middleware.AuthMiddleware(handlers.VoiceHandler))
```

### Testing

```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "audio=@test.wav" \
  https://on-za-menya.online/api/voice \
  --output response.ogg

ffplay response.ogg
```
