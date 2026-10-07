package com.example.update

import com.example.kodiwellness.update.AppReleaseInfo
import com.example.kodiwellness.update.GitHubUpdateManager
import com.example.kodiwellness.update.UpdateStatus
import org.junit.Assert.*
import org.junit.Test
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GitHubUpdateTest {

    @Test
    fun testIsNewerVersion_comparingSemver() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = GitHubUpdateManager(context)

        // Remote higher patch
        assertTrue(manager.isNewerVersion("1.0.1", 2, "1.0.0", 1))

        // Remote higher minor
        assertTrue(manager.isNewerVersion("1.1.0", 2, "1.0.0", 1))

        // Remote higher major
        assertTrue(manager.isNewerVersion("2.0.0", 2, "1.0.0", 1))

        // Same version
        assertFalse(manager.isNewerVersion("1.0.0", 1, "1.0.0", 1))

        // Local newer than remote
        assertFalse(manager.isNewerVersion("0.9.9", 1, "1.0.0", 2))
    }

    @Test
    fun testIsNewerVersion_whenVersionCodeHigher() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = GitHubUpdateManager(context)

        // Version name same, but version code higher
        assertTrue(manager.isNewerVersion("1.0.0", 5, "1.0.0", 4))
    }

    @Test
    fun testUpdateStatusTypes() {
        val release = AppReleaseInfo(
            versionName = "1.1.0",
            versionCode = 2,
            releaseTitle = "Production Update 1.1.0",
            releaseNotes = "Bug fixes and improvements",
            publishedAt = "2026-10-07",
            apkDownloadUrl = "https://github.com/my-org/kodi-wellness/releases/download/v1.1.0/app-release.apk",
            releasePageUrl = "https://github.com/my-org/kodi-wellness/releases/tag/v1.1.0",
            apkSize = 15000000L,
            isMandatory = false
        )

        val status = UpdateStatus.UpdateAvailable(
            release = release,
            currentVersion = "1.0.0"
        )

        assertEquals("1.1.0", status.release.versionName)
        assertEquals("1.0.0", status.currentVersion)
    }

    @Test
    fun testRepositoryConfiguration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = GitHubUpdateManager(context)

        manager.setRepository("https://github.com/testorg/testrepo/")
        assertEquals("testorg/testrepo", manager.getRepository())
    }
}
