package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SportsScheduleParserTest {
    @Test
    fun commonSportsMatchPreservesIdentityAndPlayerScores() {
        val schedule = HupuJsonParser.schedule(BADMINTON_SCHEDULE_JSON, Esport.BADMINTON)

        val match = schedule.days.single().matches.single()
        assertEquals("1441778164564971052", match.id)
        assertEquals("common_match:1441778164564971052", match.uniqueKey)
        assertEquals("羽毛球世锦赛男双决赛", match.introduction)
        assertEquals(listOf("谢定峰/苏伟译", "梁伟铿/王昶"), match.teams.map { it.name })
        assertEquals(listOf("0", "2"), match.teams.map { it.score })
        assertTrue(match.teams.last().winner)
        assertEquals("common_sports_first", match.outBizType)
    }

    @Test
    fun matchIdFallsBackToUniqueKeyWhenHupuOmitsTheId() {
        val schedule = HupuJsonParser.schedule(MISSING_MATCH_ID_JSON, Esport.TENNIS)

        val match = schedule.days.single().matches.single()
        assertEquals("common_match:fallback", match.id)
        assertEquals("common_match:fallback", match.uniqueKey)
    }

    private companion object {
        val BADMINTON_SCHEDULE_JSON = """
            {
              "result": {
                "anchorMatchId": "1441778164564971052",
                "dayGameData": [{
                  "dayTime": "2026-08-23",
                  "dateBlock": "8月23日 周日",
                  "matchData": [{
                    "businessType": "common_match",
                    "matchId": "1441778164564971052",
                    "uniqueKey": "common_match:1441778164564971052",
                    "matchIntroduction": "羽毛球世锦赛男双决赛",
                    "matchName": "男双决赛",
                    "matchDesc": "梁伟铿/王昶夺冠",
                    "matchType": "against",
                    "matchStatus": "COMPLETED",
                    "matchStatusDesc": "已结束",
                    "matchStartTimeStamp": "1787486400000",
                    "againstInfo": {
                      "winnerMemberId": "away",
                      "memberInfos": [
                        {"memberId":"home","memberName":"谢定峰/苏伟译","memberBaseScore":"0"},
                        {"memberId":"away","memberName":"梁伟铿/王昶","memberBaseScore":"2"}
                      ]
                    },
                    "scoreItemKey": {
                      "outBizType": "common_sports_first",
                      "outBizNo": "30999"
                    }
                  }]
                }]
              }
            }
        """.trimIndent()

        val MISSING_MATCH_ID_JSON = """
            {
              "result": {
                "dayGameData": [{
                  "dayTime": "2026-08-25",
                  "dateBlock": "8月25日 周二",
                  "matchData": [{
                    "businessType": "common_match",
                    "uniqueKey": "common_match:fallback",
                    "matchIntroduction": "美国网球公开赛男单",
                    "matchName": "男单",
                    "matchStatus": "NOTSTARTED",
                    "matchStatusDesc": "未开始",
                    "matchStartTimeStamp": "1787616000000",
                    "againstInfo": {"memberInfos": []}
                  }]
                }]
              }
            }
        """.trimIndent()
    }
}
