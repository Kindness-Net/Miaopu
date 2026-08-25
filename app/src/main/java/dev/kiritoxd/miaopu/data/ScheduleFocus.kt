package dev.kiritoxd.miaopu.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Merges schedules by date and chronological match order while retaining each match's esport. */
fun mergeSchedules(schedules: Iterable<Schedule>): Schedule {
    val days = schedules
        .flatMap { it.days }
        .groupBy(ScheduleDay::date)
        .toSortedMap()
        .map { (date, sameDateDays) ->
            ScheduleDay(
                date = date,
                label = sameDateDays.firstNotNullOfOrNull { day -> day.label.takeIf(String::isNotBlank) }
                    ?: date,
                matches = sameDateDays
                    .flatMap(ScheduleDay::matches)
                    .distinctBy { match -> match.esport.businessId to match.id }
                    .sortedWith(
                        compareBy<MatchSummary> { match ->
                            match.startTimeMillis.takeIf { it > 0L } ?: Long.MAX_VALUE
                        }
                            .thenBy { match -> match.esport.ordinal }
                            .thenBy(MatchSummary::id),
                    ),
            )
        }

    return Schedule(anchorMatchId = null, days = days)
}

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

/** Returns the list index two matches before the live or next match so recent context remains above it. */
fun Schedule.focusInitialItemIndex(nowMillis: Long, previousMatchCount: Int = 2): Int {
    if (days.isEmpty()) return 0

    val focusMatch = focusMatch(nowMillis) ?: return 0
    val retainedPreviousCount = previousMatchCount.coerceAtLeast(0)
    val previousMatchIndices = ArrayDeque<Int>()
    var itemIndex = 0

    days.forEach { day ->
        itemIndex += 1 // Date header.
        day.matches.forEach { match ->
            if (match.id == focusMatch.id && match.esport == focusMatch.esport) {
                return if (retainedPreviousCount == 0) {
                    itemIndex
                } else {
                    previousMatchIndices.firstOrNull() ?: 0
                }
            }
            previousMatchIndices.addLast(itemIndex)
            while (previousMatchIndices.size > retainedPreviousCount) {
                previousMatchIndices.removeFirst()
            }
            itemIndex += 1
        }
    }

    return 0
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
fun Schedule.focusMatchId(nowMillis: Long): String? = focusMatch(nowMillis)?.id

private fun Schedule.focusMatch(nowMillis: Long): MatchSummary? =
    days.flatMap(ScheduleDay::matches).let { matches ->
        matches.filter(MatchSummary::isLive).minWithOrNull(matchTimeOrder)
            ?: matches
                .filter { !it.isFinished && it.startTimeMillis >= nowMillis }
                .minWithOrNull(matchTimeOrder)
        ?: anchorMatchId?.let { anchor ->
                matches.firstOrNull { it.id == anchor }
            }
            ?: matches.filterNot(MatchSummary::isFinished).minWithOrNull(matchTimeOrder)
            ?: matches.maxWithOrNull(matchTimeOrder)
        }

private val matchTimeOrder = compareBy<MatchSummary> { match ->
    match.startTimeMillis.takeIf { it > 0L } ?: Long.MAX_VALUE
}
    .thenBy { match -> match.esport.ordinal }
    .thenBy(MatchSummary::id)

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
