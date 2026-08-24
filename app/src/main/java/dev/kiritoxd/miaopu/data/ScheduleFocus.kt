package dev.kiritoxd.miaopu.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Builds the home feed from the five-day local-date window centered on today.
 * Every match from two days ago through two days ahead is retained.
 */
fun Schedule.homeWindowAround(nowMillis: Long): Schedule {
    if (days.isEmpty()) return this

    val visibleDateKeys = dateKeysAround(nowMillis, daysBefore = 2, daysAfter = 2)
    val visibleDays = days.filter { it.date in visibleDateKeys }
    if (visibleDays.isEmpty()) return copy(anchorMatchId = null, days = emptyList())

    val visibleSchedule = copy(days = visibleDays)
    return copy(
        anchorMatchId = visibleSchedule.focusMatchId(nowMillis),
        days = visibleDays,
    )
}

/**
 * Returns the flattened LazyColumn index for today's day header while retaining older days above it.
 * If today has no scheduled matches, the latest past day is preferred, then the nearest future day.
 */
fun Schedule.homeInitialItemIndex(nowMillis: Long): Int {
    if (days.isEmpty()) return 0

    val todayKey = dateKey(nowMillis)
    val initialDayIndex = days.indexOfFirst { it.date == todayKey }
        .takeIf { it >= 0 }
        ?: days.indexOfLast { it.date < todayKey }.takeIf { it >= 0 }
        ?: days.indexOfFirst { it.date > todayKey }.takeIf { it >= 0 }
        ?: 0

    return days.take(initialDayIndex).sumOf { day -> 1 + day.matches.size }
}

/** Uses today's schedule day first, then the current/next match day when today has no entry. */
fun Schedule.fullScheduleInitialDayIndex(nowMillis: Long): Int {
    if (days.isEmpty()) return 0
    val todayKey = dateKey(nowMillis)
    val todayIndex = days.indexOfFirst { it.date == todayKey }
    if (todayIndex >= 0) return todayIndex

    val focusId = focusMatchId(nowMillis)
    return days.indexOfFirst { day -> day.matches.any { it.id == focusId } }
        .takeIf { it >= 0 }
        ?: days.indexOfLast { it.date < todayKey }.takeIf { it >= 0 }
        ?: days.indexOfFirst { it.date > todayKey }.takeIf { it >= 0 }
        ?: 0
}

/** Selects the live, next, API-anchored, or latest match without trimming schedule days. */
fun Schedule.focusMatchId(nowMillis: Long): String? {
    val focusMatch = days.asSequence()
        .flatMap { it.matches.asSequence() }
        .firstOrNull { it.isLive }
        ?: days.asSequence()
            .flatMap { it.matches.asSequence() }
            .firstOrNull { !it.isFinished && it.startTimeMillis >= nowMillis }
        ?: anchorMatchId?.let { anchor ->
            days.asSequence().flatMap { it.matches.asSequence() }.firstOrNull { it.id == anchor }
        }
        ?: days.asSequence().flatMap { it.matches.asSequence() }.firstOrNull { !it.isFinished }
        ?: days.last().matches.lastOrNull()
        ?: return null

    return focusMatch.id
}

private fun dateKeysAround(nowMillis: Long, daysBefore: Int, daysAfter: Int): Set<String> {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return (-daysBefore..daysAfter).mapTo(linkedSetOf()) { offset ->
        val date = today.clone() as Calendar
        date.add(Calendar.DAY_OF_YEAR, offset)
        formatter.format(date.time)
    }
}

private fun dateKey(nowMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(nowMillis)

private val MatchSummary.isLive: Boolean
    get() = statusCode in setOf("LIVE", "ONGOING", "PROCESSING") ||
        status.contains("进行") || status.contains("直播")

private val MatchSummary.isFinished: Boolean
    get() = statusCode == "COMPLETED" || status.contains("结束") || status.contains("完赛")
