package dev.kiritoxd.miaopu.ui

import dev.kiritoxd.miaopu.data.Esport
import dev.kiritoxd.miaopu.data.MatchSummary
import dev.kiritoxd.miaopu.data.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatingsScreenTest {
    @Test
    fun overallScorePrefersCompleteBigScorePair() {
        val match = matchWithTeams(
            team("home", score = "13", bigScore = "2"),
            team("away", score = "8", bigScore = "0"),
        )

        assertEquals("2 : 0", overallMatchScore(match))
    }

    @Test
    fun overallScoreFallsBackToCompleteBaseScorePair() {
        val match = matchWithTeams(
            team("home", score = "13", bigScore = "2"),
            team("away", score = "8", bigScore = null),
        )

        assertEquals("13 : 8", overallMatchScore(match))
    }

    @Test
    fun overallScoreRequiresTwoCompleteScores() {
        assertNull(overallMatchScore(matchWithTeams(team("home", "2"), team("away", null))))
        assertNull(overallMatchScore(matchWithTeams(team("only", "2"))))
    }

    private fun matchWithTeams(vararg teams: Team) = MatchSummary(
        id = "match",
        esport = Esport.CS2,
        name = "比赛",
        introduction = "赛事",
        status = "已结束",
        startTimeMillis = 0,
        startTimeLabel = "20:00",
        teams = teams.toList(),
        scoreCountText = null,
        outBizType = null,
        outBizNo = null,
        featuredPlayer = null,
    )

    private fun team(name: String, score: String?, bigScore: String? = null) = Team(
        id = name,
        name = name,
        logoUrl = null,
        score = score,
        winner = false,
        bigScore = bigScore,
    )
}
