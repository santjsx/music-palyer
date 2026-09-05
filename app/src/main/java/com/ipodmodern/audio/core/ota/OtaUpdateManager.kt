package com.ipodmodern.audio.core.ota

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Industry-standard In-App OTA Update Engine for GitHub Releases.
 * Supports background chunked streaming downloads with real-time speed calculation,
 * semantic version comparison, and seamless Android PackageInstaller handoff via FileProvider.
 */
class OtaUpdateManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _updateState = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateState: StateFlow<UpdateStatus> = _updateState.asStateFlow()

    private var downloadJob: Job? = null

    companion object {
        private const val GITHUB_REPO_OWNER = "santjsx"
        private const val GITHUB_REPO_NAME = "music-palyer"
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"
        const val CURRENT_APP_VERSION = "3.0.0"

        @Volatile
        private var instance: OtaUpdateManager? = null

        fun getInstance(context: Context): OtaUpdateManager {
            return instance ?: synchronized(this) {
                instance ?: OtaUpdateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Resolves the current version name dynamically from PackageManager.
     */
    fun getCurrentVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: CURRENT_APP_VERSION
        } catch (e: Exception) {
            CURRENT_APP_VERSION
        }
    }

    /**
     * Checks GitHub API for newer releases compared to installed version.
     */
    fun checkForUpdates(isManualCheck: Boolean = false) {
        if (_updateState.value is UpdateStatus.Checking || _updateState.value is UpdateStatus.Downloading) {
            return
        }

        _updateState.value = UpdateStatus.Checking
        val currentInstalledVersion = getCurrentVersion()

        scope.launch {
            try {
                val url = URL(LATEST_RELEASE_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", "TuneHive-Music-Android")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonString)

                    val tagName = json.optString("tag_name", "").trim()
                    val remoteVersion = tagName.removePrefix("v").trim()
                    val bodyNotes = json.optString("body", "Bug fixes and performance improvements.")
                    val publishedAt = json.optString("published_at", "")

                    // Prioritize app-release.apk specifically to avoid signature mismatch
                    var apkUrl: String? = null
                    var apkSize: Long = 0L

                    val assetsArray = json.optJSONArray("assets")
                    if (assetsArray != null) {
                        var bestAsset: JSONObject? = null
                        for (i in 0 until assetsArray.length()) {
                            val asset = assetsArray.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.equals("app-release.apk", ignoreCase = true)) {
                                bestAsset = asset
                                break
                            } else if (bestAsset == null && name.contains("release", ignoreCase = true) && name.endsWith(".apk", ignoreCase = true)) {
                                bestAsset = asset
                            } else if (bestAsset == null && name.endsWith(".apk", ignoreCase = true)) {
                                bestAsset = asset
                            }
                        }

                        if (bestAsset != null) {
                            apkUrl = bestAsset.optString("browser_download_url")
                            apkSize = bestAsset.optLong("size", 0L)
                        }
                    }

                    if (apkUrl.isNullOrBlank()) {
                        _updateState.value = if (isManualCheck) {
                            UpdateStatus.UpToDate(currentInstalledVersion)
                        } else {
                            UpdateStatus.Idle
                        }
                        return@launch
                    }

                    val isNewer = isVersionNewer(remoteVersion, currentInstalledVersion)
                    if (isNewer) {
                        val updateInfo = AppUpdateInfo(
                            tagName = tagName,
                            versionName = remoteVersion,
                            changelog = cleanChangelog(bodyNotes),
                            downloadUrl = apkUrl,
                            apkSizeBytes = apkSize,
                            publishedAt = publishedAt
                        )
                        _updateState.value = UpdateStatus.Available(updateInfo)
                    } else {
                        _updateState.value = if (isManualCheck) {
                            UpdateStatus.UpToDate(currentInstalledVersion)
                        } else {
                            UpdateStatus.Idle
                        }
                    }
                } else {
                    _updateState.value = if (isManualCheck) {
                        UpdateStatus.Error("Server returned code $responseCode")
                    } else {
                        UpdateStatus.Idle
                    }
                }
            } catch (e: Exception) {
                _updateState.value = if (isManualCheck) {
                    UpdateStatus.Error(e.message ?: "Failed to connect to update server")
                } else {
                    UpdateStatus.Idle
                }
            }
        }
    }

    /**
     * Downloads the APK file in the background with progress and speed metrics.
     */
    fun startDownload(info: AppUpdateInfo) {
        if (downloadJob?.isActive == true) return

        downloadJob = scope.launch {
            _updateState.value = UpdateStatus.Downloading(
                info = info,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = info.apkSizeBytes,
                speedBps = 0L
            )

            try {
                val updatesDir = File(context.cacheDir, "ota_updates").apply { mkdirs() }
                val targetFile = File(updatesDir, "update_${info.versionName}.apk")
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                // Download through redirect chain (supporting 301, 302, 303, 307, 308)
                var currentUrl = info.downloadUrl
                var connection: HttpURLConnection
                var redirectCount = 0

                while (true) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 20000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", "TuneHive-Music-Android")
                        setRequestProperty("Accept", "application/octet-stream, application/vnd.android.package-archive")
                    }

                    val status = connection.responseCode
                    if (status in listOf(
                        HttpURLConnection.HTTP_MOVED_PERM,
                        HttpURLConnection.HTTP_MOVED_TEMP,
                        HttpURLConnection.HTTP_SEE_OTHER,
                        307, // Temporary Redirect
                        308  // Permanent Redirect
                    )) {
                        val nextUrl = connection.getHeaderField("Location")
                        if (nextUrl.isNullOrBlank()) {
                            throw Exception("Redirect received with empty Location header")
                        }
                        currentUrl = nextUrl
                        redirectCount++
                        if (redirectCount > 8) throw Exception("Too many redirects")
                        continue
                    }

                    if (status != HttpURLConnection.HTTP_OK) {
                        throw Exception("Update server returned HTTP $status (${connection.responseMessage})")
                    }
                    break
                }

                val totalLength = connection.contentLengthLong.let { if (it > 0) it else info.apkSizeBytes }
                var downloadedBytes = 0L
                var lastSpeedCalcTime = System.currentTimeMillis()
                var lastBytesForSpeed = 0L
                var currentSpeedBps = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(32 * 1024)
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val now = System.currentTimeMillis()
                            val timeDelta = now - lastSpeedCalcTime
                            if (timeDelta >= 350) {
                                val bytesDelta = downloadedBytes - lastBytesForSpeed
                                currentSpeedBps = (bytesDelta * 1000) / timeDelta.coerceAtLeast(1)
                                lastSpeedCalcTime = now
                                lastBytesForSpeed = downloadedBytes

                                val progress = if (totalLength > 0) (downloadedBytes.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f) else 0f
                                _updateState.value = UpdateStatus.Downloading(
                                    info = info,
                                    progress = progress,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalLength,
                                    speedBps = currentSpeedBps
                                )
                            }
                        }
                        output.flush()
                    }
                }

                _updateState.value = UpdateStatus.Downloaded(info, targetFile)
            } catch (e: CancellationException) {
                _updateState.value = UpdateStatus.Idle
            } catch (e: Exception) {
                _updateState.value = UpdateStatus.Error(e.message ?: "Download failed")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _updateState.value = UpdateStatus.Idle
    }

    fun dismissUpdate() {
        _updateState.value = UpdateStatus.Idle
    }

    /**
     * Handoff to Android OS PackageInstaller to install the downloaded APK.
     */
    fun installApk(activityContext: Context, apkFile: File) {
        if (!apkFile.exists()) {
            _updateState.value = UpdateStatus.Error("APK file not found")
            return
        }

        // On Android 8.0+ (API 26+), verify install unknown apps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activityContext.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activityContext.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activityContext.startActivity(settingsIntent)
                return
            }
        }

        try {
            val apkUri = FileProvider.getUriForFile(
                activityContext,
                "${activityContext.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            activityContext.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateStatus.Error("Failed to launch package installer: ${e.message}")
        }
    }

    /**
     * Compares two semantic version strings (e.g. "2.3.0" vs "2.2.0").
     * Returns true if remote is strictly newer than current.
     */
    private fun isVersionNewer(remote: String, current: String): Boolean {
        val remoteParts = remote.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun cleanChangelog(raw: String): String {
        return raw.trim()
            .replace(Regex("##+"), "•")
            .ifBlank { "Exciting performance improvements and new flagship features." }
    }
}
