package com.example.kodiwellness.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class GitHubUpdateManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("kodi_update_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    fun getRepository(): String {
        return prefs.getString(PREF_GITHUB_REPO, BuildConfig.DEFAULT_GITHUB_REPO) ?: BuildConfig.DEFAULT_GITHUB_REPO
    }

    fun setRepository(repo: String) {
        val sanitized = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        prefs.edit().putString(PREF_GITHUB_REPO, sanitized).apply()
    }

    suspend fun checkForUpdates(manual: Boolean = false): UpdateStatus = withContext(Dispatchers.IO) {
        _updateStatus.value = UpdateStatus.Checking
        val currentVersionName = BuildConfig.VERSION_NAME
        val currentVersionCode = BuildConfig.VERSION_CODE
        val repo = getRepository()

        try {
            // Step 1: Check GitHub Releases API
            val releaseInfo = fetchLatestGitHubRelease(repo)
                ?: fetchRawManifestRelease(repo)

            if (releaseInfo == null) {
                val upToDate = UpdateStatus.UpToDate(currentVersionName)
                _updateStatus.value = upToDate
                return@withContext upToDate
            }

            val isNewer = isNewerVersion(
                remoteVersion = releaseInfo.versionName,
                remoteCode = releaseInfo.versionCode,
                localVersion = currentVersionName,
                localCode = currentVersionCode
            )

            val status = if (isNewer) {
                UpdateStatus.UpdateAvailable(
                    release = releaseInfo,
                    currentVersion = currentVersionName
                )
            } else {
                UpdateStatus.UpToDate(currentVersionName)
            }

            _updateStatus.value = status
            status
        } catch (e: Exception) {
            Log.e("GitHubUpdateManager", "Error checking for updates: ${e.message}", e)
            val errStatus = UpdateStatus.Error(
                message = e.localizedMessage ?: "Failed to connect to GitHub. Please check internet connection."
            )
            _updateStatus.value = errStatus
            errStatus
        }
    }

    private fun fetchLatestGitHubRelease(repo: String): AppReleaseInfo? {
        val url = "https://api.github.com/repos/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "KodiWellness-App/${BuildConfig.VERSION_NAME}")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                parseReleaseJson(JSONObject(body), repo)
            }
        } catch (e: Exception) {
            Log.w("GitHubUpdateManager", "Failed to fetch from releases API: ${e.message}")
            null
        }
    }

    private fun fetchRawManifestRelease(repo: String): AppReleaseInfo? {
        val url = "https://raw.githubusercontent.com/$repo/main/app-version.json"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "KodiWellness-App/${BuildConfig.VERSION_NAME}")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)

                val vName = json.optString("versionName", "1.0.0")
                val vCode = json.optInt("versionCode", 1)
                val title = json.optString("releaseTitle", "Kodi Wellness v$vName")
                val notes = json.optString("releaseNotes", "New update available from GitHub.")
                val apkUrl = json.optString("apkUrl", "https://github.com/$repo/releases/latest/download/app-release.apk")
                val releaseUrl = json.optString("releaseUrl", "https://github.com/$repo/releases/latest")
                val mandatory = json.optBoolean("isMandatory", false)

                AppReleaseInfo(
                    versionName = vName,
                    versionCode = vCode,
                    releaseTitle = title,
                    releaseNotes = notes,
                    publishedAt = json.optString("releaseDate", ""),
                    apkDownloadUrl = apkUrl,
                    releasePageUrl = releaseUrl,
                    isMandatory = mandatory
                )
            }
        } catch (e: Exception) {
            Log.w("GitHubUpdateManager", "Failed to fetch raw manifest: ${e.message}")
            null
        }
    }

    private fun parseReleaseJson(json: JSONObject, repo: String): AppReleaseInfo {
        val rawTag = json.optString("tag_name", "1.0.0")
        val cleanVersion = rawTag.removePrefix("v").trim()
        val title = json.optString("name", "Release $rawTag")
        val body = json.optString("body", "No changelog provided.")
        val publishedAt = json.optString("published_at", "")
        val htmlUrl = json.optString("html_url", "https://github.com/$repo/releases/latest")

        var apkUrl: String? = null
        var apkSize = 0L

        val assets: JSONArray? = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val assetName = asset.optString("name", "")
                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url", null)
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }
        }

        if (apkUrl.isNullOrEmpty()) {
            apkUrl = "https://github.com/$repo/releases/latest/download/app-release.apk"
        }

        return AppReleaseInfo(
            versionName = cleanVersion,
            versionCode = extractVersionCode(cleanVersion),
            releaseTitle = title,
            releaseNotes = body,
            publishedAt = publishedAt,
            apkDownloadUrl = apkUrl,
            releasePageUrl = htmlUrl,
            apkSize = apkSize
        )
    }

    private fun extractVersionCode(versionName: String): Int {
        val parts = versionName.split(".")
        var code = 0
        var multiplier = 10000
        for (part in parts) {
            val num = part.filter { it.isDigit() }.toIntOrNull() ?: 0
            code += num * multiplier
            multiplier /= 100
            if (multiplier == 0) break
        }
        return code.coerceAtLeast(1)
    }

    fun isNewerVersion(
        remoteVersion: String,
        remoteCode: Int,
        localVersion: String,
        localCode: Int
    ): Boolean {
        if (remoteCode > localCode && remoteCode > 0 && localCode > 0) return true

        val remoteParts = remoteVersion.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val localParts = localVersion.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    suspend fun downloadUpdate(
        release: AppReleaseInfo,
        onProgress: (Float, Long, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadUrl = release.apkDownloadUrl ?: release.releasePageUrl
        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "KodiWellness-App/${BuildConfig.VERSION_NAME}")
            .build()

        try {
            _updateStatus.value = UpdateStatus.Downloading(0f, 0L, release.apkSize)

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Failed to download APK: HTTP ${response.code}")
                }

                val responseBody = response.body
                    ?: throw IllegalStateException("Download body is empty")

                val totalBytes = if (responseBody.contentLength() > 0) {
                    responseBody.contentLength()
                } else {
                    release.apkSize.coerceAtLeast(1L)
                }

                val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: File(context.cacheDir, "updates")
                if (!targetDir.exists()) targetDir.mkdirs()

                val outputFile = File(targetDir, "KodiWellness-v${release.versionName}.apk")
                if (outputFile.exists()) outputFile.delete()

                responseBody.byteStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            val progress = if (totalBytes > 0) totalRead.toFloat() / totalBytes else 0.5f
                            _updateStatus.value = UpdateStatus.Downloading(progress.coerceIn(0f, 1f), totalRead, totalBytes)
                            onProgress(progress, totalRead, totalBytes)
                        }
                        output.flush()
                    }
                }

                _updateStatus.value = UpdateStatus.ReadyToInstall(outputFile, release)
                Result.success(outputFile)
            }
        } catch (e: Exception) {
            Log.e("GitHubUpdateManager", "APK download error: ${e.message}", e)
            val errStatus = UpdateStatus.Error("Download failed: ${e.localizedMessage}")
            _updateStatus.value = errStatus
            Result.failure(e)
        }
    }

    fun installUpdate(apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            _updateStatus.value = UpdateStatus.Error("APK file not found or empty.")
            return false
        }

        // On Android 8.0+ (API 26+), check permission to install from unknown sources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return false
            }
        }

        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e("GitHubUpdateManager", "Failed to launch package installer: ${e.message}", e)
            _updateStatus.value = UpdateStatus.Error("Could not launch package installer: ${e.localizedMessage}")
            false
        }
    }

    fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("GitHubUpdateManager", "Failed to open browser: ${e.message}")
        }
    }

    fun dismissUpdate() {
        _updateStatus.value = UpdateStatus.Idle
    }

    companion object {
        private const val PREF_GITHUB_REPO = "saved_github_repo"
    }
}
