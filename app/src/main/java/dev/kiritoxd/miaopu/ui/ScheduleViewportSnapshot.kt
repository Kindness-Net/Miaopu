package dev.kiritoxd.miaopu.ui

internal data class ScheduleViewportSnapshot(
    val listIndex: Int,
    val listOffset: Int,
    val dateStripIndex: Int,
    val dateStripOffset: Int,
    val selectedDayKey: String,
)
