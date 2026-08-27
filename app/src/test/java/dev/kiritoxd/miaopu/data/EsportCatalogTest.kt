package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EsportCatalogTest {
    @Test
    fun defaultSubscriptionsContainEveryVerifiedScheduleProject() {
        assertEquals(
            listOf(Esport.LOL, Esport.KOG, Esport.CS2, Esport.VALORANT, Esport.PUBG),
            EsportCatalog.subscriptions(null).toList(),
        )
    }

    @Test
    fun savedSubscriptionsIgnoreUnknownIdsAndKeepCatalogOrder() {
        assertEquals(
            listOf(Esport.CS2, Esport.PUBG),
            EsportCatalog.subscriptions(setOf("pubg", "unknown", "cs2")).toList(),
        )
    }

    @Test
    fun emptyOrUnknownSelectionFallsBackToLol() {
        assertEquals(setOf(Esport.LOL), EsportCatalog.subscriptions(emptySet()))
        assertEquals(setOf(Esport.LOL), EsportCatalog.subscriptions(setOf("unknown")))
    }

    @Test
    fun sportsProjectsAreAvailableButRemainOptIn() {
        assertEquals(
            listOf(
                Esport.TENNIS,
                Esport.TABLE_TENNIS,
                Esport.BADMINTON,
                Esport.FORMULA_ONE,
                Esport.SNOOKER,
                Esport.FOOTBALL,
                Esport.VOLLEYBALL,
            ),
            EsportCatalog.all.filter { it.category == ScheduleCategory.SPORTS },
        )
        assertTrue(EsportCatalog.subscriptions(null).all { it.category == ScheduleCategory.ESPORTS })
        assertFalse(Esport.TENNIS.defaultSubscribed)
        assertFalse(Esport.TABLE_TENNIS.defaultSubscribed)
        assertFalse(Esport.BADMINTON.defaultSubscribed)
        assertFalse(Esport.FORMULA_ONE.defaultSubscribed)
        assertFalse(Esport.SNOOKER.defaultSubscribed)
        assertFalse(Esport.FOOTBALL.defaultSubscribed)
        assertFalse(Esport.VOLLEYBALL.defaultSubscribed)
    }

    @Test
    fun everySportsProjectFiltersItsCommonScheduleSupplement() {
        val tennis = Esport.TENNIS.supplementalSources.single()
        val tableTennis = Esport.TABLE_TENNIS.supplementalSources.single()
        val badminton = Esport.BADMINTON.supplementalSources.single()

        assertTrue(tennis.includes("美国网球公开赛男单"))
        assertFalse(tennis.includes("羽毛球世锦赛男单"))
        assertTrue(tableTennis.includes("WTT瑞典大满贯女单"))
        assertFalse(tableTennis.includes("ATP辛辛那提站男单"))
        assertTrue(badminton.includes("羽毛球世锦赛男双决赛"))
        assertFalse(badminton.includes("WTT瑞典大满贯男单"))

        assertTrue(Esport.FORMULA_ONE.supplementalSources.single().includes("F1荷兰站正赛"))
        assertTrue(Esport.SNOOKER.supplementalSources.single().includes("斯诺克武汉公开赛8进4"))
        assertTrue(Esport.FOOTBALL.supplementalSources.single().includes("德国杯1/8决赛"))
        assertTrue(Esport.VOLLEYBALL.supplementalSources.single().includes("女排亚锦赛小组赛"))
    }
}
