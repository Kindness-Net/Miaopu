package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CommentImageSaverTest {
    @Test
    fun `infers common image types without trusting query parameters`() {
        assertEquals("image/png", imageMimeType("https://example.com/image.png?width=1080"))
        assertEquals("image/gif", imageMimeType("https://example.com/animated.GIF"))
        assertEquals("image/webp", imageMimeType("https://example.com/image.webp"))
    }

    @Test
    fun `falls back to jpeg for extensionless image urls`() {
        assertEquals("image/jpeg", imageMimeType("https://example.com/image/123"))
    }
}
