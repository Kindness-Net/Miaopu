package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCommentCardTest {
    @Test
    fun `single visible preview does not need a reply toggle`() {
        assertFalse(shouldShowReplyToggle(replyCount = 1, hasPreview = true, expanded = false))
    }

    @Test
    fun `single missing preview keeps a way to load the reply`() {
        assertTrue(shouldShowReplyToggle(replyCount = 1, hasPreview = false, expanded = false))
    }

    @Test
    fun `multiple or expanded replies keep the toggle`() {
        assertTrue(shouldShowReplyToggle(replyCount = 2, hasPreview = true, expanded = false))
        assertTrue(shouldShowReplyToggle(replyCount = 1, hasPreview = true, expanded = true))
    }
}
