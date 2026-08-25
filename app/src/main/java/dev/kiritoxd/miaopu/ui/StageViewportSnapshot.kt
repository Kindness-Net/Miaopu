package dev.kiritoxd.miaopu.ui

internal data class StageViewportSnapshot(
    val listIndex: Int,
    val listOffset: Int,
    val selectedTabIndex: Int,
    val selectedOrderIndex: Int,
) {
    fun coerceFor(tabCount: Int, orderCount: Int, itemCount: Int): StageViewportSnapshot = copy(
        listIndex = listIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0)),
        listOffset = listOffset.coerceAtLeast(0),
        selectedTabIndex = selectedTabIndex.coerceIn(0, (tabCount - 1).coerceAtLeast(0)),
        selectedOrderIndex = selectedOrderIndex.coerceIn(0, (orderCount - 1).coerceAtLeast(0)),
    )
}
