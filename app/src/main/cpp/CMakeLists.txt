cmake_minimum_required(VERSION 3.22.1)

project("ipod_audio_engine")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization flags for real-time DSP
add_compile_options(-O3 -ffast-math -flto)

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Source files
set(SOURCE_FILES
    biquad_filter.cpp
    dsp_equalizer.cpp
    click_synthesizer.cpp
    native_tag_inspector.cpp
    audio_decoder.cpp
    audio_engine.cpp
    jni_bridge.cpp
)

# Build shared library
add_library(ipod_audio_engine SHARED ${SOURCE_FILES})

# Link libraries (Android log, AAudio, OpenSL ES, math)
find_library(LOG_LIB log)
find_library(AAUDIO_LIB aaudio)
find_library(OPENSL_LIB OpenSLES)
find_library(MATH_LIB m)

target_link_libraries(
    ipod_audio_engine
    ${LOG_LIB}
    ${AAUDIO_LIB}
    ${OPENSL_LIB}
    ${MATH_LIB}
)
