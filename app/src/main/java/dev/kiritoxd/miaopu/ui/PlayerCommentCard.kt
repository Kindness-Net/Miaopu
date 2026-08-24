package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.kiritoxd.miaopu.data.CommentPage
import dev.kiritoxd.miaopu.data.HupuComment
import dev.kiritoxd.miaopu.data.nestedReplyTarget
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun PlayerCommentCard(
    comment: HupuComment,
    expanded: Boolean,
    repliesState: LoadState<CommentPage>?,
    isLoadingMoreReplies: Boolean,
    replyPaginationError: String?,
    onToggleReplies: () -> Unit,
    onRetryReplies: () -> Unit,
    onLoadMoreReplies: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        insideMargin = PaddingValues(16.dp),
        cornerRadius = 20.dp,
    ) {
        CommentAuthor(comment)
        Spacer(Modifier.height(12.dp))
        Text(comment.content, style = MiuixTheme.textStyles.body1)
        CommentImages(comment.imageUrls)

        if (expanded) {
            ExpandedReplies(
                state = repliesState,
                fallback = comment.previewReplies,
                rootCommentId = comment.id,
                isLoadingMore = isLoadingMoreReplies,
                paginationError = replyPaginationError,
                onRetry = onRetryReplies,
                onLoadMore = onLoadMoreReplies,
            )
        } else {
            comment.previewReplies.firstOrNull()?.let { reply ->
                Spacer(Modifier.height(10.dp))
                PreviewReply(reply, rootCommentId = comment.id)
            }
        }

        Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = listOfNotNull(comment.date.takeIf(String::isNotBlank), comment.location).joinToString(" · "),
                modifier = Modifier.weight(1f),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            if (comment.replyCount > 0) {
                TextButton(
                    text = if (expanded) "收起回复" else "${comment.replyCount} 条回复",
                    onClick = onToggleReplies,
                    modifier = Modifier.width(96.dp),
                    minWidth = 96.dp,
                    minHeight = 32.dp,
                    insideMargin = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    textStyle = MiuixTheme.textStyles.footnote2,
                )
            }
        }
    }
}

@Composable
private fun CommentAuthor(comment: HupuComment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CommentAvatar(comment, 40)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MiuixTheme.textStyles.body1,
                )
                comment.badge?.name?.takeIf(String::isNotBlank)?.let { badgeName ->
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = badgeName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "👍 ${comment.lightCount}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (comment.hasLight) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    },
                )
            }
            RatingStars(comment.score)
        }
    }
}

@Composable
private fun RatingStars(score: Int) {
    val filledStars = (score / 2).coerceIn(0, 5)
    Text(
        text = buildString {
            repeat(filledStars) { append('★') }
            repeat(5 - filledStars) { append('☆') }
        },
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.primary,
    )
}

@Composable
private fun CommentImages(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) return
    Spacer(Modifier.height(10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(
            items = imageUrls,
            key = { index, imageUrl -> "$index-$imageUrl" },
            contentType = { _, _ -> "comment-image" },
        ) { _, imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "评论图片",
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun ExpandedReplies(
    state: LoadState<CommentPage>?,
    fallback: List<HupuComment>,
    rootCommentId: String,
    isLoadingMore: Boolean,
    paginationError: String?,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Spacer(Modifier.height(10.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            null, LoadState.Loading -> {
                fallback.firstOrNull()?.let { ReplyRow(it, rootCommentId) }
                Text(
                    text = "正在加载完整回复…",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            is LoadState.Failed -> {
                fallback.firstOrNull()?.let { ReplyRow(it, rootCommentId) }
                Text(
                    text = state.message,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (state.retryable) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text("重试展开")
                    }
                }
            }
            is LoadState.Ready -> {
                val page = state.value
                if (page.comments.isEmpty()) {
                    Text(
                        text = "暂无可见回复",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                } else {
                    page.comments.forEach { ReplyRow(it, rootCommentId) }
                }
                when {
                    paginationError != null -> {
                        Text(
                            text = paginationError,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        TextButton(
                            text = "重试加载更多",
                            onClick = onLoadMore,
                            textStyle = MiuixTheme.textStyles.footnote2,
                        )
                    }
                    page.hasMore && page.nextPublishTime != null -> TextButton(
                        text = if (isLoadingMore) "加载中…" else "加载更多回复",
                        onClick = onLoadMore,
                        enabled = !isLoadingMore,
                        textStyle = MiuixTheme.textStyles.footnote2,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewReply(reply: HupuComment, rootCommentId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommentAvatar(reply, 28)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = replyAuthorLabel(reply, rootCommentId),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MiuixTheme.textStyles.footnote1,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = reply.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MiuixTheme.textStyles.footnote1,
            )
        }
    }
}

@Composable
private fun ReplyRow(reply: HupuComment, rootCommentId: String) {
    Row(verticalAlignment = Alignment.Top) {
        CommentAvatar(reply, 30)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = replyAuthorLabel(reply, rootCommentId),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MiuixTheme.textStyles.footnote1,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "👍 ${reply.lightCount}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(reply.content, style = MiuixTheme.textStyles.footnote1)
            val metadata = listOfNotNull(reply.date.takeIf(String::isNotBlank), reply.location).joinToString(" · ")
            if (metadata.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = metadata,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

private fun replyAuthorLabel(reply: HupuComment, rootCommentId: String): String =
    reply.nestedReplyTarget(rootCommentId)?.let { "${reply.author} 回复 $it" } ?: reply.author

@Composable
private fun CommentAvatar(comment: HupuComment, sizeDp: Int) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (comment.avatarUrl.isNullOrBlank()) {
            Text(
                comment.author.take(1),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        } else {
            AsyncImage(
                model = comment.avatarUrl,
                contentDescription = "${comment.author}头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
