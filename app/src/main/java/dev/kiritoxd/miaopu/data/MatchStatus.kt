package dev.kiritoxd.miaopu.data

internal val MatchSummary.isLive: Boolean
    get() = statusCode in setOf("LIVE", "ONGOING", "PROCESSING", "INPROGRESS") ||
        status.contains("进行") || status.contains("直播")

internal val MatchSummary.isTerminal: Boolean
    get() = statusCode in setOf("COMPLETED", "CANCELLED") ||
        status.contains("结束") || status.contains("完赛") || status.contains("取消")
