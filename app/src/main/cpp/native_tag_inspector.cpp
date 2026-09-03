#include "native_tag_inspector.h"
#include <fstream>
#include <vector>
#include <cstring>
#include <sstream>
#include <algorithm>

AudioMetadataInfo NativeTagInspector::inspectFile(const std::string& filePath) {
    AudioMetadataInfo info;
    info.formatName = "UNKNOWN";
    info.sampleRate = 44100;
    info.bitDepth = 16;
    info.channels = 2;
    info.bitRateKbps = 0;
    info.durationMs = 0;
    info.qualityCategory = AudioQualityCategory::LOSSLESS;
    info.badgeText = "LOSSLESS";

    std::string ext = "";
    auto dotPos = filePath.find_last_of('.');
    if (dotPos != std::string::npos) {
        ext = filePath.substr(dotPos + 1);
        std::transform(ext.begin(), ext.end(), ext.begin(), ::tolower);
    }

    if (ext == "flac" || ext.empty()) {
        if (inspectFlac(filePath, info)) {
            categorizeQuality(info);
            return info;
        }
    }
    if (ext == "wav" || ext.empty()) {
        if (inspectWav(filePath, info)) {
            categorizeQuality(info);
            return info;
        }
    }
    if (ext == "dsf" || ext == "dff" || ext == "dsd") {
        if (inspectDsd(filePath, info)) {
            categorizeQuality(info);
            return info;
        }
    }
    if (ext == "mp3") {
        inspectMp3(filePath, info);
        categorizeQuality(info);
        return info;
    }

    categorizeQuality(info);
    return info;
}

bool NativeTagInspector::inspectFlac(const std::string& path, AudioMetadataInfo& info) {
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) return false;

    char header[4];
    file.read(header, 4);

    // If file has an ID3v2 tag prefix, skip it to reach fLaC stream
    if (std::memcmp(header, "ID3", 3) == 0) {
        char id3Rest[6];
        file.read(id3Rest, 6);
        uint32_t tagSize = ((id3Rest[2] & 0x7F) << 21) |
                           ((id3Rest[3] & 0x7F) << 14) |
                           ((id3Rest[4] & 0x7F) << 7) |
                           (id3Rest[5] & 0x7F);
        file.seekg(tagSize, std::ios::cur);
        file.read(header, 4);
    }

    if (std::memcmp(header, "fLaC", 4) != 0) return false;

    // STREAMINFO block header (4 bytes) + block data (34 bytes)
    uint8_t streamInfo[38];
    file.read(reinterpret_cast<char*>(streamInfo), 38);

    uint8_t blockType = streamInfo[0] & 0x7F;
    if (blockType != 0) return false; // Not STREAMINFO

    // Bytes 14..17 contain sample rate (20 bits), channels (3 bits), bits per sample (5 bits), total samples (36 bits)
    uint32_t sr_ch_bps = (static_cast<uint32_t>(streamInfo[14]) << 24) |
                         (static_cast<uint32_t>(streamInfo[15]) << 16) |
                         (static_cast<uint32_t>(streamInfo[16]) << 8) |
                         static_cast<uint32_t>(streamInfo[17]);

    info.formatName = "FLAC";
    info.sampleRate = sr_ch_bps >> 12;
    info.channels = ((sr_ch_bps >> 9) & 0x07) + 1;
    info.bitDepth = ((sr_ch_bps >> 4) & 0x1F) + 1;

    uint64_t totalSamples = (static_cast<uint64_t>(streamInfo[17] & 0x0F) << 32) |
                            (static_cast<uint64_t>(streamInfo[18]) << 24) |
                            (static_cast<uint64_t>(streamInfo[19]) << 16) |
                            (static_cast<uint64_t>(streamInfo[20]) << 8) |
                            static_cast<uint64_t>(streamInfo[21]);

    if (info.sampleRate > 0) {
        info.durationMs = static_cast<int64_t>((totalSamples * 1000ULL) / info.sampleRate);
    }

    return true;
}

