package dev.kiritoxd.miaopu.ui

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppScreenTest {
    @Test
    fun `stage screen survives serialization round trip`() {
        val screen: AppScreen = AppScreen.Stage(
            match = matchRoute(),
            stage = StageRoute(
                name = "决赛",
                outBizType = "match",
                outBizNo = "stage-7",
                nodeId = 7L,
                targetCount = 12,
            ),
            stageNumber = 3,
            returnToStagePicker = true,
        )

        val encoded = Json.encodeToString(screen)
        val restored = Json.decodeFromString<AppScreen>(encoded)

        assertEquals(screen, restored)
        assertEquals(screen.navigationContentKey, restored.navigationContentKey)
    }

    @Test
    fun `stage route keeps identity without embedding target collections`() {
        val route = StageRoute(
            name = "第一局",
            outBizType = "game",
            outBizNo = "game-1",
            nodeId = 42L,
            targetCount = 20,
        )

        val restored = route.toModel()

        assertTrue(restored.targets.isEmpty())
        assertEquals(route.name, restored.name)
        assertEquals(route.outBizType, restored.outBizType)
        assertEquals(route.outBizNo, restored.outBizNo)
        assertEquals(route.nodeId, restored.nodeId)
        assertEquals(route.targetCount, restored.targetCount)
    }

    private fun matchRoute() = MatchRoute(
        id = "match-1",
        esportBusinessId = "lol",
        name = "蓝队 vs 红队",
        introduction = "决赛",
        status = "进行中",
        startTimeMillis = 1_700_000_000_000,
        startTimeLabel = "20:00",
        teams = emptyList(),
        scoreCountText = null,
        outBizType = "match",
        outBizNo = "match-1",
        matchType = null,
        statusCode = null,
        liveRoomLink = null,
    )
}
