#ifndef BIQUAD_FILTER_H
#define BIQUAD_FILTER_H

#include <cmath>
#include <cstdint>

enum class FilterType {
    PEAKING_EQ = 0,
    LOW_SHELF = 1,
    HIGH_SHELF = 2,
    LOW_PASS = 3,
    HIGH_PASS = 4
};

/**
 * Direct Form II Transposed Second-Order IIR Biquad Filter.
 *
 * Difference Equations:
 *   y[n]  = b0 * x[n] + s1[n-1]
 *   s1[n] = b1 * x[n] - a1 * y[n] + s2[n-1]
 *   s2[n] = b2 * x[n] - a2 * y[n]
 */
class BiquadFilter {
public:
    BiquadFilter();
    ~BiquadFilter() = default;

    void configure(FilterType type, double centerFreqHz, double gainDb, double qFactor, double sampleRateHz);
    void resetState();

    // Process a single sample (mono)
    inline float processSample(float inputSample) {
        double in = static_cast<double>(inputSample);
        double out = b0 * in + s1;
        s1 = b1 * in - a1 * out + s2;
        s2 = b2 * in - a2 * out;
        return static_cast<float>(out);
    }

    // Process stereo interleaved buffer in-place
    void processStereo(float* buffer, int32_t numFrames);

    double getGainDb() const { return currentGainDb; }
    double getCenterFrequency() const { return currentCenterFreq; }

private:
    void calculateCoefficients();

    FilterType filterType;
    double currentCenterFreq;
    double currentGainDb;
    double currentQ;
    double currentSampleRate;

    // Normalized coefficients (where a0 = 1.0)
    double b0;
    double b1;
    double b2;
    double a1;
    double a2;

    // State variables for Left Channel (or Mono)
    double s1;
    double s2;

    // State variables for Right Channel (Stereo)
    double s1_r;
    double s2_r;
};

#endif // BIQUAD_FILTER_H