bool NativeTagInspector::inspectWav(const std::string& path, AudioMetadataInfo& info) {
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) return false;

    char riff[12];
    file.read(riff, 12);
    if (std::memcmp(riff, "RIFF", 4) != 0 || std::memcmp(riff + 8, "WAVE", 4) != 0) return false;

    while (file) {
        char chunkId[4];
        uint32_t chunkSize = 0;
        file.read(chunkId, 4);
        file.read(reinterpret_cast<char*>(&chunkSize), 4);

        if (std::memcmp(chunkId, "fmt ", 4) == 0) {
            uint16_t audioFormat = 0;
            uint16_t numChannels = 0;
            uint32_t sampleRate = 0;
            uint32_t byteRate = 0;
            uint16_t blockAlign = 0;
            uint16_t bitsPerSample = 0;

            file.read(reinterpret_cast<char*>(&audioFormat), 2);
            file.read(reinterpret_cast<char*>(&numChannels), 2);
            file.read(reinterpret_cast<char*>(&sampleRate), 4);
            file.read(reinterpret_cast<char*>(&byteRate), 4);
            file.read(reinterpret_cast<char*>(&blockAlign), 2);
            file.read(reinterpret_cast<char*>(&bitsPerSample), 2);

            info.formatName = "WAV";
            info.sampleRate = static_cast<int32_t>(sampleRate);
            info.channels = static_cast<int32_t>(numChannels);
            info.bitDepth = static_cast<int32_t>(bitsPerSample);
            info.bitRateKbps = static_cast<int32_t>((byteRate * 8) / 1000);
            return true;
        } else {
            file.seekg(chunkSize, std::ios::cur);
        }
    }
    return false;
}

bool NativeTagInspector::inspectDsd(const std::string& path, AudioMetadataInfo& info) {
    info.formatName = "DSD";
    info.sampleRate = 2822400; // DSD64 1-bit / 2.8224MHz standard
    info.bitDepth = 1;
    info.channels = 2;
    info.qualityCategory = AudioQualityCategory::HI_RES_LOSSLESS;
    info.badgeText = "DSD256 HI-RES";
    return true;
}

bool NativeTagInspector::inspectMp3(const std::string& path, AudioMetadataInfo& info) {
    info.formatName = "MP3";
    info.sampleRate = 44100;
    info.bitDepth = 16;
    info.channels = 2;
    info.bitRateKbps = 320;
    info.qualityCategory = AudioQualityCategory::LOSSY;
    info.badgeText = "LOSSY";
    return true;
}

void NativeTagInspector::categorizeQuality(AudioMetadataInfo& info) {
    if (info.formatName == "MP3" || info.formatName == "AAC" || info.formatName == "OGG" || info.formatName == "OPUS") {
        info.qualityCategory = AudioQualityCategory::LOSSY;
        std::ostringstream ss;
        ss << info.formatName;
        if (info.bitRateKbps > 0) ss << " " << info.bitRateKbps << "k";
        info.badgeText = ss.str();
    } else if (info.sampleRate > 48000 || (info.bitDepth > 16 && info.sampleRate >= 48000) || info.formatName == "DSD") {
        info.qualityCategory = AudioQualityCategory::HI_RES_LOSSLESS;
        std::ostringstream ss;
        if (info.formatName == "DSD") {
            ss << "DSD HI-RES";
        } else {
            ss << "HI-RES " << info.bitDepth << "-BIT / " << (info.sampleRate / 1000.0f) << "kHz";
        }
        info.badgeText = ss.str();
    } else {
        info.qualityCategory = AudioQualityCategory::LOSSLESS;
        std::ostringstream ss;
        ss << "LOSSLESS " << info.bitDepth << "-BIT / " << (info.sampleRate / 1000.0f) << "kHz";
        info.badgeText = ss.str();
    }
}
