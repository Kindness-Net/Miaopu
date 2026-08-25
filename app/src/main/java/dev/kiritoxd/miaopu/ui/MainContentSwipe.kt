package dev.kiritoxd.miaopu.ui

internal enum class MainContentSwipeDirection {
    PREVIOUS,
    NEXT,
}

internal data class MainContentSwipeTarget(
    val section: MainSection,
    val esportIndex: Int,
)

internal fun resolveMainContentSwipeTarget(
    currentSection: MainSection,
    currentEsportIndex: Int,
    esportCount: Int,
    direction: MainContentSwipeDirection,
): MainContentSwipeTarget? {
    if (esportCount <= 0) return null
    val esportIndex = currentEsportIndex.coerceIn(0, esportCount - 1)

    if (currentSection != MainSection.PROFILE) {
        val adjacentEsportIndex = when (direction) {
            MainContentSwipeDirection.PREVIOUS -> esportIndex - 1
            MainContentSwipeDirection.NEXT -> esportIndex + 1
        }
        if (adjacentEsportIndex in 0 until esportCount) {
            return MainContentSwipeTarget(currentSection, adjacentEsportIndex)
        }
    }

    val adjacentSectionIndex = when (direction) {
        MainContentSwipeDirection.PREVIOUS -> currentSection.ordinal - 1
        MainContentSwipeDirection.NEXT -> currentSection.ordinal + 1
    }
    val adjacentSection = MainSection.entries.getOrNull(adjacentSectionIndex) ?: return null
    return MainContentSwipeTarget(adjacentSection, esportIndex)
}
