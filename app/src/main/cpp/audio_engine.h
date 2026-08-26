#ifndef AUDIO_ENGINE_H
#define AUDIO_ENGINE_H

#include <aaudio/AAudio.h>
#include "dsp_equalizer.h"
#include "click_synthesizer.h"
#include "audio_decoder.h"
#include "native_tag_inspector.h"
#include <atomic>
#include <memory>
#include <string>

enum class EngineState {
    STOPPED = 0,
    PLAYING = 1,
    PAUSED = 2
};

class AudioEngine {
public:
    static AudioEngine& getInstance();

    bool startStream(int32_t sampleRate = 48000, bool exclusiveMode = true);
    void stopStream();

    bool loadTrack(const std::string& filePath);
    void play();
    void pause();
    void stop();
    void seekToMs(int64_t positionMs);

    void setVolume(float volumeLinear);
    void triggerClick(float volume = 0.85f);
    void setClickSoundEnabled(bool enabled);

    // Equalizer controls
    void setEqBandGain(size_t bandIndex, float gainDb);
    void setEqAllBands(const float* gainsDb);
    void setEqEnabled(bool enabled);
    float getDynamicPrecutGainDb() const;

    // Playback state query
    int64_t getCurrentPositionMs() const;
    int64_t getDurationMs() const;
    bool isPlaying() const;
    EngineState getState() const { return engineState.load(); }
    AudioMetadataInfo getCurrentMetadata() const;

    // AAudio data callback handler
    aaudio_data_callback_result_t onAudioReady(AAudioStream* stream, void* audioData, int32_t numFrames);

private:
    AudioEngine();
    ~AudioEngine();

    AAudioStream* aaudioStream;
    AudioDecoder decoder;
    DspEqualizer equalizer;
    ClickSynthesizer clickSynthesizer;

    std::atomic<EngineState> engineState;
    std::atomic<float> masterVolume;
    std::atomic<bool> isExclusiveMode;
    int32_t currentStreamSampleRate;
    AudioMetadataInfo activeMetadata;
};

#endif // AUDIO_ENGINE_H
