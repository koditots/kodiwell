package com.example.kodiwellness.update

import java.io.File

/**
 * Represents the current status of the GitHub OTA app update system.
 */
sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    data class UpToDate(
        val currentVersion: String,
        val checkedAt: Long = System.currentTimeMillis()
    ) : UpdateStatus

    data class UpdateAvailable(
        val release: AppReleaseInfo,
        val currentVersion: String
    ) : UpdateStatus

    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateStatus

    data class ReadyToInstall(
        val apkFile: File,
        val release: AppReleaseInfo
    ) : UpdateStatus

    data class Error(
        val message: String
    ) : UpdateStatus
}
