package dev.kiritoxd.miaopu.ui

import dev.kiritoxd.miaopu.data.Esport
import dev.kiritoxd.miaopu.data.EsportCatalog
import dev.kiritoxd.miaopu.data.MatchSummary
import dev.kiritoxd.miaopu.data.RatingStage
import dev.kiritoxd.miaopu.data.RatingTarget
import dev.kiritoxd.miaopu.data.Team
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
internal sealed interface AppScreen : NavKey {
    @Serializable
    data object Schedule : AppScreen

    @Serializable
    data object Subscriptions : AppScreen

    @Serializable
    data class Ratings(val match: MatchRoute) : AppScreen

    @Serializable
    data class Stage(
        val match: MatchRoute,
        val stage: StageRoute,
        val stageNumber: Int,
        val returnToStagePicker: Boolean,
    ) : AppScreen

    @Serializable
    data class Comments(val target: RatingTargetRoute) : AppScreen

    @Serializable
    data class Web(
        val title: String,
        val url: String,
        val login: Boolean,
    ) : AppScreen
}

@Serializable
internal data class TeamRoute(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val score: String?,
    val winner: Boolean,
    val extraScore: String?,
    val bigScore: String?,
    val type: String?,
    val description: String?,
)

@Serializable
internal data class MatchRoute(
    val id: String,
    val esportBusinessId: String,
    val name: String,
    val introduction: String,
    val status: String,
    val startTimeMillis: Long,
    val startTimeLabel: String,
    val teams: List<TeamRoute>,
    val scoreCountText: String?,
    val outBizType: String?,
    val outBizNo: String?,
    val matchType: String?,
    val statusCode: String?,
    val liveRoomLink: String?,
)

@Serializable
internal data class StageRoute(
    val name: String,
    val outBizType: String?,
    val outBizNo: String?,
    val nodeId: Long?,
    val targetCount: Int,
)

@Serializable
internal data class RatingTargetRoute(
    val nodeId: Long?,
    val outBizType: String,
    val outBizNo: String,
    val name: String,
    val description: String?,
    val stageName: String?,
    val labels: List<String>,
    val imageUrl: String?,
    val scoreAverage: Double,
    val scoreCount: Int,
    val commentCount: Int,
    val userScore: Int,
    val canScore: Boolean,
    val canComment: Boolean,
    val hotComment: String?,
    val scoreDistribution: Map<Int, Int>,
    val category: String?,
    val directCommentCount: Int,
    val showScore: Boolean,
    val visible: Boolean,
    val deleted: Boolean,
    val finalStatus: String?,
    val championImageUrl: String? = null,
) {
    val key: String get() = "$outBizType:$outBizNo"
}

internal fun MatchSummary.toRoute(): MatchRoute = MatchRoute(
    id = id,
    esportBusinessId = esport.businessId,
    name = name,
    introduction = introduction,
    status = status,
    startTimeMillis = startTimeMillis,
    startTimeLabel = startTimeLabel,
    teams = teams.map(Team::toRoute),
    scoreCountText = scoreCountText,
    outBizType = outBizType,
    outBizNo = outBizNo,
    matchType = matchType,
    statusCode = statusCode,
    liveRoomLink = liveRoomLink,
)

internal fun MatchRoute.toModel(): MatchSummary = MatchSummary(
    id = id,
    esport = EsportCatalog.byBusinessId(esportBusinessId) ?: Esport.LOL,
    name = name,
    introduction = introduction,
    status = status,
    startTimeMillis = startTimeMillis,
    startTimeLabel = startTimeLabel,
    teams = teams.map(TeamRoute::toModel),
    scoreCountText = scoreCountText,
    outBizType = outBizType,
    outBizNo = outBizNo,
    featuredPlayer = null,
    matchType = matchType,
    statusCode = statusCode,
    liveRoomLink = liveRoomLink,
)

internal fun RatingStage.toRoute(): StageRoute = StageRoute(
    name = name,
    outBizType = outBizType,
    outBizNo = outBizNo,
    nodeId = nodeId,
    targetCount = targetCount,
)

internal fun StageRoute.toModel(): RatingStage = RatingStage(
    name = name,
    targets = emptyList(),
    outBizType = outBizType,
    outBizNo = outBizNo,
    nodeId = nodeId,
    targetCount = targetCount,
)

internal fun RatingTarget.toRoute(): RatingTargetRoute = RatingTargetRoute(
    nodeId = nodeId,
    outBizType = outBizType,
    outBizNo = outBizNo,
    name = name,
    description = description,
    stageName = stageName,
    labels = labels,
    imageUrl = imageUrl,
    scoreAverage = scoreAverage,
    scoreCount = scoreCount,
    commentCount = commentCount,
    userScore = userScore,
    canScore = canScore,
    canComment = canComment,
    hotComment = hotComment,
    scoreDistribution = scoreDistribution,
    category = category,
    directCommentCount = directCommentCount,
    showScore = showScore,
    visible = visible,
    deleted = deleted,
    finalStatus = finalStatus,
    championImageUrl = championImageUrl,
)

internal fun RatingTargetRoute.toModel(): RatingTarget = RatingTarget(
    nodeId = nodeId,
    outBizType = outBizType,
    outBizNo = outBizNo,
    name = name,
    description = description,
    stageName = stageName,
    labels = labels,
    imageUrl = imageUrl,
    scoreAverage = scoreAverage,
    scoreCount = scoreCount,
    commentCount = commentCount,
    userScore = userScore,
    canScore = canScore,
    canComment = canComment,
    hotComment = hotComment,
    scoreDistribution = scoreDistribution,
    category = category,
    directCommentCount = directCommentCount,
    showScore = showScore,
    visible = visible,
    deleted = deleted,
    finalStatus = finalStatus,
    championImageUrl = championImageUrl,
)

private fun Team.toRoute(): TeamRoute = TeamRoute(
    id = id,
    name = name,
    logoUrl = logoUrl,
    score = score,
    winner = winner,
    extraScore = extraScore,
    bigScore = bigScore,
    type = type,
    description = description,
)

private fun TeamRoute.toModel(): Team = Team(
    id = id,
    name = name,
    logoUrl = logoUrl,
    score = score,
    winner = winner,
    extraScore = extraScore,
    bigScore = bigScore,
    type = type,
    description = description,
)
