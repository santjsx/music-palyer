# iPod Modern (Android Edition) - System Architecture Specification

## 1. Architectural Overview & System Design

iPod Modern is architected around the principles of **Clean Architecture**, **Unidirectional Data Flow (UDF / MVI)**, and **Hardware-Direct Audio HAL Bypass**. The application decouples user interaction kinematics, library database indexing, local network sync services, and native real-time DSP audio pipelines.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           JETPACK COMPOSE UI LAYER                          │
│   ┌────────────────────────┐  ┌─────────────────┐  ┌────────────────────┐   │
│   │ DisplayScreen (Top)    │  │ HoldSwitch      │  │ CoverFlow 2.0 (3D) │   │
│   │ Menu / NowPlaying / EQ │  │ Safety Interlock│  │ Mirror Floor Alpha │   │
│   └───────────┬────────────┘  └────────┬────────┘  └─────────┬──────────┘   │
│               │                        │                     │              │
│   ┌───────────┴────────────────────────┴─────────────────────┴──────────┐   │
│   │                    ClickWheel Kinematics Engine                     │   │
│   │        Polar Coordinate Tracking (atan2) + Inertia Velocity         │   │
│   └────────────────────────────────────┬────────────────────────────────┘   │
└────────────────────────────────────────┼────────────────────────────────────┘
                                         │ UDF Events
┌────────────────────────────────────────▼────────────────────────────────────┐
│                             STATE & VIEWMODELS                              │
│   ┌───────────────────────┐ ┌──────────────────────┐ ┌──────────────────┐   │
│   │    PlayerViewModel    │ │    MenuViewModel     │ │  SyncViewModel   │   │
│   │ Queue, Transport, HUD │ │ Navigation Hierarchy │ │ Ktor HTTP Server │   │
│   └───────────┬───────────┘ └──────────┬───────────┘ └────────┬─────────┘   │
└───────────────┼────────────────────────┼──────────────────────┼─────────────┘
                │                        │                      │
┌───────────────▼────────────────────────▼──────────────────────▼─────────────┐
│                             CORE SERVICE LAYER                              │
│   ┌───────────────────────────┐ ┌───────────────────────────┐ ┌─────────┐   │
│   │   AudioPlaybackService    │ │    MediaStore & CUE Scan  │ │ Room DB │   │
│   │ MediaSession + Foreground │ │  Virtual Track Splitting  │ │ SQLite  │   │
│   └─────────────┬─────────────┘ └───────────────────────────┘ └────▲────┘   │
└─────────────────┼──────────────────────────────────────────────────┼────────┘
                  │ JNI (Native Audio Bridge)                        │ Upload
┌─────────────────▼──────────────────────────────────────────────────┴────────┐
│                        NATIVE C++ AUDIOPHILE ENGINE (NDK)                   │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ Oboe Audio Engine (SharingMode::Exclusive, LowLatency Performance)   │   │
│   │ ─────────────────────────────────────────────────────────────────── │   │
│   │ ┌───────────────────────────────┐ ┌───────────────────────────────┐ │   │
│   │ │ 10-Band Biquad IIR Matrix     │ │ Click Synthesizer (Zero-Lag)  │ │   │
│   │ │ Direct Form II + Headroom Reg │ │ Native Audio Sample Injection │ │   │
│   │ └───────────────────────────────┘ └───────────────────────────────┘ │   │
│   │ ┌───────────────────────────────┐ ┌───────────────────────────────┐ │   │
│   │ │ Audio Decoder (PCM/FLAC/DSD)  │ │ TagLib / Metadata Inspector   │ │   │
│   │ └───────────────────────────────┘ └───────────────────────────────┘ │   │
│   └───────────────────────────────────┬─────────────────────────────────┘   │
└───────────────────────────────────────┼─────────────────────────────────────┘
                                        │ AAudio Driver Bypass
                                        ▼
                               [ Audio DAC / USB HAL ]
