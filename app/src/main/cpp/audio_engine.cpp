#include "audio_engine.h"
#include <android/log.h>
#include <cstring>
#include <algorithm>

#define TAG "iPodAudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static aaudio_data_callback_result_t dataCallback(
    AAudioStream* stream,
    void* userData,
    void* audioData,
    int32_t numFrames) {
    auto* engine = static_cast<AudioEngine*>(userData);
    return engine->onAudioReady(stream, audioData, numFrames);
}

AudioEngine& AudioEngine::getInstance() {
    static AudioEngine instance;
    return instance;
}

AudioEngine::AudioEngine()
    : aaudioStream(nullptr),
      engineState(EngineState::STOPPED),
      masterVolume(1.0f),
      isExclusiveMode(true),
      currentStreamSampleRate(48000) {
    equalizer.initialize(48000);
    clickSynthesizer.initialize(48000);
}

AudioEngine::~AudioEngine() {
    stopStream();
}

bool AudioEngine::startStream(int32_t sampleRate, bool exclusiveMode) {
    stopStream();

    AAudioStreamBuilder* builder = nullptr;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) {
        LOGE("Failed to create AAudio stream builder: %s", AAudio_convertResultToText(result));
        return false;
    }

    currentStreamSampleRate = sampleRate;
    isExclusiveMode.store(exclusiveMode);

    AAudioStreamBuilder_setSampleRate(builder, sampleRate);
    AAudioStreamBuilder_setChannelCount(builder, 2); // Stereo
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(builder, exclusiveMode ? AAUDIO_SHARING_MODE_EXCLUSIVE : AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setDataCallback(builder, dataCallback, this);

    result = AAudioStreamBuilder_openStream(builder, &aaudioStream);
    AAudioStreamBuilder_delete(builder);

    if (result != AAUDIO_OK) {
        LOGE("Failed to open AAudio stream: %s. Retrying in shared mode...", AAudio_convertResultToText(result));
        // Fallback to shared mode if exclusive is not supported by device hardware
        if (exclusiveMode) {
            return startStream(sampleRate, false);
        }
        return false;
    }

    int32_t actualSampleRate = AAudioStream_getSampleRate(aaudioStream);
    currentStreamSampleRate = actualSampleRate;
    equalizer.initialize(actualSampleRate);
    clickSynthesizer.initialize(actualSampleRate);

    result = AAudioStream_requestStart(aaudioStream);
    if (result != AAUDIO_OK) {
        LOGE("Failed to start AAudio stream: %s", AAudio_convertResultToText(result));
        stopStream();
        return false;
    }

    LOGI("AAudio stream successfully started! Rate: %d Hz, Exclusive: %d", actualSampleRate, exclusiveMode ? 1 : 0);
    return true;
}

void AudioEngine::stopStream() {
    if (aaudioStream != nullptr) {
        AAudioStream_requestStop(aaudioStream);
        AAudioStream_close(aaudioStream);
        aaudioStream = nullptr;
    }
}

bool AudioEngine::loadTrack(const std::string& filePath) {
    activeMetadata = NativeTagInspector::inspectFile(filePath);
    bool loaded = decoder.loadFile(filePath);

    if (loaded && decoder.getSampleRate() > 0 && decoder.getSampleRate() != currentStreamSampleRate) {
        // Re-align stream sample rate with loaded track sample rate for bit-perfect audio
        startStream(decoder.getSampleRate(), isExclusiveMode.load());
    }

    return loaded;
}

void AudioEngine::play() {
    if (aaudioStream == nullptr) {
        startStream(decoder.isLoaded() ? decoder.getSampleRate() : 48000, isExclusiveMode.load());
    }
    engineState.store(EngineState::PLAYING);
}

void AudioEngine::pause() {
    engineState.store(EngineState::PAUSED);
}

void AudioEngine::stop() {
    engineState.store(EngineState::STOPPED);
    decoder.seekToFrame(0);
}

void AudioEngine::seekToMs(int64_t positionMs) {
    if (decoder.getSampleRate() > 0) {
        int64_t targetFrame = (positionMs * decoder.getSampleRate()) / 1000;
        decoder.seekToFrame(targetFrame);
        equalizer.reset();
    }
}

void AudioEngine::setVolume(float volumeLinear) {
    masterVolume.store(std::clamp(volumeLinear, 0.0f, 1.0f));
}

void AudioEngine::triggerClick(float volume) {
    clickSynthesizer.triggerClick(volume);
}

void AudioEngine::setClickSoundEnabled(bool enabled) {
    clickSynthesizer.setEnabled(enabled);
}

void AudioEngine::setEqBandGain(size_t bandIndex, float gainDb) {
    equalizer.setBandGain(bandIndex, gainDb);
}

void AudioEngine::setEqAllBands(const float* gainsDb) {
    equalizer.setAllBandGains(gainsDb);
}

void AudioEngine::setEqEnabled(bool enabled) {
    equalizer.setEnabled(enabled);
}

float AudioEngine::getDynamicPrecutGainDb() const {
    return equalizer.getDynamicPrecutGainDb();
}

int64_t AudioEngine::getCurrentPositionMs() const {
    if (decoder.getSampleRate() > 0) {
        return (decoder.getCurrentFrame() * 1000) / decoder.getSampleRate();
    }
    return 0;
}

int64_t AudioEngine::getDurationMs() const {
    if (decoder.getSampleRate() > 0) {
        return (decoder.getTotalFrames() * 1000) / decoder.getSampleRate();
    }
    return 0;
}

bool AudioEngine::isPlaying() const {
    return engineState.load() == EngineState::PLAYING;
}

AudioMetadataInfo AudioEngine::getCurrentMetadata() const {
    return activeMetadata;
}

aaudio_data_callback_result_t AudioEngine::onAudioReady(
    AAudioStream* /*stream*/,
    void* audioData,
    int32_t numFrames) {
    auto* outBuffer = static_cast<float*>(audioData);
    int32_t totalSamples = numFrames * 2;

    // 1. Zero out buffer initially
    std::memset(outBuffer, 0, totalSamples * sizeof(float));

    // 2. If playing, read audio frames from decoder
    if (engineState.load() == EngineState::PLAYING) {
        int32_t readFrames = decoder.readSamples(outBuffer, numFrames);
        if (readFrames < numFrames) {
            // Buffer remaining frames with silence
            std::memset(&outBuffer[readFrames * 2], 0, (numFrames - readFrames) * 2 * sizeof(float));
        }

        // Apply 10-band Biquad IIR Equalizer with dynamic headroom pre-cut
        equalizer.process(outBuffer, numFrames);

        // Apply Master Volume
        float vol = masterVolume.load();
        if (vol < 0.999f) {
            for (int32_t i = 0; i < totalSamples; ++i) {
                outBuffer[i] *= vol;
            }
        }
    }

    // 3. Always mix synthetic mechanical click impulse with zero latency
    clickSynthesizer.renderAndMix(outBuffer, numFrames);

    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}
