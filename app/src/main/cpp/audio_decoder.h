#ifndef AUDIO_DECODER_H
#define AUDIO_DECODER_H

#include <string>
#include <vector>
#include <cstdint>
#include <mutex>
#include <atomic>

class AudioDecoder {
public:
    AudioDecoder();
    ~AudioDecoder() = default;

    bool loadFile(const std::string& filePath);
    void close();

    int32_t readSamples(float* targetBuffer, int32_t numFrames);
    void seekToFrame(int64_t targetFrame);

    int32_t getSampleRate() const { return sampleRate; }
    int32_t getChannelCount() const { return channelCount; }
    int64_t getTotalFrames() const { return totalFrames; }
    int64_t getCurrentFrame() const { return currentFramePosition.load(); }
    bool isLoaded() const { return fileLoaded.load(); }

private:
    bool loadWavPcm(const std::string& path);
    void generateSyntheticTone(); // High quality demo tone fallback

    std::vector<float> pcmBuffer; // Interleaved stereo float buffer
    int32_t sampleRate;
    int32_t channelCount;
    int64_t totalFrames;
    std::atomic<int64_t> currentFramePosition;
    std::atomic<bool> fileLoaded;
    std::mutex bufferMutex;
};

#endif // AUDIO_DECODER_H
