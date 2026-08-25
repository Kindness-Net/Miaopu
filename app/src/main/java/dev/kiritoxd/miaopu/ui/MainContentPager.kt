package dev.kiritoxd.miaopu.ui

internal data class MainContentDestination(
    val section: MainSection,
    val esportIndex: Int?,
)

internal fun mainContentDestinations(esportCount: Int): List<MainContentDestination> = buildList {
    repeat(esportCount.coerceAtLeast(0)) { esportIndex ->
        add(MainContentDestination(MainSection.HOME, esportIndex))
    }
    repeat(esportCount.coerceAtLeast(0)) { esportIndex ->
        add(MainContentDestination(MainSection.EVENTS, esportIndex))
    }
    add(MainContentDestination(MainSection.PROFILE, esportIndex = null))
}

internal fun mainContentDestinationIndex(
    section: MainSection,
    esportIndex: Int,
    esportCount: Int,
): Int {
    if (esportCount <= 0 || section == MainSection.PROFILE) return (esportCount.coerceAtLeast(0) * 2)
    val safeEsportIndex = esportIndex.coerceIn(0, esportCount - 1)
    return when (section) {
        MainSection.HOME -> safeEsportIndex
        MainSection.EVENTS -> esportCount + safeEsportIndex
        MainSection.PROFILE -> esportCount * 2
    }
}
