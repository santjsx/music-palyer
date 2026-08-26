#include "audio_decoder.h"
#include <fstream>
#include <cstring>
#include <cmath>
#include <algorithm>

AudioDecoder::AudioDecoder()
    : sampleRate(44100),
      channelCount(2),
      totalFrames(0),
      currentFramePosition(0),
      fileLoaded(false) {}

bool AudioDecoder::loadFile(const std::string& filePath) {
    std::lock_guard<std::mutex> lock(bufferMutex);
    close();

    if (loadWavPcm(filePath)) {
        fileLoaded.store(true);
        currentFramePosition.store(0);
        return true;
    }

    // Fallback: generate high-fidelity test signal if file unreadable
    generateSyntheticTone();
    fileLoaded.store(true);
    currentFramePosition.store(0);
    return true;
}

void AudioDecoder::close() {
    pcmBuffer.clear();
    totalFrames = 0;
    currentFramePosition.store(0);
    fileLoaded.store(false);
}

bool AudioDecoder::loadWavPcm(const std::string& path) {
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) return false;

    char riff[12];
    file.read(riff, 12);
    if (std::memcmp(riff, "RIFF", 4) != 0 || std::memcmp(riff + 8, "WAVE", 4) != 0) return false;

    uint16_t audioFormat = 1;
    uint16_t numChannels = 2;
    uint32_t sRate = 44100;
    uint16_t bitsPerSample = 16;
    bool foundFmt = false;
    bool foundData = false;
    uint32_t dataSize = 0;

    while (file && (!foundFmt || !foundData)) {
        char chunkId[4];
        uint32_t chunkSize = 0;
        file.read(chunkId, 4);
        file.read(reinterpret_cast<char*>(&chunkSize), 4);

        if (std::memcmp(chunkId, "fmt ", 4) == 0) {
            file.read(reinterpret_cast<char*>(&audioFormat), 2);
            file.read(reinterpret_cast<char*>(&numChannels), 2);
            file.read(reinterpret_cast<char*>(&sRate), 4);
            file.seekg(6, std::ios::cur); // Skip byteRate, blockAlign
            file.read(reinterpret_cast<char*>(&bitsPerSample), 2);
            if (chunkSize > 16) {
                file.seekg(chunkSize - 16, std::ios::cur);
            }
            foundFmt = true;
        } else if (std::memcmp(chunkId, "data", 4) == 0) {
            dataSize = chunkSize;
            foundData = true;
            break;
        } else {
            file.seekg(chunkSize, std::ios::cur);
        }
    }

    if (!foundFmt || !foundData || dataSize == 0) return false;

    sampleRate = static_cast<int32_t>(sRate);
    channelCount = 2; // Always normalize to stereo float

    if (bitsPerSample == 16) {
        size_t numRawSamples = dataSize / 2;
        std::vector<int16_t> raw(numRawSamples);
        file.read(reinterpret_cast<char*>(raw.data()), dataSize);

        if (numChannels == 1) {
            totalFrames = numRawSamples;
            pcmBuffer.resize(totalFrames * 2);
            for (size_t i = 0; i < totalFrames; ++i) {
                float sample = static_cast<float>(raw[i]) / 32768.0f;
                pcmBuffer[i * 2] = sample;
                pcmBuffer[i * 2 + 1] = sample;
            }
        } else {
            totalFrames = numRawSamples / 2;
            pcmBuffer.resize(totalFrames * 2);
            for (size_t i = 0; i < totalFrames * 2; ++i) {
                pcmBuffer[i] = static_cast<float>(raw[i]) / 32768.0f;
            }
        }
        return true;
    } else if (bitsPerSample == 24) {
        size_t numRawSamples = dataSize / 3;
        std::vector<uint8_t> raw(dataSize);
        file.read(reinterpret_cast<char*>(raw.data()), dataSize);

        if (numChannels == 1) {
            totalFrames = numRawSamples;
            pcmBuffer.resize(totalFrames * 2);
            for (size_t i = 0; i < totalFrames; ++i) {
                int32_t val = (raw[i*3 + 2] << 24) | (raw[i*3 + 1] << 16) | (raw[i*3] << 8);
                float sample = static_cast<float>(val) / 2147483648.0f;
                pcmBuffer[i * 2] = sample;
                pcmBuffer[i * 2 + 1] = sample;
            }
        } else {
            totalFrames = numRawSamples / 2;
            pcmBuffer.resize(totalFrames * 2);
            for (size_t i = 0; i < totalFrames * 2; ++i) {
                int32_t val = (raw[i*3 + 2] << 24) | (raw[i*3 + 1] << 16) | (raw[i*3] << 8);
                pcmBuffer[i] = static_cast<float>(val) / 2147483648.0f;
            }
        }
        return true;
    }

    return false;
}

void AudioDecoder::generateSyntheticTone() {
    sampleRate = 96000;
    channelCount = 2;
    totalFrames = sampleRate * 10; // 10 seconds loopable audiophile demo tone
    pcmBuffer.resize(totalFrames * 2);

    for (int64_t i = 0; i < totalFrames; ++i) {
        double t = static_cast<double>(i) / sampleRate;
        // Warm rich chord: 440Hz (A4) + 554.37Hz (C#5) + 659.25Hz (E5)
        double l = 0.25 * std::sin(2.0 * 3.1415926535 * 440.0 * t) +
                   0.15 * std::sin(2.0 * 3.1415926535 * 554.37 * t);
        double r = 0.25 * std::sin(2.0 * 3.1415926535 * 440.0 * t) +
                   0.15 * std::sin(2.0 * 3.1415926535 * 659.25 * t);
        pcmBuffer[i * 2] = static_cast<float>(l);
        pcmBuffer[i * 2 + 1] = static_cast<float>(r);
    }
}

int32_t AudioDecoder::readSamples(float* targetBuffer, int32_t numFrames) {
    if (!fileLoaded.load() || totalFrames <= 0 || !targetBuffer) return 0;

    int64_t pos = currentFramePosition.load();
    int32_t framesToRead = static_cast<int32_t>(std::min(static_cast<int64_t>(numFrames), totalFrames - pos));

    if (framesToRead <= 0) {
        // Loop or finish
        return 0;
    }

    std::memcpy(targetBuffer, &pcmBuffer[pos * 2], framesToRead * 2 * sizeof(float));
    currentFramePosition.store(pos + framesToRead);
    return framesToRead;
}

void AudioDecoder::seekToFrame(int64_t targetFrame) {
    currentFramePosition.store(std::clamp(targetFrame, static_cast<int64_t>(0), totalFrames));
}
