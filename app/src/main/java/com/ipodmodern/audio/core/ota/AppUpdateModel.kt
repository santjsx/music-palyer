package com.ipodmodern.audio.core.ota

import java.io.File

/**
 * Metadata for an available in-app Over-The-Air (OTA) update from GitHub Releases.
 */
data class AppUpdateInfo(
    val tagName: String,
    val versionName: String,
    val changelog: String,
    val downloadUrl: String,
    val apkSizeBytes: Long,
    val publishedAt: String,
    val isMandatory: Boolean = false
)

/**
 * High-precision state machine representing Play Store level in-app update progress.
 */
sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpToDate(val currentVersion: String) : UpdateStatus
    data class Available(val info: AppUpdateInfo) : UpdateStatus
    data class Downloading(
        val info: AppUpdateInfo,
        val progress: Float, // 0.0f .. 1.0f
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBps: Long
    ) : UpdateStatus
    data class Downloaded(val info: AppUpdateInfo, val apkFile: File) : UpdateStatus
    data class Installing(val info: AppUpdateInfo) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
