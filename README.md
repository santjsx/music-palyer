# 🎧 iPod Modern (Android Edition)

> **Audiophile-Grade Local Music Player with Hardware Skeuomorphism & AAudio HAL Direct Exclusive Stream**

![iPod Modern Badge](https://img.shields.io/badge/Platform-Android%2010%20to%2016-blue.svg)
![NDK C++20](https://img.shields.io/badge/Engine-C%2B%2B20%20%2F%20Oboe-red.svg)
![DSP](https://img.shields.io/badge/DSP-10--Band%20Cascaded%20Biquad-green.svg)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg)

---

## 📖 Overview

**iPod Modern** is an uncompromising, hyper-realistic tribute to the definitive era of portable hardware audio, engineered exclusively for modern Android devices.

By bypassing Android's software mixing frameworks (`AudioFlinger`), the player delivers **bit-perfect audio directly to external USB DACs or headphones** via `AAudio SharingMode::Exclusive`. It pairs this with a zero-latency hardware click wheel kinematics engine, Linear Resonant Actuator (LRA) haptic primitives, spatial 3D Cover Flow 2.0, an embedded Ktor Wi-Fi music sync portal, and a 10-band Direct Form II Cascaded Biquad IIR Parametric Equalizer with dynamic headroom regulation.

---

## 🛠️ Key Architectural Pillars

### 1. 🕹️ Hardware Skeuomorph & Polar Kinematics
- **Continuous Orbit Tracking**: Polar coordinate equations ($\theta = \text{atan2}(y - y_0, x - x_0)$) with multi-quadrant phase unwrapping.
- **LRA Haptic Primitives**: $15^\circ$ discrete step triggers mapping to `VibrationEffect.Composition.PRIMITIVE_TICK` (Android 12+) and `PRIMITIVE_CLICK` on center action.
- **Zero-Latency Native Sound Click**: Injected directly into the native C++ audio stream buffer with microsecond precision.
- **Hold Switch Interlock**: Sliding physical top switch deactivates touch events to prevent pocket misfires.

### 2. 🔊 Audiophile Native C++ Audio Engine (Oboe / AAudio)
- **Direct HAL Bypass**: Runs stream in `SharingMode::Exclusive` and `PerformanceMode::LowLatency` (`SCHED_FIFO` real-time scheduling).
- **Bit-Perfect Lossless Playback**: Native stream locks to the loaded audio's native clock rate (up to 32-bit/384kHz, DSD over PCM / DoP).
- **Zero-Phase Distortion 10-Band Biquad IIR DSP**:
  - 10 Anchor Nodes: `31.25Hz`, `62.5Hz`, `125Hz`, `250Hz`, `500Hz`, `1kHz`, `2kHz`, `4kHz`, `8kHz`, `16kHz`.
  - **Dynamic Headroom Regulator**: Automatically calculates peak band boost and scales pre-cut digital attenuation to guarantee 0dBFS inter-sample safety.

### 3. 💿 Cover Flow 2.0 3D Spatial Layout
- **Dynamic 3D Camera Projection**: Perspective matrix transformation in Jetpack Compose (`graphicsLayer { cameraDistance = 16f, rotationY = ±55° }`).
- **Dynamic Mirror Floor Reflection**: Inverted vertical alpha-gradient mirror floor fading naturally into dark stadium staging.

### 4. 🌐 Embedded Wi-Fi Sync Server & CUE Sheet Splitting
- **Embedded Ktor CIO HTTP Server**: Host `http://<device-ip>:8080` locally for drag-and-drop file ingestion from any desktop browser without cords or companion software.
- **CUE Sheet Frame Parser**: Automatic conversion of Red Book CD frames ($1\text{ frame} = 1000/75\text{ ms}$) into virtual track partitions for single-file vinyl or CD image transfers.
- **Synchronized Lyrics**: Sub-second scrolling USLT/LRC synchronized lyrics renderer.

---

## 📂 Project Directory Structure

```
music-app/
├── docs/
│   ├── ARCHITECTURE.md                  # Detailed clean architecture specification
│   ├── DSP_EQUATIONS_WHITEPAPER.md      # Mathematical derivation of Biquad IIR & Headroom
│   ├── UI_UX_DESIGN_SPEC.md             # Skeuomorphic design tokens & color palettes
│   └── WI_FI_SYNC_API.md                # Embedded Ktor HTTP upload API specification
├── app/
│   ├── CMakeLists.txt                   # NDK build configuration
│   ├── src/main/
│   │   ├── cpp/                         # Native C++ Audiophile Engine
│   │   │   ├── audio_engine.h/.cpp      # Oboe stream loop & mixer
│   │   │   ├── biquad_filter.h/.cpp     # Direct Form II Transposed filter
│   │   │   ├── dsp_equalizer.h/.cpp     # 10-band cascaded equalizer
│   │   │   ├── click_synthesizer.h/.cpp # Mechanical click impulse model
│   │   │   ├── native_tag_inspector.h   # Container tag inspector
│   │   │   └── jni_bridge.cpp           # JNI bindings
│   │   └── java/com/ipodmodern/audio/
│   │       ├── core/
│   │       │   ├── audio/               # Native bridge & Foreground Service
│   │       │   ├── database/            # Room Database & DAOs
│   │       │   ├── haptics/             # Kinematics & LRA Haptic Engine
│   │       │   ├── model/               # Immutable Domain models
│   │       │   ├── parser/              # CUE Sheet & Lyrics parsers
│   │       │   └── sync/                # Embedded Ktor Web Server
│   │       └── ui/
│   │           ├── components/          # ClickWheel, HoldSwitch, Badges
│   │           ├── screens/             # DisplayScreen, CoverFlow, EQ, Menu
│   │           ├── theme/               # Colors, Typography, Shaders
│   │           ├── viewmodel/           # MVI / UDF ViewModels
│   │           └── MainActivity.kt      # Main Entry Point
│   └── src/test/                        # Unit tests for Kinematics & Parsers
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Koala or newer
- Android SDK Platform 35 (API 35+)
- Android NDK (r26+ recommended)
- CMake 3.22.1+

### CLI Build
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```

---

## 📜 Documentation Reference
- 📑 [System Architecture Specification](docs/ARCHITECTURE.md)
- 🔬 [DSP Biquad Equations Whitepaper](docs/DSP_EQUATIONS_WHITEPAPER.md)
- 🎨 [UI/UX Skeuomorphic Design Tokens](docs/UI_UX_DESIGN_SPEC.md)
- 🌐 [Local Wi-Fi Sync Server API](docs/WI_FI_SYNC_API.md)
