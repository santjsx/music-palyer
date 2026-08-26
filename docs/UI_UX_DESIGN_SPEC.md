# iPod Modern - UI/UX Design System Specification

## 1. Visual Skeuomorphism Philosophy

iPod Modern synthesizes nostalgic 2000s industrial product design with ultra-crisp 2026 mobile display ergonomics. It avoids flat design complacency in favor of physical tactility, depth, brushed textures, bevel reflections, and sub-pixel micro-interactions.

---

## 2. Color Palettes & Chassis Materials

### 2.1 Classic Silver & White (1st - 5th Gen Heritage)
- **Chassis Body**: `#ECECEC` with radial gradient highlight `#FFFFFF`
- **Click Wheel**: Matte Light Gray `#D6D6D6` with embossed border `#B8B8B8`
- **Center Button**: Polished White `#F7F7F7`
- **LCD Bezel**: High-gloss obsidian `#121212` with 2px inner shadow
- **Screen Display**: Pure White `#FFFFFF` / Retro Monochrome LCD `#C7D4A8` (with sub-pixel dot-matrix grid)

### 2.2 Space Titanium (Audiophile Flagship Edition)
- **Chassis Body**: Dark Anodized Titanium `#24272C` with brushed micro-texture
- **Click Wheel**: Carbon Matte `#1B1D21`
- **Center Button**: Sunburst Brushed Steel `#3A3F47`
- **Accent Highlight**: Audiophile Gold `#D4AF37` / Lossless Cyan `#00F5D4`

### 2.3 Stealth Obsidian (Black Edition)
- **Chassis Body**: Deep Matte Black `#101114`
- **Click Wheel**: Textured Charcoal `#1E2024`
- **Screen Display**: True OLED Black `#000000` with high-contrast Crisp White typography `#FAFAFA`

---

## 3. Typography & Information Hierarchy

- **Header / Status Bar**: `San Francisco Display` / `Inter` Medium 12sp, `#8E8E93` (Battery, Play Icon, Title, Lock/Hold Icon)
- **Menu Items**: `Inter` SemiBold 16sp, `#1C1C1E` (Active Selection: `#FFFFFF` with Vibrant Blue pill gradient `#007AFF` to `#0055B3`)
- **Lossless HUD Badges**:
  - `LOSSY`: Background `#3A3A3C`, Text `#8E8E93` (MP3/AAC)
  - `LOSSLESS`: Background `#1C3A27`, Text `#34C759` (16-bit 44.1/48kHz)
  - `HI-RES LOSSLESS`: Background `#3D2E0B`, Text `#FFD60A`, Border `#FFD60A` (24-bit/192kHz/DSD)

---

## 4. Click Wheel Kinematics & Touch Ergonomics

```
               [ MENU ]
                 0° (12:00)
                    ▲
                    │
   [ |<< ]          ● Center Action        [ >>| ]
270° (09:00) ◄─────( )─────►              90° (03:00)
                    │
                    ▼
               [ > / || ]
                180° (06:00)
```

- **Wheel Diameter**: Fixed $240\text{ dp}$ to $280\text{ dp}$ depending on screen aspect ratio.
- **Deadzone Buffer**: Central radius $r < 42\text{ dp}$ (triggers Center Push Action).
- **Active Orbit Ring**: $42\text{ dp} \le r \le 130\text{ dp}$.
- **Haptic Feedback**:
  - Angular displacement delta $\Delta \theta = 15^\circ \implies$ `PRIMITIVE_TICK` (Micro-click feel).
  - Center button push $\implies$ `PRIMITIVE_CLICK` (Solid mechanical switch actuation).
  - Navigation limits (top/bottom of list) $\implies$ `PRIMITIVE_THUD` (End-of-travel spring bounce).

---

## 5. Cover Flow 2.0 3D Layout Geometry

```
           [ -2 ]           [ -1 ]           [  0  ]           [ +1 ]           [ +2 ]
        Angle: +55°      Angle: +50°       Angle: 0°        Angle: -50°      Angle: -55°
         Scale: 0.7       Scale: 0.85      Scale: 1.05       Scale: 0.85      Scale: 0.7
         Z-Index: 8       Z-Index: 9       Z-Index: 10       Z-Index: 9       Z-Index: 8
```

- **Perspective Rendering**:
  - `cameraDistance = 16f`
  - Dynamic `rotationY` computed continuously based on horizontal scroll offset.
  - Inverted Mirror Floor: Vertical scale `scaleY = -1f`, Alpha gradient mask fading to transparent at $y = 80\text{dp}$.
- **Wheel Coupling**: Continuous rotational velocity $\omega$ on the Click Wheel directly drives the Cover Flow carousel with natural inertial deceleration.
