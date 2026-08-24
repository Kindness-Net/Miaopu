package dev.kiritoxd.miaopu.ui

import androidx.compose.runtime.mutableStateMapOf
import dev.kiritoxd.miaopu.data.AdapterStatus
import dev.kiritoxd.miaopu.data.CommentPage
import dev.kiritoxd.miaopu.data.HupuAdapter
import dev.kiritoxd.miaopu.data.HupuComment
import dev.kiritoxd.miaopu.data.RatingTarget
import dev.kiritoxd.miaopu.data.mergeCommentsByHeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal data class CommentRepliesEntry(
    val parentCommentId: String,
    val state: LoadState<CommentPage>,
    val isLoadingMore: Boolean = false,
    val paginationError: String? = null,
)

/** Owns an independent loading and pagination lifecycle for every expanded comment card. */
internal class CommentRepliesController(
    private val scope: CoroutineScope,
    private val adapter: HupuAdapter,
) {
    private val entries = mutableStateMapOf<String, CommentRepliesEntry>()
    private val jobs = mutableMapOf<String, Job>()
    private val generations = mutableMapOf<String, Long>()
    private var generationCounter = 0L

    fun entry(cardKey: String): CommentRepliesEntry? = entries[cardKey]

    fun toggle(target: RatingTarget, comment: HupuComment, cardKey: String) {
        if (cardKey in entries) {
            collapse(cardKey)
        } else {
            loadInitial(target, comment.id, cardKey)
        }
    }

    fun retry(target: RatingTarget, cardKey: String) {
        val parentId = entries[cardKey]?.parentCommentId ?: return
        loadInitial(target, parentId, cardKey)
    }

    fun loadMore(target: RatingTarget, cardKey: String) {
        val entry = entries[cardKey] ?: return
        if (entry.isLoadingMore) return
        val current = (entry.state as? LoadState.Ready)?.value ?: return
        val cursor = current.nextPublishTime ?: return
        entries[cardKey] = entry.copy(isLoadingMore = true, paginationError = null)
        jobs.remove(cardKey)?.cancel()
        val generation = nextGeneration(cardKey)
        jobs[cardKey] = scope.launch {
            try {
                val result = adapter.getCommentReplies(target, entry.parentCommentId, cursor)
                if (generations[cardKey] != generation) return@launch
                val latest = entries[cardKey]
                    ?.takeIf { it.parentCommentId == entry.parentCommentId }
                    ?: return@launch
                val next = result.data
                entries[cardKey] = if (result.status == AdapterStatus.SUCCESS && next != null) {
                    latest.copy(
                        state = LoadState.Ready(
                            next.copy(
                                comments = mergeCommentsByHeat(current.comments, next.comments),
                                totalCount = maxOf(current.totalCount, next.totalCount),
                            ),
                        ),
                        isLoadingMore = false,
                    )
                } else {
                    latest.copy(
                        isLoadingMore = false,
                        paginationError = result.error?.message ?: "加载更多回复失败",
                    )
                }
            } finally {
                if (generations[cardKey] == generation) {
                    entries[cardKey]?.let { latest ->
                        if (latest.isLoadingMore) entries[cardKey] = latest.copy(isLoadingMore = false)
                    }
                    jobs.remove(cardKey)
                }
            }
        }
    }

    fun clear() {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
        generations.clear()
        entries.clear()
    }

    private fun collapse(cardKey: String) {
        jobs.remove(cardKey)?.cancel()
        nextGeneration(cardKey)
        entries.remove(cardKey)
    }

    private fun loadInitial(target: RatingTarget, parentId: String, cardKey: String) {
        jobs.remove(cardKey)?.cancel()
        val generation = nextGeneration(cardKey)
        entries[cardKey] = CommentRepliesEntry(
            parentCommentId = parentId,
            state = LoadState.Loading,
        )
        jobs[cardKey] = scope.launch {
            val result = adapter.getCommentReplies(target, parentId)
            if (generations[cardKey] != generation) return@launch
            val latest = entries[cardKey]?.takeIf { it.parentCommentId == parentId } ?: return@launch
            entries[cardKey] = latest.copy(
                state = result.data?.let(LoadState<CommentPage>::Ready)
                    ?: LoadState.Failed(
                        message = result.error?.message ?: "展开回复失败",
                        retryable = result.error?.retryable == true,
                    ),
            )
            if (generations[cardKey] == generation) jobs.remove(cardKey)
        }
    }

    private fun nextGeneration(cardKey: String): Long =
        (++generationCounter).also { generations[cardKey] = it }
}
