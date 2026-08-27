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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.RatingTarget
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun PlayerIdentityCard(target: RatingTarget, onCommentClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        insideMargin = PaddingValues(18.dp),
        cornerRadius = 22.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingTargetPortrait(
                target = target,
                size = 88.dp,
                cornerRadius = 20.dp,
                championSize = 31.dp,
                contentDescription = "${target.name}头像",
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = target.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.title1,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (target.scoreCount > 0) {
                            "%.1f".format(target.scoreAverage)
                        } else {
                            "—"
                        },
                        style = MiuixTheme.textStyles.title1,
                        color = MiuixTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                val tags = buildList {
                    target.description?.takeIf(String::isNotBlank)?.let(::add)
                    addAll(target.labels)
                    target.stageName?.takeIf { it.isNotBlank() && it != "评分" }?.let(::add)
                }.distinct().take(3)
                tags.forEach { tag ->
                    Spacer(Modifier.height(6.dp))
                    TagPill(tag)
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${target.scoreCount} JRs评分 · ${target.commentCount} 条评论",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.width(6.dp))
                    TextButton(
                        text = "写评论",
                        onClick = onCommentClick,
                        minWidth = 62.dp,
                        minHeight = 32.dp,
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        textStyle = MiuixTheme.textStyles.footnote1,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        ScoreDistributionStats(target)
    }
}

@Composable
private fun ScoreDistributionStats(target: RatingTarget) {
    Text(
        text = "1–5 星评分数量",
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (1..5).forEach { stars ->
            val count = target.scoreDistribution[stars * 2] ?: 0
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 7.dp)
                    .semantics { contentDescription = "$stars 星，$count 人" },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$stars★",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = count.toString(),
                    maxLines = 1,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun CommentInputBar(
    value: String,
    target: RatingTarget,
    loggedIn: Boolean,
    publishing: Boolean,
    selectedScore: Int,
    onValueChange: (String) -> Unit,
    onPublish: () -> Unit,
    onScoreChange: (Int) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scoreChanged = hasPendingScore(selectedScore, target.userScore)
    val scoreOnly = value.isBlank() && scoreChanged
    LaunchedEffect(loggedIn) {
        if (!loggedIn) return@LaunchedEffect
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        insideMargin = PaddingValues(12.dp),
        cornerRadius = 22.dp,
    ) {
        if (target.canScore) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (target.userScore > 0) "我的评分" else "选几星",
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                (1..5).forEach { stars ->
                    val score = stars * 2
                    val filled = selectedScore >= score
                    IconButton(
                        onClick = { onScoreChange(score) },
                        modifier = Modifier.semantics {
                            contentDescription = "$stars 星"
                            selected = selectedScore == score
                        },
                        enabled = loggedIn && !publishing,
                        minWidth = 40.dp,
                        minHeight = 40.dp,
                    ) {
                        Text(
                            text = if (filled) "★" else "☆",
                            color = if (filled) MiuixTheme.colorScheme.primary else {
                                MiuixTheme.colorScheme.onSurfaceVariantSummary
                            },
                        )
                    }
                    if (stars < 5) Spacer(Modifier.width(3.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            insideMargin = DpSize(width = 14.dp, height = 12.dp),
            cornerRadius = 16.dp,
            label = if (loggedIn) "说说你的看法" else "登录后即可发表评论",
            useLabelAsPlaceholder = true,
            enabled = loggedIn && !publishing,
            singleLine = false,
            minLines = 3,
            maxLines = 6,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (loggedIn) "${value.length}/500" else "请先在“我的”中登录",
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = if (loggedIn) {
                            "已输入 ${value.length} 字，最多 500 字"
                        } else {
                            "请先登录虎扑"
                        }
                    },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onPublish,
                enabled = loggedIn && canSubmitCommentOrScore(value, selectedScore, target.userScore) && !publishing,
                minWidth = 72.dp,
                minHeight = 40.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    text = when {
                        publishing -> "提交中"
                        scoreOnly -> "评分"
                        else -> "发送"
                    },
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}

internal fun canSubmitCommentOrScore(
    comment: String,
    selectedScore: Int,
    userScore: Int,
): Boolean = comment.isNotBlank() || hasPendingScore(selectedScore, userScore)

private fun hasPendingScore(selectedScore: Int, userScore: Int): Boolean =
    selectedScore > 0 && selectedScore != userScore
