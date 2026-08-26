package com.ipodmodern.audio.core.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbDacInfo(
    val isConnected: Boolean,
    val deviceName: String,
    val maxSampleRate: Int,
    val supportsDoP: Boolean,
    val bitDepths: List<Int>
)

class UsbAudioDacDetector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _dacState = MutableStateFlow(checkConnectedDac())
    val dacState: StateFlow<UsbDacInfo> = _dacState.asStateFlow()

    fun checkConnectedDac(): UsbDacInfo {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_USB_ACCESSORY) {

                val name = device.productName.toString().ifBlank { "External USB DAC" }
                val rates = device.sampleRates
                val maxRate = rates.maxOrNull() ?: 192000

                return UsbDacInfo(
                    isConnected = true,
                    deviceName = name,
                    maxSampleRate = maxRate,
                    supportsDoP = maxRate >= 176400,
                    bitDepths = listOf(16, 24, 32)
                )
            }
        }

        return UsbDacInfo(
            isConnected = false,
            deviceName = "Internal Audio HAL",
            maxSampleRate = 48000,
            supportsDoP = false,
            bitDepths = listOf(16, 24)
        )
    }

    fun refresh() {
        _dacState.value = checkConnectedDac()
    }
}
