package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ScheduleFocusTest {
    @Test
    fun `merges esports by date and match time`() {
        val lol = scheduleOf(
            day(
                "2026-08-24",
                match("lol-late", "未开始", time(24, 20), Esport.LOL),
                match("lol-early", "已结束", time(24, 10), Esport.LOL),
            ),
        )
        val kog = scheduleOf(
            day(
                "2026-08-24",
                match("kog-middle", "未开始", time(24, 16), Esport.KOG),
            ),
            day("2026-08-25", match("kog-next", "未开始", time(25, 18), Esport.KOG)),
        )

        val merged = mergeSchedules(listOf(lol, kog))

        assertEquals(listOf("2026-08-24", "2026-08-25"), merged.days.map { it.date })
        assertEquals(
            listOf("lol-early", "kog-middle", "lol-late", "kog-next"),
            merged.days.flatMap { it.matches }.map { it.id },
        )
        assertEquals(
            listOf(Esport.LOL, Esport.KOG, Esport.LOL, Esport.KOG),
            merged.days.flatMap { it.matches }.map { it.esport },
        )
    }

    @Test
    fun `keeps same match ids from different esports`() {
        val merged = mergeSchedules(
            listOf(
                scheduleOf(day("2026-08-24", match("shared", "未开始", time(24, 10), Esport.LOL))),
                scheduleOf(day("2026-08-24", match("shared", "未开始", time(24, 11), Esport.KOG))),
            ),
        )

        assertEquals(2, merged.days.single().matches.size)
    }

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
    fun `home initial list item keeps the two prior matches above the next match`() {
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

        assertEquals(3, schedule.focusInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item keeps two prior matches when today has no matches`() {
        val schedule = scheduleOf(
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-23", match("yesterday", "已结束", time(23, 10))),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        assertEquals(1, schedule.focusInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item keeps the only prior match`() {
        val schedule = scheduleOf(
            day("2026-08-22", match("two-days-ago", "已结束", time(22, 10))),
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
        )

        assertEquals(1, schedule.focusInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item falls forward only when no past day exists`() {
        val schedule = scheduleOf(
            day("2026-08-25", match("tomorrow", "未开始", time(25, 18))),
            day("2026-08-26", match("two-days-ahead", "未开始", time(26, 18))),
        )

        assertEquals(0, schedule.focusInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `home initial list item prioritizes a live match over an upcoming match`() {
        val schedule = scheduleOf(
            day(
                "2026-08-24",
                match("oldest", "已结束", time(24, 10)),
                match("recent", "已结束", time(24, 12)),
                match("live", "进行中", time(24, 14)),
                match("upcoming", "未开始", time(24, 18)),
            ),
        )

        assertEquals(1, schedule.focusInitialItemIndex(time(24, 16)))
    }

    @Test
    fun `focus uses the earliest upcoming match even when source order differs`() {
        val schedule = scheduleOf(
            day(
                "2026-08-24",
                match("later", "未开始", time(24, 20)),
                match("next", "未开始", time(24, 18)),
            ),
        )

        assertEquals("next", schedule.focusMatchId(time(24, 16)))
    }

    @Test
    fun `focus skips a cancelled future match`() {
        val schedule = scheduleOf(
            day(
                "2026-08-24",
                match("cancelled", "已取消", time(24, 17), statusCode = "CANCELLED"),
                match("next", "未开始", time(24, 18), statusCode = "NOTSTARTED"),
            ),
        )

        assertEquals("next", schedule.focusMatchId(time(24, 16)))
    }

    @Test
    fun `focus recognizes hupu in progress status code`() {
        val schedule = scheduleOf(
            day(
                "2026-08-24",
                match("live", "比赛中", time(24, 15), statusCode = "INPROGRESS"),
                match("next", "未开始", time(24, 18), statusCode = "NOTSTARTED"),
            ),
        )

        assertEquals("live", schedule.focusMatchId(time(24, 16)))
    }

    @Test
    fun `merge deduplicates the same hupu unique key across sources`() {
        val primary = match("primary-id", "已结束", time(24, 10), uniqueKey = "common_match:42")
        val supplement = match("supplement-id", "已结束", time(24, 10), uniqueKey = "common_match:42")

        val merged = mergeSchedules(
            listOf(
                scheduleOf(day("2026-08-24", primary)),
                scheduleOf(day("2026-08-24", supplement)),
            ),
        )

        assertEquals(listOf("primary-id"), merged.days.single().matches.map { it.id })
    }

    @Test
    fun `home initial list item can point directly at focus without prior slots`() {
        val schedule = scheduleOf(
            day(
                "2026-08-24",
                match("ended", "已结束", time(24, 10)),
                match("next", "未开始", time(24, 18)),
            ),
        )

        assertEquals(2, schedule.focusInitialItemIndex(time(24, 16), previousMatchCount = 0))
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

    private fun match(
        id: String,
        status: String,
        start: Long,
        esport: Esport = Esport.LOL,
        statusCode: String? = null,
        uniqueKey: String = id,
    ) = MatchSummary(
        id = id,
        esport = esport,
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
        statusCode = statusCode,
        uniqueKey = uniqueKey,
    )

    private fun time(dayOfMonth: Int, hour: Int): Long = Calendar.getInstance().run {
        set(2026, Calendar.AUGUST, dayOfMonth, hour, 0, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }
}
