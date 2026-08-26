#include "biquad_filter.h"
#include <algorithm>

constexpr double M_PI_VAL = 3.14159265358979323846;

BiquadFilter::BiquadFilter()
    : filterType(FilterType::PEAKING_EQ),
      currentCenterFreq(1000.0),
      currentGainDb(0.0),
      currentQ(1.414),
      currentSampleRate(44100.0),
      b0(1.0), b1(0.0), b2(0.0),
      a1(0.0), a2(0.0),
      s1(0.0), s2(0.0),
      s1_r(0.0), s2_r(0.0) {}

void BiquadFilter::configure(FilterType type, double centerFreqHz, double gainDb, double qFactor, double sampleRateHz) {
    filterType = type;
    currentCenterFreq = centerFreqHz;
    currentGainDb = gainDb;
    currentQ = std::max(0.1, qFactor);
    currentSampleRate = std::max(8000.0, sampleRateHz);

    calculateCoefficients();
}

void BiquadFilter::resetState() {
    s1 = 0.0;
    s2 = 0.0;
    s1_r = 0.0;
    s2_r = 0.0;
}

void BiquadFilter::calculateCoefficients() {
    // If gain is 0dB for peaking/shelving, filter is transparent pass-through
    if (std::abs(currentGainDb) < 0.0001 &&
        (filterType == FilterType::PEAKING_EQ ||
         filterType == FilterType::LOW_SHELF ||
         filterType == FilterType::HIGH_SHELF)) {
        b0 = 1.0;
        b1 = 0.0;
        b2 = 0.0;
        a1 = 0.0;
        a2 = 0.0;
        return;
    }

    // Nyquist clamp
    double nyquist = currentSampleRate * 0.499;
    double f0 = std::clamp(currentCenterFreq, 10.0, nyquist);

    double A = std::pow(10.0, currentGainDb / 40.0);
    double w0 = 2.0 * M_PI_VAL * (f0 / currentSampleRate);
    double cosw0 = std::cos(w0);
    double sinw0 = std::sin(w0);
    double alpha = sinw0 / (2.0 * currentQ);

    double raw_b0 = 1.0, raw_b1 = 0.0, raw_b2 = 0.0;
    double raw_a0 = 1.0, raw_a1 = 0.0, raw_a2 = 0.0;

    switch (filterType) {
        case FilterType::PEAKING_EQ: {
            raw_b0 = 1.0 + alpha * A;
            raw_b1 = -2.0 * cosw0;
            raw_b2 = 1.0 - alpha * A;
            raw_a0 = 1.0 + alpha / A;
            raw_a1 = -2.0 * cosw0;
            raw_a2 = 1.0 - alpha / A;
            break;
        }
        case FilterType::LOW_SHELF: {
            double beta = 2.0 * std::sqrt(A) * alpha;
            raw_b0 = A * ((A + 1.0) - (A - 1.0) * cosw0 + beta);
            raw_b1 = 2.0 * A * ((A - 1.0) - (A + 1.0) * cosw0);
            raw_b2 = A * ((A + 1.0) - (A - 1.0) * cosw0 - beta);
            raw_a0 = (A + 1.0) + (A - 1.0) * cosw0 + beta;
            raw_a1 = -2.0 * ((A - 1.0) + (A + 1.0) * cosw0);
            raw_a2 = (A + 1.0) + (A - 1.0) * cosw0 - beta;
            break;
        }
        case FilterType::HIGH_SHELF: {
            double beta = 2.0 * std::sqrt(A) * alpha;
            raw_b0 = A * ((A + 1.0) + (A - 1.0) * cosw0 + beta);
            raw_b1 = -2.0 * A * ((A - 1.0) + (A + 1.0) * cosw0);
            raw_b2 = A * ((A + 1.0) + (A - 1.0) * cosw0 - beta);
            raw_a0 = (A + 1.0) - (A - 1.0) * cosw0 + beta;
            raw_a1 = 2.0 * ((A - 1.0) - (A + 1.0) * cosw0);
            raw_a2 = (A + 1.0) - (A - 1.0) * cosw0 - beta;
            break;
        }
        case FilterType::LOW_PASS: {
            raw_b0 = (1.0 - cosw0) / 2.0;
            raw_b1 = 1.0 - cosw0;
            raw_b2 = (1.0 - cosw0) / 2.0;
            raw_a0 = 1.0 + alpha;
            raw_a1 = -2.0 * cosw0;
            raw_a2 = 1.0 - alpha;
            break;
        }
        case FilterType::HIGH_PASS: {
            raw_b0 = (1.0 + cosw0) / 2.0;
            raw_b1 = -(1.0 + cosw0);
            raw_b2 = (1.0 + cosw0) / 2.0;
            raw_a0 = 1.0 + alpha;
            raw_a1 = -2.0 * cosw0;
            raw_a2 = 1.0 - alpha;
            break;
        }
    }

    // Normalize coefficients by a0
    b0 = raw_b0 / raw_a0;
    b1 = raw_b1 / raw_a0;
    b2 = raw_b2 / raw_a0;
    a1 = raw_a1 / raw_a0;
    a2 = raw_a2 / raw_a0;
}

void BiquadFilter::processStereo(float* buffer, int32_t numFrames) {
    for (int32_t i = 0; i < numFrames; ++i) {
        int32_t idx = i * 2;
        // Left Channel
        double in_l = static_cast<double>(buffer[idx]);
        double out_l = b0 * in_l + s1;
        s1 = b1 * in_l - a1 * out_l + s2;
        s2 = b2 * in_l - a2 * out_l;
        buffer[idx] = static_cast<float>(out_l);

        // Right Channel
        double in_r = static_cast<double>(buffer[idx + 1]);
        double out_r = b0 * in_r + s1_r;
        s1_r = b1 * in_r - a1 * out_r + s2_r;
        s2_r = b2 * in_r - a2 * out_r;
        buffer[idx + 1] = static_cast<float>(out_r);
    }
}
