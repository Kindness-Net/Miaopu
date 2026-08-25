package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MainContentSwipeTest {
    @Test
    fun `switches esport before main section`() {
        assertEquals(
            MainContentSwipeTarget(MainSection.HOME, esportIndex = 1),
            resolveMainContentSwipeTarget(
                currentSection = MainSection.HOME,
                currentEsportIndex = 0,
                esportCount = 3,
                direction = MainContentSwipeDirection.NEXT,
            ),
        )
        assertEquals(
            MainContentSwipeTarget(MainSection.EVENTS, esportIndex = 0),
            resolveMainContentSwipeTarget(
                currentSection = MainSection.EVENTS,
                currentEsportIndex = 1,
                esportCount = 3,
                direction = MainContentSwipeDirection.PREVIOUS,
            ),
        )
    }

    @Test
    fun `switches main section after reaching esport boundary`() {
        assertEquals(
            MainContentSwipeTarget(MainSection.EVENTS, esportIndex = 2),
            resolveMainContentSwipeTarget(
                currentSection = MainSection.HOME,
                currentEsportIndex = 2,
                esportCount = 3,
                direction = MainContentSwipeDirection.NEXT,
            ),
        )
        assertEquals(
            MainContentSwipeTarget(MainSection.PROFILE, esportIndex = 2),
            resolveMainContentSwipeTarget(
                currentSection = MainSection.EVENTS,
                currentEsportIndex = 2,
                esportCount = 3,
                direction = MainContentSwipeDirection.NEXT,
            ),
        )
    }

    @Test
    fun `profile swipes directly between main sections`() {
        assertEquals(
            MainContentSwipeTarget(MainSection.EVENTS, esportIndex = 1),
            resolveMainContentSwipeTarget(
                currentSection = MainSection.PROFILE,
                currentEsportIndex = 1,
                esportCount = 3,
                direction = MainContentSwipeDirection.PREVIOUS,
            ),
        )
        assertNull(
            resolveMainContentSwipeTarget(
                currentSection = MainSection.PROFILE,
                currentEsportIndex = 1,
                esportCount = 3,
                direction = MainContentSwipeDirection.NEXT,
            ),
        )
    }
}
