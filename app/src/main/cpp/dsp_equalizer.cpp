#include "dsp_equalizer.h"
#include <algorithm>
#include <cmath>

static const std::array<double, NUM_EQ_BANDS> ANCHOR_FREQUENCIES = {
    31.25,   // Band 1: Sub-bass
    62.5,    // Band 2: Bass resonance
    125.0,   // Band 3: Bass punch
    250.0,   // Band 4: Low-mid warmth
    500.0,   // Band 5: Mid body
    1000.0,  // Band 6: Vocal presence
    2000.0,  // Band 7: Attack
    4000.0,  // Band 8: Treble definition
    8000.0,  // Band 9: Treble brilliance
    16000.0  // Band 10: Air
};

const std::array<double, NUM_EQ_BANDS>& DspEqualizer::getAnchorFrequencies() {
    return ANCHOR_FREQUENCIES;
}

DspEqualizer::DspEqualizer()
    : currentSampleRate(44100.0),
      eqEnabled(true),
      dynamicPrecutGainDb(0.0f),
      linearPrecutScale(1.0f) {
    bandGainsDb.fill(0.0f);
}

void DspEqualizer::initialize(double sampleRateHz) {
    std::lock_guard<std::mutex> lock(eqMutex);
    currentSampleRate = std::max(8000.0, sampleRateHz);
    updateHeadroomAndFilters();
    reset();
}

void DspEqualizer::setSampleRate(double sampleRateHz) {
    std::lock_guard<std::mutex> lock(eqMutex);
    if (std::abs(currentSampleRate - sampleRateHz) > 1.0) {
        currentSampleRate = std::max(8000.0, sampleRateHz);
        updateHeadroomAndFilters();
    }
}

void DspEqualizer::setBandGain(size_t bandIndex, float gainDb) {
    if (bandIndex >= NUM_EQ_BANDS) return;
    std::lock_guard<std::mutex> lock(eqMutex);
    // Clamp gain between -12dB and +12dB
    bandGainsDb[bandIndex] = std::clamp(gainDb, -12.0f, 12.0f);
    updateHeadroomAndFilters();
}

void DspEqualizer::setAllBandGains(const float* gainsDb) {
    if (!gainsDb) return;
    std::lock_guard<std::mutex> lock(eqMutex);
    for (size_t i = 0; i < NUM_EQ_BANDS; ++i) {
        bandGainsDb[i] = std::clamp(gainsDb[i], -12.0f, 12.0f);
    }
    updateHeadroomAndFilters();
}

void DspEqualizer::setEnabled(bool enabled) {
    std::lock_guard<std::mutex> lock(eqMutex);
    eqEnabled = enabled;
}

void DspEqualizer::updateHeadroomAndFilters() {
    // 1. Calculate maximum positive boost across all bands
    float maxBoost = 0.0f;
    for (size_t i = 0; i < NUM_EQ_BANDS; ++i) {
        if (bandGainsDb[i] > maxBoost) {
            maxBoost = bandGainsDb[i];
        }
    }

    // Dynamic Headroom Regulator: apply negative pre-amp gain equal to peak boost
    dynamicPrecutGainDb = -maxBoost;
    linearPrecutScale = std::pow(10.0f, dynamicPrecutGainDb / 20.0f);

    // 2. Configure 10 Biquad filters
    for (size_t i = 0; i < NUM_EQ_BANDS; ++i) {
        FilterType type = FilterType::PEAKING_EQ;
        if (i == 0) {
            type = FilterType::LOW_SHELF;
        } else if (i == NUM_EQ_BANDS - 1) {
            type = FilterType::HIGH_SHELF;
        }
        filters[i].configure(type, ANCHOR_FREQUENCIES[i], bandGainsDb[i], 1.414, currentSampleRate);
    }
}

void DspEqualizer::reset() {
    for (auto& filter : filters) {
        filter.resetState();
    }
}

void DspEqualizer::process(float* buffer, int32_t numFrames) {
    if (!eqEnabled || !buffer || numFrames <= 0) return;

    std::lock_guard<std::mutex> lock(eqMutex);

    // 1. Apply pre-cut linear attenuation for headroom
    if (linearPrecutScale < 0.9999f) {
        int32_t totalSamples = numFrames * 2;
        for (int32_t i = 0; i < totalSamples; ++i) {
            buffer[i] *= linearPrecutScale;
        }
    }

    // 2. Cascade through each of the 10 biquad filter sections
    for (size_t i = 0; i < NUM_EQ_BANDS; ++i) {
        if (std::abs(bandGainsDb[i]) > 0.01f) {
            filters[i].processStereo(buffer, numFrames);
        }
    }
}
