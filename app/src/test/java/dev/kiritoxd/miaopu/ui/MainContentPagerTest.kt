package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MainContentPagerTest {
    @Test
    fun `orders esports inside each main section`() {
        assertEquals(
            listOf(
                MainContentDestination(MainSection.HOME, esportIndex = 0),
                MainContentDestination(MainSection.HOME, esportIndex = 1),
                MainContentDestination(MainSection.HOME, esportIndex = 2),
                MainContentDestination(MainSection.EVENTS, esportIndex = 0),
                MainContentDestination(MainSection.EVENTS, esportIndex = 1),
                MainContentDestination(MainSection.EVENTS, esportIndex = 2),
                MainContentDestination(MainSection.PROFILE, esportIndex = null),
            ),
            mainContentDestinations(esportCount = 3),
        )
    }

    @Test
    fun `resolves selected destination index`() {
        assertEquals(1, mainContentDestinationIndex(MainSection.HOME, esportIndex = 1, esportCount = 3))
        assertEquals(5, mainContentDestinationIndex(MainSection.EVENTS, esportIndex = 2, esportCount = 3))
        assertEquals(6, mainContentDestinationIndex(MainSection.PROFILE, esportIndex = 0, esportCount = 3))
    }

    @Test
    fun `keeps profile available without esports`() {
        assertEquals(
            listOf(MainContentDestination(MainSection.PROFILE, esportIndex = null)),
            mainContentDestinations(esportCount = 0),
        )
        assertEquals(0, mainContentDestinationIndex(MainSection.PROFILE, esportIndex = 0, esportCount = 0))
    }
}
