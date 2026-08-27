#include <jni.h>
#include <string>
#include <vector>
#include "audio_engine.h"
#include "native_tag_inspector.h"


extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_initEngine(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jint sampleRate,
    jboolean exclusiveMode) {
    return static_cast<jboolean>(AudioEngine::getInstance().startStream(sampleRate, exclusiveMode));
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_destroyEngine(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    AudioEngine::getInstance().stopStream();
}

JNIEXPORT jboolean JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_loadTrack(
    JNIEnv* env,
    jobject /*thiz*/,
    jstring filePath) {
    if (!filePath) return JNI_FALSE;
    const char* pathStr = env->GetStringUTFChars(filePath, nullptr);
    bool result = AudioEngine::getInstance().loadTrack(std::string(pathStr));
    env->ReleaseStringUTFChars(filePath, pathStr);
    return static_cast<jboolean>(result);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_play(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    AudioEngine::getInstance().play();
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_pause(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    AudioEngine::getInstance().pause();
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_stop(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    AudioEngine::getInstance().stop();
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_seekTo(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jlong positionMs) {
    AudioEngine::getInstance().seekToMs(positionMs);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_setVolume(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jfloat volume) {
    AudioEngine::getInstance().setVolume(volume);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_triggerNativeClick(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jfloat volume) {
    AudioEngine::getInstance().triggerClick(volume);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_setClickEnabled(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jboolean enabled) {
    AudioEngine::getInstance().setClickSoundEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_setEqBandGain(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jint bandIndex,
    jfloat gainDb) {
    AudioEngine::getInstance().setEqBandGain(static_cast<size_t>(bandIndex), gainDb);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_setEqAllBands(
    JNIEnv* env,
    jobject /*thiz*/,
    jfloatArray gainsArray) {
    if (!gainsArray) return;
    jsize len = env->GetArrayLength(gainsArray);
    if (len < 10) return;
    jfloat* elements = env->GetFloatArrayElements(gainsArray, nullptr);
    AudioEngine::getInstance().setEqAllBands(elements);
    env->ReleaseFloatArrayElements(gainsArray, elements, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_setEqEnabled(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jboolean enabled) {
    AudioEngine::getInstance().setEqEnabled(enabled);
}

JNIEXPORT jfloat JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_getDynamicPrecutGainDb(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    return AudioEngine::getInstance().getDynamicPrecutGainDb();
}

JNIEXPORT jlong JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_getCurrentPositionMs(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    return static_cast<jlong>(AudioEngine::getInstance().getCurrentPositionMs());
}

JNIEXPORT jlong JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_getDurationMs(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    return static_cast<jlong>(AudioEngine::getInstance().getDurationMs());
}

JNIEXPORT jboolean JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_isPlaying(
    JNIEnv* /*env*/,
    jobject /*thiz*/) {
    return static_cast<jboolean>(AudioEngine::getInstance().isPlaying());
}

JNIEXPORT jobject JNICALL
Java_com_ipodmodern_audio_core_audio_NativeAudioBridge_inspectFileMetadata(
    JNIEnv* env,
    jobject /*thiz*/,
    jstring filePath) {
    if (!filePath) return nullptr;
    const char* pathStr = env->GetStringUTFChars(filePath, nullptr);
    AudioMetadataInfo info = NativeTagInspector::inspectFile(std::string(pathStr));
    env->ReleaseStringUTFChars(filePath, pathStr);

    jclass metaClass = env->FindClass("com/ipodmodern/audio/core/model/NativeAudioMetadata");
    if (!metaClass) return nullptr;

    jmethodID constructor = env->GetMethodID(
        metaClass,
        "<init>",
        "(Ljava/lang/String;IIIIJLjava/lang/String;I)V"
    );
    if (!constructor) return nullptr;

    jstring formatStr = env->NewStringUTF(info.formatName.c_str());
    jstring badgeStr = env->NewStringUTF(info.badgeText.c_str());

    jobject result = env->NewObject(
        metaClass,
        constructor,
        formatStr,
        info.sampleRate,
        info.bitDepth,
        info.channels,
        info.bitRateKbps,
        static_cast<jlong>(info.durationMs),
        badgeStr,
        static_cast<jint>(info.qualityCategory)
    );

    return result;
}

}
