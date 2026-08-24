package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseAdapterTest {
    @Test
    fun `release parser keeps tag title and page url`() {
        val release = parseGitHubRelease(
            """
                {
                  "tag_name":"1.0",
                  "name":"喵扑 1.0",
                  "html_url":"https://github.com/KiritoXDone/Miaopu/releases/tag/1.0",
                  "published_at":"2026-08-24T10:00:00Z"
                }
            """.trimIndent(),
        )

        assertEquals("1.0", release.tagName)
        assertEquals("喵扑 1.0", release.title)
        assertEquals("https://github.com/KiritoXDone/Miaopu/releases/tag/1.0", release.pageUrl)
    }

    @Test
    fun `version comparison accepts optional v prefix and missing patch`() {
        assertTrue(isNewerVersion("v1.1.0", "1.0"))
        assertTrue(isNewerVersion("1.0.1", "1.0"))
        assertFalse(isNewerVersion("1.0.0", "1.0"))
        assertFalse(isNewerVersion("0.9.9", "1.0"))
    }
}
