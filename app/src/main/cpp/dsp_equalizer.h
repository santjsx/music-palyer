#ifndef DSP_EQUALIZER_H
#define DSP_EQUALIZER_H

#include "biquad_filter.h"
#include <array>
#include <mutex>

constexpr size_t NUM_EQ_BANDS = 10;

class DspEqualizer {
public:
    DspEqualizer();
    ~DspEqualizer() = default;

    void initialize(double sampleRateHz);
    void setSampleRate(double sampleRateHz);

    // Set gain for a specific band index (0..9) in decibels (-12.0dB to +12.0dB)
    void setBandGain(size_t bandIndex, float gainDb);

    // Set all 10 band gains simultaneously
    void setAllBandGains(const float* gainsDb);

    // Enable / Disable EQ processing bypass
    void setEnabled(bool enabled);
    bool isEnabled() const { return eqEnabled; }

    // Pre-amp gain for dynamic headroom
    float getDynamicPrecutGainDb() const { return dynamicPrecutGainDb; }

    // In-place stereo buffer processing (interleaved L/R float samples)
    void process(float* buffer, int32_t numFrames);

    // Reset filter states on seek / track change
    void reset();

    static const std::array<double, NUM_EQ_BANDS>& getAnchorFrequencies();

private:
    void updateHeadroomAndFilters();

    std::array<BiquadFilter, NUM_EQ_BANDS> filters;
    std::array<float, NUM_EQ_BANDS> bandGainsDb;
    double currentSampleRate;
    bool eqEnabled;
    float dynamicPrecutGainDb;
    float linearPrecutScale;

    std::mutex eqMutex;
};

#endif // DSP_EQUALIZER_H
