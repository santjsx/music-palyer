# iPod Modern - Local Wi-Fi Sync Server & Web Portal API

## 1. Overview

The **Embedded Wi-Fi Sync Engine** runs a zero-configuration, lightweight Ktor HTTP server on the Android device. Users connect any browser on their local Wi-Fi network (e.g. `http://192.168.1.150:8080`) to drag-and-drop audio files and CUE sheets directly into the device's indexed storage.

---

## 2. Server Architecture & Endpoints

### 2.1 HTTP Endpoints

| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Serves the single-page Web Upload Portal | `text/html; charset=utf-8` |
| `GET` | `/api/status` | Returns device storage, library count & state | `application/json` |
| `POST` | `/api/upload` | Multipart file upload endpoint | `application/json` |
| `GET` | `/api/tracks` | Lists recently uploaded / indexed tracks | `application/json` |
| `DELETE`| `/api/track/{id}` | Removes a track from device storage | `application/json` |

---

## 3. API Schemas

### 3.1 Device Status (`GET /api/status`)
```json
{
  "deviceName": "iPod Modern (Pixel 9 Pro)",
  "ipAddress": "192.168.1.150",
  "port": 8080,
  "storageFreeBytes": 134217728000,
  "storageTotalBytes": 256000000000,
  "totalTracks": 1420,
  "totalAlbums": 118,
  "totalArtists": 64,
  "isScanning": false
}
```

### 3.2 File Upload (`POST /api/upload`)
- **Encoding**: `multipart/form-data`
- **Supported File Types**:
  - Audio: `.flac`, `.alac`, `.wav`, `.aiff`, `.dsf`, `.dff`, `.mp3`, `.m4a`, `.aac`, `.ogg`, `.opus`
  - Metadata: `.cue`, `.lrc`
  - Artwork: `cover.jpg`, `folder.png`, `front.webp`

#### Upload Response:
```json
{
  "status": "success",
  "uploadedFiles": [
    {
      "filename": "Pink_Floyd_Time.flac",
      "sizeBytes": 68420120,
      "format": "FLAC",
      "sampleRate": 96000,
      "bitDepth": 24,
      "channels": 2,
      "durationMs": 425000,
      "artist": "Pink Floyd",
      "album": "The Dark Side of the Moon",
      "title": "Time"
    }
  ]
}
```

---

## 4. CUE Sheet Ingestion Workflow

When a `.cue` file is uploaded alongside a continuous image file (e.g. `album.flac`), the ingestion pipeline:
1. Validates the referenced target audio filename in `FILE "..." WAVE`.
2. Computes the offset of each `TRACK 01`, `TRACK 02` based on `INDEX 01 mm:ss:ff`.
3. Creates individual virtual database entries for each track, mapping the parent file URI with start and end millisecond boundaries.
4. Updates the Room database reactively, instantly making the individual tracks visible in the Artist/Album hierarchy on the iPod display.
