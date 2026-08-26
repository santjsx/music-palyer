#include "click_synthesizer.h"
#include <cmath>
#include <algorithm>

constexpr double TWO_PI = 6.28318530717958647692;

ClickSynthesizer::ClickSynthesizer()
    : playbackPosition(0),
      isPlaying(false),
      clickEnabled(true),
      clickVolume(0.85f),
      currentSampleRate(44100.0) {
    generateClickImpulseTable(44100.0);
}

void ClickSynthesizer::initialize(double sampleRateHz) {
    std::lock_guard<std::mutex> lock(synthMutex);
    currentSampleRate = std::max(8000.0, sampleRateHz);
    generateClickImpulseTable(currentSampleRate);
    playbackPosition = 0;
    isPlaying = false;
}

void ClickSynthesizer::generateClickImpulseTable(double sampleRateHz) {
    // iPod Mechanical Click: ~10ms transient duration
    // Dual damped oscillator model:
    // f1 = 2800Hz (sharp transient contact), tau1 = 1.5ms
    // f2 = 950Hz (cavity resonance), tau2 = 6.0ms
    double durationSeconds = 0.012; // 12ms total
    size_t totalSamples = static_cast<size_t>(durationSeconds * sampleRateHz);
    clickTable.resize(totalSamples);

    double dt = 1.0 / sampleRateHz;
    for (size_t i = 0; i < totalSamples; ++i) {
        double t = i * dt;

        // Fast high-frequency transient click
        double transient = std::sin(TWO_PI * 2800.0 * t) * std::exp(-t / 0.0012);

        // Body thump resonance
        double resonance = std::sin(TWO_PI * 950.0 * t) * std::exp(-t / 0.0055);

        // Secondary subtle sub-click (spring bounce at 3ms)
        double bounce = 0.0;
        if (t > 0.003) {
            double tb = t - 0.003;
            bounce = 0.35 * std::sin(TWO_PI * 3400.0 * tb) * std::exp(-tb / 0.0010);
        }

        // Weighted mix with smooth attack envelope
        double rawSample = (0.65 * transient + 0.35 * resonance + bounce);
        double envelope = std::min(1.0, t / 0.0003); // 0.3ms fast attack

        clickTable[i] = static_cast<float>(rawSample * envelope * 0.90);
    }
}

void ClickSynthesizer::triggerClick(float volume) {
    if (!clickEnabled.load()) return;
    clickVolume.store(std::clamp(volume, 0.0f, 1.0f));
    playbackPosition.store(0);
    isPlaying.store(true);
}

void ClickSynthesizer::setEnabled(bool enabled) {
    clickEnabled.store(enabled);
}

void ClickSynthesizer::renderAndMix(float* buffer, int32_t numFrames) {
    if (!isPlaying.load() || !buffer || numFrames <= 0) return;

    int32_t pos = playbackPosition.load();
    int32_t tableSize = static_cast<int32_t>(clickTable.size());
    float vol = clickVolume.load();

    for (int32_t i = 0; i < numFrames; ++i) {
        if (pos >= tableSize) {
            isPlaying.store(false);
            break;
        }

        float sample = clickTable[pos++] * vol;
        int32_t idx = i * 2;
        // Mix into left and right channels
        buffer[idx] += sample;
        buffer[idx + 1] += sample;
    }

    playbackPosition.store(pos);
}
