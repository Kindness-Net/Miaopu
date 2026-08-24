package dev.kiritoxd.miaopu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.HupuComment
import dev.kiritoxd.miaopu.data.RatingTarget
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CommentsScreen(viewModel: MiaopuViewModel, target: RatingTarget) {
    var showComposer by rememberSaveable(target.outBizType, target.outBizNo) { mutableStateOf(false) }
    BackHandler(enabled = !showComposer, onBack = viewModel::goBack)
    BackHandler(enabled = showComposer) { showComposer = false }
    val listState = rememberLazyListState()
    val currentTarget = viewModel.latestRatingTarget(target)
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            SmallTopAppBar(
                title = "选手详情",
                subtitle = target.name,
                navigationIcon = {
                    IconButton(onClick = viewModel::goBack) {
                        Icon(MiuixIcons.ChevronBackward, contentDescription = "返回评分")
                    }
                },
            )
        },
        bottomBar = {
            if (showComposer) {
                CommentInputBar(
                    value = viewModel.commentDraft,
                    target = currentTarget,
                    loggedIn = viewModel.isLoggedIn,
                    publishing = viewModel.isPublishingComment,
                    scoring = viewModel.scoringTargetKey != null,
                    onValueChange = viewModel::updateCommentDraft,
                    onPublish = {
                        viewModel.publishComment(target)
                        showComposer = false
                    },
                    onScore = viewModel::requestScore,
                )
            }
        },
    ) { innerPadding ->
        when (val state = viewModel.commentState) {
            LoadState.Loading -> LoadingPane("正在加载选手详情", Modifier.padding(innerPadding))
            is LoadState.Failed -> ErrorPane(
                message = state.message,
                retryable = state.retryable,
                onRetry = viewModel::retry,
                modifier = Modifier.padding(innerPadding),
            )
            is LoadState.Ready -> {
                val page = state.value
                LaunchedEffect(
                    target.outBizType,
                    target.outBizNo,
                    page.comments.size,
                    page.nextPublishTime,
                    page.hasMore,
                    viewModel.commentPaginationError,
                ) {
                    if (
                        !page.hasMore ||
                        page.nextPublishTime == null ||
                        viewModel.commentPaginationError != null
                    ) return@LaunchedEffect
                    snapshotFlow {
                        val layout = listState.layoutInfo
                        val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
                        layout.totalItemsCount > 0 && lastVisible >= layout.totalItemsCount - 3
                    }
                        .filter { it }
                        .first()
                    viewModel.loadMoreComments(target)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                    state = listState,
                ) {
                    item(contentType = "player-identity") {
                        PlayerIdentityCard(
                            target = currentTarget,
                            onCommentClick = { showComposer = true },
                        )
                    }
                    if (page.hottestComments.isNotEmpty()) {
                        item(contentType = "hot-heading") {
                            SectionHeading("亮回复 /${page.hottestComments.size} · 最亮")
                        }
                        itemsIndexed(
                            items = page.hottestComments,
                            key = { index, comment -> "hot-${comment.id.ifBlank { "$index-${comment.subjectId}" }}" },
                            contentType = { _, _ -> "hot-comment" },
                        ) { _, comment ->
                            CommentFeedItem(
                                viewModel = viewModel,
                                target = target,
                                comment = comment,
                                cardKey = "hot:${comment.id}",
                            )
                        }
                    }

                    item(contentType = "all-heading") {
                        SectionHeading("全部回复 /${page.totalCount} · 最晚")
                    }
                    if (page.comments.isEmpty()) {
                        item(contentType = "empty-comments") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "还没有更多评论",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = page.comments,
                            key = { index, comment -> "all-${comment.id.ifBlank { "$index-${comment.subjectId}" }}" },
                            contentType = { _, _ -> "comment" },
                        ) { _, comment ->
                            CommentFeedItem(
                                viewModel = viewModel,
                                target = target,
                                comment = comment,
                                cardKey = "all:${comment.id}",
                            )
                        }
                    }

                    if (page.hasMore && page.nextPublishTime != null) {
                        item(contentType = "pagination") {
                            PaginationFooter(
                                error = viewModel.commentPaginationError,
                                loading = viewModel.isLoadingMoreComments,
                                onRetry = { viewModel.loadMoreComments(target) },
                            )
                        }
                    } else {
                        item(contentType = "pagination-end") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "已加载全部 ${page.comments.size} 条回复",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CommentFeedItem(
    viewModel: MiaopuViewModel,
    target: RatingTarget,
    comment: HupuComment,
    cardKey: String,
) {
    val replies = viewModel.commentReplies
    val replyEntry = replies.entry(cardKey)
    val expanded = replyEntry != null
    PlayerCommentCard(
        comment = comment,
        expanded = expanded,
        repliesState = replyEntry?.state,
        isLoadingMoreReplies = replyEntry?.isLoadingMore == true,
        replyPaginationError = replyEntry?.paginationError,
        onToggleReplies = { replies.toggle(target, comment, cardKey) },
        onRetryReplies = { replies.retry(target, cardKey) },
        onLoadMoreReplies = { replies.loadMore(target, cardKey) },
    )
}

@Composable
private fun PaginationFooter(
    error: String?,
    loading: Boolean,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = error,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text("重试加载")
                }
            }
        } else {
            Text(
                text = if (loading) "正在异步加载更多回复…" else "继续下滑加载全部回复",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