```

---

## 2. Component Specifications

### 2.1 Native Audiophile Audio Engine
- **Framework**: Google Oboe C++ Library utilizing the Android AAudio HAL backend.
- **Stream Sharing Mode**: `oboe::SharingMode::Exclusive`. This bypasses Android `AudioFlinger` software mixing and resampling, granting the audio thread direct access to the audio hardware MMAP ring buffer.
- **Performance Mode**: `oboe::PerformanceMode::LowLatency` with `SCHED_FIFO` real-time thread scheduling.
- **Audio Sample Formats**: 32-bit floating-point PCM internally (`float32`), automatically negotiated up to 384kHz / 32-bit or DSD over PCM (DoP v1.1).
- **Zero-Latency Click Synthesis**: When the click wheel is rotated, a mechanical click pulse is synthesized or retrieved directly inside the native audio render callback loop, guaranteeing sub-millisecond audio response without JVM thread dispatch overhead.

### 2.2 10-Band Direct Form II Cascaded Biquad EQ
- **Filter Topology**: 10 cascaded second-order Direct Form II Transposed Infinite Impulse Response (IIR) filter sections.
- **Frequency Nodes**: 31.25Hz, 62.5Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz.
- **Dynamic Headroom Regulator**: Continuously monitors the maximum positive gain across all 10 bands ($G_{\text{max}} = \max(0, g_1, \dots, g_{10})$) and applies an automatic pre-attenuation gain $G_{\text{pre}} = -G_{\text{max}}$ to prevent digital clipping in 0dBFS float conversions.

### 2.3 Polar Kinematics Click Wheel & Haptic Subsystem
- **Coordinate Model**: Converts touch $(x, y)$ relative to wheel center $(x_0, y_0)$ into polar angles:
  $$\theta = \text{atan2}(y - y_0, x - x_0)$$
- **Phase Unwrapping**: Tracks angular delta $\Delta \theta = \theta_t - \theta_{t-1}$ with branch cut correction around $\pm \pi$:
  $$\Delta \theta_{\text{unwrapped}} = \Delta \theta - 2\pi \cdot \text{round}\left(\frac{\Delta \theta}{2\pi}\right)$$
- **Tactile Actuation**: Accumulates angular displacement; every $15^\circ$ ($\pi / 12$ radians) fires `VibrationEffect.Composition.PRIMITIVE_TICK` on the device's Linear Resonant Actuator (LRA).
- **Inertial Momentum**: Upon touch release, angular velocity $\omega_0$ decays via exponential damping: $\omega(t) = \omega_0 e^{-\gamma t}$, continuing list navigation until $\omega(t) < \epsilon$.

### 2.4 Cover Flow 2.0 Spatial 3D Graphic Layer
- **Projection Parameters**:
  - Distance: `cameraDistance = 16f`
  - Selected Album: `rotationY = 0°`, `scale = 1.05`, `z-index = 100`
  - Side Albums: `rotationY = ±55°`, `scale = 0.75`, `translationX = ±(offset * 70dp)`, `z-index = 100 - |offset|`
- **Dynamic Mirror Floor**: Renders a vertical flip of the album artwork card with an alpha gradient mask fading from `0.45` to `0.0` across 80dp.

### 2.5 Ingestion, CUE Sheet Splitting & Wi-Fi Sync Node
- **Embedded Ktor Server**: Lightweight asynchronous server running on port 8080.
- **Web UI Portal**: Single-page application served from raw assets providing a drag-and-drop interface for `.flac`, `.alac`, `.wav`, `.mp3`, `.m4a`, and `.cue` files.
- **CUE Sheet Parser**: Parses standard CDRWIN CUE format (`PERFORMER`, `TITLE`, `FILE`, `TRACK`, `INDEX 00/01`) converting `mm:ss:ff` frames ($1\text{ frame} = 1/75\text{ sec} = 13.333\text{ ms}$) to precise sample offsets.

---

## 3. Threading & Concurrency Model

1. **Audio Real-Time Thread (`SCHED_FIFO`)**: Executes the Oboe `onAudioReady` callback. Non-blocking; no heap allocations or mutex locks in the hot loop.
2. **UI Thread (Main Looper)**: Jetpack Compose rendering, 120Hz frame pacing.
3. **Kinematics & Gesture Coroutine Scope**: Processes high-frequency touch events, calculating velocity and haptic triggers.
4. **I/O Coroutine Scope**: Handles Room SQLite queries, MediaStore scanning, file decoding, and Ktor Wi-Fi socket transmissions.
