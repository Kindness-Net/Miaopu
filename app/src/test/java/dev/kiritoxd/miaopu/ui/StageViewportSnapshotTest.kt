package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class StageViewportSnapshotTest {
    @Test
    fun `coerceFor keeps a valid viewport unchanged`() {
        val snapshot = StageViewportSnapshot(
            listIndex = 4,
            listOffset = 120,
            selectedTabIndex = 1,
            selectedOrderIndex = 2,
        )

        assertEquals(snapshot, snapshot.coerceFor(tabCount = 3, orderCount = 4, itemCount = 10))
    }

    @Test
    fun `coerceFor clamps stale indexes and negative offset`() {
        val snapshot = StageViewportSnapshot(
            listIndex = 20,
            listOffset = -5,
            selectedTabIndex = 8,
            selectedOrderIndex = 9,
        )

        assertEquals(
            StageViewportSnapshot(
                listIndex = 0,
                listOffset = 0,
                selectedTabIndex = 0,
                selectedOrderIndex = 0,
            ),
            snapshot.coerceFor(tabCount = 0, orderCount = 0, itemCount = 0),
        )
    }
}
