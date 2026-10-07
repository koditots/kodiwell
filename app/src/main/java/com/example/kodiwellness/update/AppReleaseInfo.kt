package com.example.kodiwellness.update

/**
 * Metadata for a Kodi Wellness application release published on GitHub.
 */
data class AppReleaseInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAt: String,
    val apkDownloadUrl: String?,
    val releasePageUrl: String,
    val apkSize: Long = 0L,
    val isMandatory: Boolean = false
)
