#ifndef NATIVE_TAG_INSPECTOR_H
#define NATIVE_TAG_INSPECTOR_H

#include <string>
#include <cstdint>

enum class AudioQualityCategory {
    LOSSY = 0,
    LOSSLESS = 1,
    HI_RES_LOSSLESS = 2
};

struct AudioMetadataInfo {
    std::string formatName;
    int32_t sampleRate;
    int32_t bitDepth;
    int32_t channels;
    int32_t bitRateKbps;
    int64_t durationMs;
    AudioQualityCategory qualityCategory;
    std::string badgeText;
};

class NativeTagInspector {
public:
    static AudioMetadataInfo inspectFile(const std::string& filePath);

private:
    static bool inspectFlac(const std::string& path, AudioMetadataInfo& info);
    static bool inspectWav(const std::string& path, AudioMetadataInfo& info);
    static bool inspectMp3(const std::string& path, AudioMetadataInfo& info);
    static bool inspectDsd(const std::string& path, AudioMetadataInfo& info);
    static void categorizeQuality(AudioMetadataInfo& info);
};

#endif // NATIVE_TAG_INSPECTOR_H
