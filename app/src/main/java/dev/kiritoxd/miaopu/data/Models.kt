package dev.kiritoxd.miaopu.data

enum class Esport(
    val businessId: String,
    val title: String,
    val shortTitle: String,
    val defaultSubscribed: Boolean,
) {
    LOL("lol", "英雄联盟", "LOL", true),
    KOG("kog", "王者荣耀", "KPL", true),
    CS2("cs2", "反恐精英 2", "CS2", true),
    VALORANT("val", "无畏契约", "VAL", true),
    PUBG("pubg", "绝地求生", "PUBG", true),
}

object EsportCatalog {
    val all: List<Esport> = Esport.entries

    fun subscriptions(savedBusinessIds: Set<String>?): Set<Esport> {
        if (savedBusinessIds == null) return all.filterTo(linkedSetOf()) { it.defaultSubscribed }
        val resolved = all.filterTo(linkedSetOf()) { it.businessId in savedBusinessIds }
        return resolved.ifEmpty { linkedSetOf(Esport.LOL) }
    }

    fun byBusinessId(id: String?): Esport? = all.firstOrNull { it.businessId == id }
}

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val score: String?,
    val winner: Boolean,
    val extraScore: String? = null,
    val bigScore: String? = null,
    val type: String? = null,
    val description: String? = null,
)

data class MatchSummary(
    val id: String,
    val esport: Esport,
    val name: String,
    val introduction: String,
    val status: String,
    val startTimeMillis: Long,
    val startTimeLabel: String,
    val teams: List<Team>,
    val scoreCountText: String?,
    val outBizType: String?,
    val outBizNo: String?,
    val featuredPlayer: FeaturedPlayer?,
    val matchType: String? = null,
    val statusCode: String? = null,
    val liveRoomLink: String? = null,
)

data class FeaturedPlayer(
    val name: String,
    val logoUrl: String?,
    val score: String?,
    val hotComment: String?,
    val scoreCountText: String? = null,
    val outBizType: String? = null,
    val outBizNo: String? = null,
    val jumpLink: String? = null,
    val teamLogoUrl: String? = null,
)

data class ScheduleDay(
    val date: String,
    val label: String,
    val matches: List<MatchSummary>,
)

data class Schedule(
    val anchorMatchId: String?,
    val days: List<ScheduleDay>,
)

data class RatingTarget(
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
    val category: String? = null,
    val imageUrls: List<String> = emptyList(),
    val directCommentCount: Int = commentCount,
    val showScore: Boolean = true,
    val visible: Boolean = true,
    val deleted: Boolean = false,
    val finalStatus: String? = null,
    val infoAttributes: Map<String, List<String>> = emptyMap(),
    val hotCommentPreviews: List<HupuComment> = emptyList(),
    val championImageUrl: String? = null,
)

data class RatingStage(
    val name: String,
    val targets: List<RatingTarget>,
    val outBizType: String? = null,
    val outBizNo: String? = null,
    val nodeId: Long? = null,
    val targetCount: Int = targets.size,
)

data class RatingDetail(
    val title: String,
    val stages: List<RatingStage>,
)

data class RatingGroup(
    val id: Long?,
    val name: String,
    val rootNodeId: Long,
    val sort: Int,
    val logoUrl: String?,
    val teamId: String? = null,
    val childCount: Int = 0,
    val targets: List<RatingTarget> = emptyList(),
)

data class StageRatingDetail(
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val targets: List<RatingTarget>,
    val groups: List<RatingGroup>,
)

data class CommentBadge(
    val id: String?,
    val name: String?,
    val iconUrl: String?,
    val backgroundImageUrl: String?,
)

data class CommentMedia(
    val id: String?,
    val url: String,
    val type: String?,
    val durationSeconds: Int?,
    val transcodeComplete: Boolean?,
    val audioText: String?,
    val audioConversionAvailable: Boolean? = null,
)

data class HupuComment(
    val id: String,
    val subjectId: String,
    val author: String,
    val avatarUrl: String?,
    val content: String,
    val date: String,
    val location: String?,
    val score: Int,
    val lightCount: Int,
    val imageUrls: List<String> = emptyList(),
    val replyCount: Int = 0,
    val previewReplies: List<HupuComment> = emptyList(),
    val parentCommentId: String? = null,
    val parentAuthor: String? = null,
    val authorId: String? = null,
    val badge: CommentBadge? = null,
    val certificationIconUrl: String? = null,
    val ornamentIconUrl: String? = null,
    val userLabel: String? = null,
    val hasLight: Boolean = false,
    val hasBlack: Boolean = false,
    val blackCount: Int = 0,
    val lightScore: Double = 0.0,
    val publishTime: Long? = null,
    val media: List<CommentMedia> = emptyList(),
    val ancillaryMedia: List<CommentMedia> = emptyList(),
    val parentAuthorId: String? = null,
    val parentAvatarUrl: String? = null,
    val parentContent: String? = null,
    val parentCanSee: Boolean? = null,
    val parentDeleted: Boolean = false,
    val parentMedia: List<CommentMedia> = emptyList(),
    val parentAncillaryMedia: List<CommentMedia> = emptyList(),
    val parentBadge: CommentBadge? = null,
    val parentCertificationIconUrl: String? = null,
    val parentOrnamentIconUrl: String? = null,
    val commentKey: String? = null,
    val contentDigest: String? = null,
    val commentType: String? = null,
    val aiType: String? = null,
    val occasionText: String? = null,
    val prompt: String? = null,
    val chosenTags: List<String> = emptyList(),
    val highlightTexts: List<String> = emptyList(),
)

data class CommentPage(
    val comments: List<HupuComment>,
    val totalCount: Int,
    val nextPublishTime: Long?,
    val hasMore: Boolean,
    val hottestComments: List<HupuComment> = emptyList(),
)

enum class AdapterStatus {
    SUCCESS,
    NOT_CONFIGURED,
    AUTH_REQUIRED,
    TRANSIENT_FAILURE,
    INVALID_RESPONSE,
}

data class AdapterMeta(
    val source: String,
    val receivedAtMillis: Long = System.currentTimeMillis(),
)

data class AdapterError(
    val message: String,
    val retryable: Boolean,
    val httpCode: Int? = null,
)

data class AdapterResult<T>(
    val status: AdapterStatus,
    val meta: AdapterMeta,
    val data: T? = null,
    val error: AdapterError? = null,
) {
    companion object {
        fun <T> success(source: String, data: T) = AdapterResult(
            status = AdapterStatus.SUCCESS,
            meta = AdapterMeta(source),
            data = data,
        )

        fun <T> failure(
            source: String,
            status: AdapterStatus,
            message: String,
            retryable: Boolean,
            httpCode: Int? = null,
        ) = AdapterResult<T>(
            status = status,
            meta = AdapterMeta(source),
            error = AdapterError(message, retryable, httpCode),
        )
    }
}
