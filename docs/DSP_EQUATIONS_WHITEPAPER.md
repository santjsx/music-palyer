# Audiophile Digital Signal Processing Whitepaper: 10-Band Biquad IIR Engine

## 1. Introduction

The **iPod Modern DSP Engine** implements a studio-grade, 10-band parametric equalizer using **Direct Form II Transposed Cascaded Biquad Infinite Impulse Response (IIR)** filter sections.

---

## 2. Mathematical Foundation of Biquad IIR Filters

The general continuous-time second-order transfer function in the $s$-plane is mapped to the discrete-time $z$-plane using the **Bilinear Transform**:
$$s \leftarrow \frac{2}{T} \frac{1 - z^{-1}}{1 + z^{-1}} = 2f_s \frac{1 - z^{-1}}{1 + z^{-1}}$$

The resulting discrete-time transfer function $H(z)$ is:
$$H(z) = \frac{b_0 + b_1 z^{-1} + b_2 z^{-2}}{a_0 + a_1 z^{-1} + a_2 z^{-2}} = \frac{\frac{b_0}{a_0} + \frac{b_1}{a_0} z^{-1} + \frac{b_2}{a_0} z^{-2}}{1 + \frac{a_1}{a_0} z^{-1} + \frac{a_2}{a_0} z^{-2}}$$

Normalized coefficients:
$$H(z) = \frac{b_0' + b_1' z^{-1} + b_2' z^{-2}}{1 + a_1' z^{-1} + a_2' z^{-2}}$$

---

## 3. Direct Form II Transposed Structure

The **Direct Form II Transposed** realization is selected for its superior numerical stability, lower round-off noise under floating-point arithmetic, and single-delay-line memory efficiency.

### Difference Equations:
$$\begin{aligned}
y[n] &= b_0 x[n] + s_1[n-1] \\
s_1[n] &= b_1 x[n] - a_1 y[n] + s_2[n-1] \\
s_2[n] &= b_2 x[n] - a_2 y[n]
\end{aligned}$$
where $s_1[n]$ and $s_2[n]$ represent the internal filter state variables.

---

## 4. Acoustic Anchor Frequency Bands & Filter Types

| Band | Center Freq ($f_0$) | Filter Type | Default Q | Octave Bandwidth |
| :--- | :--- | :--- | :--- | :--- |
| **Band 1** | 31.25 Hz | Low Shelf / Peaking | 1.414 ($Q$) | Sub-Bass |
| **Band 2** | 62.5 Hz | Peaking EQ | 1.414 | Bass Resonance |
| **Band 3** | 125 Hz | Peaking EQ | 1.414 | Bass Punch |
| **Band 4** | 250 Hz | Peaking EQ | 1.414 | Low-Mid Warmth |
| **Band 5** | 500 Hz | Peaking EQ | 1.414 | Mid Body |
| **Band 6** | 1.0 kHz | Peaking EQ | 1.414 | Vocal Clarity |
| **Band 7** | 2.0 kHz | Peaking EQ | 1.414 | Attack / Presence |
| **Band 8** | 4.0 kHz | Peaking EQ | 1.414 | Edge & Definition |
| **Band 9** | 8.0 kHz | Peaking EQ | 1.414 | Treble Air |
| **Band 10** | 16.0 kHz | High Shelf / Peaking| 1.414 | Ultra-High Brilliance |

---

## 5. Coefficient Derivation (RBJ Audio EQ Cookbook)

Given:
- Sample rate: $f_s$ (e.g. 44100, 48000, 96000, 192000, 384000 Hz)
- Band center frequency: $f_0$
- Gain in decibels: $G_{\text{dB}} \in [-12.0, +12.0]\text{ dB}$
- Quality factor: $Q = 1.414$

### Common Intermediate Variables:
$$A = 10^{\frac{G_{\text{dB}}}{40}} = \sqrt{10^{\frac{G_{\text{dB}}}{20}}}$$
$$\omega_0 = 2\pi \frac{f_0}{f_s}$$
$$\alpha = \frac{\sin(\omega_0)}{2Q}$$

### 5.1 Peaking EQ Filter (Bands 2 through 9):
$$\begin{aligned}
b_0 &= 1 + \alpha \cdot A \\
b_1 &= -2 \cos(\omega_0) \\
b_2 &= 1 - \alpha \cdot A \\
a_0 &= 1 + \frac{\alpha}{A} \\
a_1 &= -2 \cos(\omega_0) \\
a_2 &= 1 - \frac{\alpha}{A}
\end{aligned}$$

### 5.2 Low-Shelf Filter (Band 1 Alternative):
$$\begin{aligned}
\beta &= 2\sqrt{A}\alpha \\
b_0 &= A\left[(A+1) - (A-1)\cos(\omega_0) + \beta\right] \\
b_1 &= 2A\left[(A-1) - (A+1)\cos(\omega_0)\right] \\
b_2 &= A\left[(A+1) - (A-1)\cos(\omega_0) - \beta\right] \\
a_0 &= (A+1) + (A-1)\cos(\omega_0) + \beta \\
a_1 &= -2\left[(A-1) + (A+1)\cos(\omega_0)\right] \\
a_2 &= (A+1) + (A-1)\cos(\omega_0) - \beta
\end{aligned}$$

### 5.3 High-Shelf Filter (Band 10 Alternative):
$$\begin{aligned}
\beta &= 2\sqrt{A}\alpha \\
b_0 &= A\left[(A+1) + (A-1)\cos(\omega_0) + \beta\right] \\
b_1 &= -2A\left[(A-1) + (A+1)\cos(\omega_0)\right] \\
b_2 &= A\left[(A+1) + (A-1)\cos(\omega_0) - \beta\right] \\
a_0 &= (A+1) - (A-1)\cos(\omega_0) + \beta \\
a_1 &= 2\left[(A-1) - (A+1)\cos(\omega_0)\right] \\
a_2 &= (A+1) - (A-1)\cos(\omega_0) - \beta
\end{aligned}$$

---

## 6. Dynamic Headroom Regulator & Anti-Clipping

When boosting frequency bands, total signal energy may exceed $0\text{ dBFS}$ ($1.0$ linear amplitude), causing harsh digital clipping in the final DAC stage.

### Mathematical Headroom Constraint:
Let $G_k$ be the user gain in dB for band $k \in \{1, \dots, 10\}$.
$$G_{\text{peak}} = \max_{k \in \{1,\dots,10\}} (0, G_k)$$

The pre-cut digital attenuation gain $G_{\text{pre}}$ applied across the input buffer is:
$$G_{\text{pre}}(\text{dB}) = -G_{\text{peak}}$$
$$g_{\text{pre}} = 10^{\frac{G_{\text{pre}}}{20}}$$

Every audio sample $x[n]$ is first scaled by $g_{\text{pre}}$ prior to cascading through the 10 biquad stages:
$$x_{\text{scaled}}[n] = x[n] \cdot g_{\text{pre}}$$
$$\text{Output: } y[n] = H_{10}(z) * H_9(z) * \dots * H_1(z) * x_{\text{scaled}}[n]$$

This mathematical guarantee eliminates inter-sample clipping while preserving full dynamic range.
