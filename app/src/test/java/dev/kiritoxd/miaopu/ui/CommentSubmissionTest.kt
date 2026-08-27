package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentSubmissionTest {
    @Test
    fun textCanBeSubmittedWithoutChangingScore() {
        assertTrue(canSubmitCommentOrScore("评论", selectedScore = 8, userScore = 8))
    }

    @Test
    fun changedScoreCanBeSubmittedWithoutText() {
        assertTrue(canSubmitCommentOrScore("", selectedScore = 10, userScore = 8))
    }

    @Test
    fun emptyTextAndUnchangedScoreCannotBeSubmitted() {
        assertFalse(canSubmitCommentOrScore(" ", selectedScore = 8, userScore = 8))
        assertFalse(canSubmitCommentOrScore("", selectedScore = 0, userScore = 0))
    }
}
