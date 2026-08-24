package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ScheduleFocusTest {
    @Test
    fun `home keeps every match from two days before through two days after`() {
        val schedule = scheduleOf(
            day("2026-08-21", match("too-old", "已结束", time(21, 10))),
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day(
                "2026-08-24",
                match("ended", "已结束", time(24, 10)),
                match("live", "进行中", time(24, 15)),
                match("later", "未开始", time(24, 20)),
            ),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 15))),
            day("2026-08-26", match("two-days-ahead", "未开始", time(26, 15))),
            day("2026-08-27", match("too-late", "未开始", time(27, 15))),
        )

        val home = schedule.homeWindowAround(nowMillis = time(24, 16))

        assertEquals("live", home.anchorMatchId)
        assertEquals(
            listOf("2026-08-22", "2026-08-23", "2026-08-24", "2026-08-25", "2026-08-26"),
            home.days.map { it.date },
        )
        assertEquals(
            listOf("two-days-ago", "yesterday", "ended", "live", "later", "tomorrow", "two-days-ahead"),
            home.days.flatMap { it.matches }.map { it.id },
        )
    }

    @Test
    fun `home chooses upcoming day without dropping earlier matches on that day`() {
        val schedule = scheduleOf(
            day("2026-08-23", match("ended", "已结束", time(23, 10))),
            day("2026-08-24", match("earlier", "待定", time(24, 10)), match("next", "未开始", time(24, 18))),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        val home = schedule.homeWindowAround(nowMillis = time(24, 16))

        assertEquals("next", home.anchorMatchId)
        assertEquals(listOf("ended", "earlier", "next", "tomorrow"), home.days.flatMap { it.matches }.map { it.id })
    }

    @Test
    fun `home excludes event days outside the five day date window`() {
        val schedule = scheduleOf(
            day("2026-08-20", match("outside-before", "已结束", time(20, 10))),
            day("2026-08-22", match("inside-before", "已结束", time(22, 10))),
            day("2026-08-26", match("inside-after", "未开始", time(26, 10))),
            day("2026-08-28", match("outside-after", "未开始", time(28, 10))),
        )

        val home = schedule.homeWindowAround(nowMillis = time(24, 16))

        assertEquals(listOf("2026-08-22", "2026-08-26"), home.days.map { it.date })
        assertEquals(listOf("inside-before", "inside-after"), home.days.flatMap { it.matches }.map { it.id })
    }

    @Test
    fun `home initial list item is today's day header with history retained above`() {
        val schedule = scheduleOf(
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day(
                "2026-08-24",
                match("earlier", "已结束", time(24, 10)),
                match("later", "未开始", time(24, 18)),
            ),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        assertEquals(4, schedule.homeInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item uses yesterday when today has no matches`() {
        val schedule = scheduleOf(
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        assertEquals(2, schedule.homeInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item uses the latest available past day`() {
        val schedule = scheduleOf(
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        assertEquals(0, schedule.homeInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item falls forward only when no past day exists`() {
        val schedule = scheduleOf(
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
            day("2026-08-26", match("two-days-ahead", "未开始", time(26, 18))),
        )

        assertEquals(0, schedule.homeInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `full schedule focus still finds next match outside home window`() {
        val schedule = scheduleOf(
            day("2026-08-23", match("ended", "已结束", time(23, 10))),
            day("2026-08-30", match("next-week", "未开始", time(30, 18))),
        )

        assertEquals("next-week", schedule.focusMatchId(time(24, 16)))
    }

    @Test
    fun `full schedule initial day prefers today over a future focus match`() {
        val schedule = scheduleOf(
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day("2026-08-24", match("today-ended", "已结束", time(24, 10))),
            day("2026-08-25", match("next", "未开始", time(25, 18))),
        )

        assertEquals(1, schedule.fullScheduleInitialDayIndex(time(24, 16)))
    }

    @Test
    fun `full schedule initial day falls back to next match when today is absent`() {
        val schedule = scheduleOf(
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day("2026-08-25", match("next", "未开始", time(25, 18))),
        )

        assertEquals(1, schedule.fullScheduleInitialDayIndex(time(24, 16)))
    }

    private fun scheduleOf(vararg days: ScheduleDay) = Schedule(
        anchorMatchId = null,
        days = days.toList(),
    )

    private fun day(date: String, vararg matches: MatchSummary) = ScheduleDay(
        date = date,
        label = date,
        matches = matches.toList(),
    )

    private fun match(id: String, status: String, start: Long) = MatchSummary(
        id = id,
        esport = Esport.LOL,
        name = id,
        introduction = "",
        status = status,
        startTimeMillis = start,
        startTimeLabel = "",
        teams = emptyList(),
        scoreCountText = null,
        outBizType = "type",
        outBizNo = id,
        featuredPlayer = null,
    )

    private fun time(dayOfMonth: Int, hour: Int): Long = Calendar.getInstance().run {
        set(2026, Calendar.AUGUST, dayOfMonth, hour, 0, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }
}
