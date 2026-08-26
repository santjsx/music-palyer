#ifndef CLICK_SYNTHESIZER_H
#define CLICK_SYNTHESIZER_H

#include <cstdint>
#include <vector>
#include <atomic>
#include <mutex>

class ClickSynthesizer {
public:
    ClickSynthesizer();
    ~ClickSynthesizer() = default;

    void initialize(double sampleRateHz);
    void triggerClick(float volume = 0.8f);
    void setEnabled(bool enabled);
    bool isEnabled() const { return clickEnabled.load(); }

    // Render active click samples into interleaved stereo buffer
    void renderAndMix(float* buffer, int32_t numFrames);

private:
    void generateClickImpulseTable(double sampleRateHz);

    std::vector<float> clickTable; // Pre-computed high-resolution impulse waveform
    std::atomic<int32_t> playbackPosition;
    std::atomic<bool> isPlaying;
    std::atomic<bool> clickEnabled;
    std::atomic<float> clickVolume;
    double currentSampleRate;
    std::mutex synthMutex;
};

#endif // CLICK_SYNTHESIZER_H
