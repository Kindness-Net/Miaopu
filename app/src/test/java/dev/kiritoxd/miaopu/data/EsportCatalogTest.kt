package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
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
}
