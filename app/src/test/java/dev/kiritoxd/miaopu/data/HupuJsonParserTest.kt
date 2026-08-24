package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HupuJsonParserTest {
    @Test
    fun `schedule parser maps match and rating key`() {
        val schedule = HupuJsonParser.schedule(SCHEDULE_JSON, Esport.LOL)

        assertEquals("m1", schedule.anchorMatchId)
        assertEquals(1, schedule.days.size)
        val match = schedule.days.single().matches.single()
        assertEquals("lol_match", match.outBizType)
        assertEquals("3677", match.outBizNo)
        assertEquals(listOf("LGD", "WE"), match.teams.map { it.name })
        assertTrue(match.teams.last().winner)
        assertEquals("Karis", match.featuredPlayer?.name)
        assertEquals("COMPLETED", match.statusCode)
        assertEquals("against", match.matchType)
        assertEquals("lol_item", match.featuredPlayer?.outBizType)
        assertEquals("71707", match.featuredPlayer?.outBizNo)
    }

    @Test
    fun `non against schedule drops empty member placeholders`() {
        val schedule = HupuJsonParser.schedule(NON_AGAINST_SCHEDULE_JSON, Esport.PUBG)

        val match = schedule.days.single().matches.single()
        assertEquals("not_against", match.matchType)
        assertTrue(match.teams.isEmpty())
        assertEquals("19小组赛", match.name)
    }

    @Test
    fun `rating parser flattens scoreable sub nodes by stage`() {
        val detail = HupuJsonParser.ratingDetail(RATING_JSON)

        assertEquals("LGD 0-2 WE", detail.title)
        assertEquals("第2局", detail.stages.single().name)
        val target = detail.stages.single().targets.single()
        assertEquals("Karis", target.name)
        assertEquals(9.9, target.scoreAverage, 0.001)
        assertEquals("可惜差一个兵", target.hotComment)
        assertEquals("第2局", target.stageName)
        assertEquals(listOf("关键先生"), target.labels)
        assertEquals(1600, target.scoreDistribution[10])
        assertEquals(120, target.commentCount)
        assertEquals(108, target.directCommentCount)
        assertEquals(listOf("11"), target.infoAttributes["killCount"])
        assertEquals("hot-preview", target.hotCommentPreviews.single().id)
        assertEquals("lol_bo", detail.stages.single().outBizType)
        assertEquals("6581", detail.stages.single().outBizNo)
        assertEquals(19, detail.stages.single().targetCount)
    }

    @Test
    fun `single stage parser reads every direct rating target`() {
        val detail = HupuJsonParser.stageRatingDetail(STAGE_RATING_JSON)

        assertEquals("核子危机", detail.title)
        assertEquals("EWC CS2单淘汰赛:FUT vs Spirit", detail.description)
        assertEquals(listOf("donk", "Team Spirit"), detail.targets.map { it.name })
        assertEquals(18751031L, detail.targets.first().nodeId)
    }

    @Test
    fun `official stage groups preserve roots and parse grouped targets`() {
        val groups = HupuJsonParser.ratingGroups(STAGE_GROUPS_JSON)
        val targets = HupuJsonParser.ratingGroupTargets(STAGE_GROUP_TARGETS_JSON, "核子危机")

        assertEquals(listOf("Spirit", "趣评"), groups.map { it.name })
        assertEquals(listOf(18751029L, 18753598L), groups.map { it.rootNodeId })
        assertEquals("https://example.com/spirit.png", groups.first().logoUrl)
        assertEquals("team-spirit", groups.first().teamId)
        assertEquals(5, groups.first().childCount)
        assertEquals(listOf("donk"), targets.map { it.name })
        assertEquals("核子危机", targets.single().stageName)
    }

    @Test
    fun `rating parser groups flat bo1 players into one full match stage`() {
        val detail = HupuJsonParser.ratingDetail(BO1_RATING_JSON)

        assertEquals("EWC CS2B组", detail.title)
        assertEquals(1, detail.stages.size)
        assertEquals("全场", detail.stages.single().name)
        assertEquals(listOf("jabbi", "HooXi"), detail.stages.single().targets.map { it.name })
        assertTrue(detail.stages.single().targets.all { it.stageName == "全场" })
    }

    @Test
    fun `comment parser preserves cursor and strips markup`() {
        val page = HupuJsonParser.comments(COMMENTS_JSON)

        assertEquals(108, page.totalCount)
        assertTrue(page.hasMore)
        assertEquals(1234L, page.nextPublishTime)
        assertEquals("第一行\n第二行", page.comments.single().content)
        assertEquals(10, page.comments.single().score)
        assertEquals(listOf("https://example.com/image.jpg"), page.comments.single().imageUrls)
        assertEquals(listOf("IMAGE", "AUDIO"), page.comments.single().media.map { it.type })
        assertEquals("98693416", page.comments.single().authorId)
        assertEquals("卧龙凤雏", page.comments.single().badge?.name)
        assertTrue(page.comments.single().hasLight)
        assertEquals(5, page.comments.single().replyCount)
        val preview = page.comments.single().previewReplies.single()
        assertEquals("回复预览", preview.content)
        assertEquals("c1", preview.parentCommentId)
        assertEquals("JR", preview.parentAuthor)
        assertEquals("98693416", preview.parentAuthorId)
        assertEquals("第一行\n第二行", preview.parentContent)
        assertEquals(true, preview.parentCanSee)
    }

    @Test
    fun `hottest comment parser reads official array response`() {
        val comments = HupuJsonParser.hottestComments(HOTTEST_COMMENTS_JSON)

        assertEquals(listOf(99, 42), comments.map { it.lightCount })
        assertEquals(listOf("hot-1", "hot-2"), comments.map { it.id })
        assertEquals(null, comments.first().parentCommentId)
    }

    @Test
    fun `expanded reply page preserves cursor count and likes`() {
        val page = HupuJsonParser.comments(EXPANDED_REPLIES_JSON)

        assertEquals(2, page.totalCount)
        assertEquals(1787488188284L, page.nextPublishTime)
        assertEquals(false, page.hasMore)
        assertEquals(listOf("reply-1", "reply-2"), page.comments.map { it.id })
        assertEquals(listOf(4, 0), page.comments.map { it.lightCount })
        assertEquals(listOf("root-1", "reply-1"), page.comments.map { it.parentCommentId })
        assertEquals(null, page.comments.first().nestedReplyTarget("root-1"))
        assertEquals("A", page.comments.last().nestedReplyTarget("root-1"))
    }

    @Test
    fun `comment feed keeps official hottest first and preserves loaded order`() {
        val regular = HupuJsonParser.comments(COMMENTS_JSON).comments
        val hottest = HupuJsonParser.hottestComments(HOTTEST_COMMENTS_JSON)

        assertEquals(
            listOf("hot-1", "hot-2", "c1"),
            mergeCommentsByHeat(
                hottest + hottest.first(),
                regular,
                officialHotOrder = listOf("hot-1", "hot-2"),
            ).map { it.id },
        )
    }

    @Test
    fun `later high-like comment does not reorder the visible feed`() {
        val original = comment(id = "first", likes = 1)
        val incoming = comment(id = "later", likes = 999)

        assertEquals(
            listOf("first", "later"),
            mergeCommentsByHeat(listOf(original), listOf(incoming)).map { it.id },
        )
    }

    private fun comment(id: String, likes: Int) = HupuComment(
        id = id,
        subjectId = id,
        author = "JR",
        avatarUrl = null,
        content = id,
        date = "",
        location = null,
        score = 0,
        lightCount = likes,
    )

    private companion object {
        val SCHEDULE_JSON = """
            {
              "result": {
                "anchorMatchId": "m1",
                "dayGameData": [{
                  "dayTime": "2026-07-25",
                  "dateBlock": "7月25日 周六",
                  "matchData": [{
                    "matchId": "m1",
                    "matchName": "组内赛",
                    "matchIntroduction": "LPL第三赛段",
                    "matchType": "against",
                    "matchStatus": "COMPLETED",
                    "matchStatusDesc": "已结束",
                    "matchStartTimeStamp": "1784962800000",
                    "scoreCountText": "1.7万人评分",
                    "againstInfo": {
                      "winnerMemberId": "12",
                      "memberInfos": [
                        {"memberId":"4","memberName":"LGD","memberBaseScore":"0"},
                        {"memberId":"12","memberName":"WE","memberBaseScore":"2"}
                      ]
                    },
                    "scoreItemKey": {"outBizType":"lol_match","outBizNo":"3677"},
                    "scoreItemInfo": {
                      "name":"Karis","scoreNum":"9.9","hotComment":"精彩",
                      "scoreCountText":"1687人评分","scoreOutBizType":"lol_item","scoreOutBizNo":"71707"
                    }
                  }]
                }]
              }
            }
        """.trimIndent()

        val RATING_JSON = """
            {
              "data": {
                "self": {"node":{"name":"LGD 0-2 WE"}},
                "pageResult": {"data":[{
                  "node":{"bizType":"lol_bo","bizId":"6581","name":"第2局","canScore":false},
                  "subNodeCount":19,
                  "nodeId":99,
                  "subNodes":[{"node":{
                    "bizType":"lol_item","bizId":"71707","name":"Karis",
                    "image":["https://example.com/k.png"],
                    "infoJson":{"desc":["K/D/A:11/3/10"],"label":[{"text":"关键先生"}],"killCount":[11]},
                    "scoreAvg":9.9,"scorePersonCount":1687,"commentCount":108,"summedCommentCount":120,
                    "scoreDistribution":{"2":10,"4":12,"6":20,"8":45,"10":1600},
                    "canScore":true,"canComment":true,"hottestComments":["可惜差一个兵"],
                    "hotCommentModels":[{"commentId":"hot-preview","commentUserName":"热评用户","commentContent":"可惜差一个兵"}]
                  }}]
                }]}
              }
            }
        """.trimIndent()

        val COMMENTS_JSON = """
            {
              "data": {
                "commentCount":108,
                "hasMore":true,
                "cursor":{"publishTime":1234},
                "comments":[{
                  "commentId":"c1","subjectId":"s1","commentUserId":98693416,"commentUserName":"JR",
                  "commentContent":"第一行<br/>第二行<b></b>","commentDate":"07-26",
                  "ipLocation":"北京","score":10,"lightCount":8,"hasLight":true,"descendantCount":5,
                  "commentUserTakeBadge":{"badgeId":12984,"name":"卧龙凤雏","badgeIcon":"https://example.com/badge.gif"},
                  "commentContentImages":[
                    {"commentContentId":"image-1","commentContent":"https://example.com/image.jpg","commentContentType":"IMAGE"},
                    {"commentContentId":"audio-1","commentContent":"https://example.com/audio.aac","commentContentType":"AUDIO","durationInSec":8,"audioConvertToText":"语音文本"}
                  ],
                  "subCommentList":[{
                    "commentId":"r1","commentUserName":"回复者","commentContent":"回复预览",
                    "parentCommentId":"c1","parentCommentUserId":98693416,"parentCommentUserName":"JR",
                    "parentCommentContent":"第一行<br/>第二行","parentCommentCanSee":true
                  }]
                }]
              }
            }
        """.trimIndent()

        val BO1_RATING_JSON = """
            {
              "data": {
                "self": {"node":{"name":"EWC CS2B组"}},
                "pageResult": {"data":[
                  {"node":{
                    "bizType":"common_sports_second","bizId":"30273-1","name":"jabbi",
                    "scoreAvg":2.8,"scorePersonCount":48,"canScore":true,"canComment":true
                  }},
                  {"node":{
                    "bizType":"common_sports_second","bizId":"30273-2","name":"HooXi",
                    "scoreAvg":8.8,"scorePersonCount":20,"canScore":true,"canComment":true
                  }}
                ]}
              }
            }
        """.trimIndent()

        val HOTTEST_COMMENTS_JSON = """
            {
              "code":1,
              "data":[
                {"commentId":"hot-1","parentCommentId":"0","commentUserName":"A","commentContent":"最亮","lightCount":99},
                {"commentId":"hot-2","commentUserName":"B","commentContent":"次亮","lightCount":42}
              ]
            }
        """.trimIndent()

        val EXPANDED_REPLIES_JSON = """
            {
              "code":1,
              "data":{
                "commentCount":2,
                "hasMore":false,
                "cursor":{"publishTime":1787488188284,"limit":20},
                "comments":[
                  {
                    "commentId":"reply-1","parentCommentId":"root-1","parentCommentUserName":"主评论者",
                    "commentUserName":"A","commentContent":"第一条子回复","lightCount":4
                  },
                  {
                    "commentId":"reply-2","parentCommentId":"reply-1","parentCommentUserName":"A",
                    "commentUserName":"B","commentContent":"第二条子回复","lightCount":0
                  }
                ]
              }
            }
        """.trimIndent()

        val STAGE_RATING_JSON = """
            {
              "data": {
                "self": {"node":{
                  "name":"核子危机","image":["https://example.com/map.png"],
                  "infoJson":{"desc":["EWC CS2单淘汰赛:FUT vs Spirit"]}
                }},
                "pageResult":{"totalCount":6,"data":[
                  {"nodeId":18751031,"node":{"bizType":"common_sports_third","bizId":"65567","name":"donk","infoJson":{"type":["player"]},"scoreAvg":9.8,"scorePersonCount":6447,"canScore":true}},
                  {"nodeId":18753599,"node":{"bizType":"common_sports_third","bizId":"65620","name":"Team Spirit","scoreAvg":8.7,"scorePersonCount":405,"canScore":true}},
                  {"node":{"bizType":"common_sports_third","bizId":"hidden","name":"隐藏","scorePersonCount":1,"canScore":true,"visible":false}},
                  {"node":{"bizType":"common_sports_third","bizId":"deleted","name":"删除","scorePersonCount":1,"canScore":true,"del":1}},
                  {"node":{"bizType":"common_sports_third","bizId":"rejected","name":"驳回","scorePersonCount":1,"canScore":true,"finalStatus":"REJECT"}},
                  {"node":{"bizType":"common_sports_third","bizId":"no-score","name":"不展示评分","scorePersonCount":1,"canScore":true,"showScore":false}}
                ]}
              }
            }
        """.trimIndent()

        val STAGE_GROUPS_JSON = """
            {"code":1,"data":[
              {"groupId":216860,"sort":2,"groupName":"Spirit","rootNodeId":18751029,"childCount":5,
               "groupAttributes":[
                 {"attributeKey":"logo","attributeValue":"https://example.com/spirit.png"},
                 {"attributeKey":"teamId","attributeValue":"team-spirit"}
               ]},
              {"groupId":216915,"sort":3,"groupName":"趣评","rootNodeId":18753598,"childCount":9,
               "groupAttributes":[{"attributeKey":"logo","attributeValue":""}]}
            ]}
        """.trimIndent()

        val NON_AGAINST_SCHEDULE_JSON = """
            {
              "result": {
                "anchorMatchId": "pubg-1",
                "dayGameData": [{
                  "dayTime": "2026-08-24",
                  "dateBlock": "8月24日 周一",
                  "matchData": [{
                    "matchId": "pubg-1",
                    "matchName": "19小组赛",
                    "matchType": "not_against",
                    "matchStatus": "COMPLETED",
                    "matchStatusDesc": "已结束",
                    "againstInfo": {"memberInfos": [
                      {"memberId":null,"memberName":null,"memberLogo":null,"memberBaseScore":null},
                      {"memberId":null,"memberName":null,"memberLogo":null,"memberBaseScore":null}
                    ]}
                  }]
                }]
              }
            }
        """.trimIndent()

        val STAGE_GROUP_TARGETS_JSON = """
            {"code":1,"data":{"nodePageResult":{"totalCount":1,"data":[
              {"nodeId":18751031,"node":{"bizType":"common_sports_third","bizId":"65567","name":"donk","scoreAvg":9.8,"scorePersonCount":6447,"canScore":true}}
            ]}}}
        """.trimIndent()
    }
}
