package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.MatchSummary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun RatingsScreen(viewModel: MiaopuViewModel, match: MatchSummary) {
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            SmallTopAppBar(
                title = "选手评分",
                subtitle = match.teams.joinToString("  vs  ") { it.name }.ifBlank { match.name },
                navigationIcon = {
                    IconButton(onClick = viewModel::goBack) {
                        Icon(MiuixIcons.ChevronBackward, contentDescription = "返回赛事")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = viewModel.ratingState) {
            LoadState.Loading -> LoadingPane("正在读取选手评分", Modifier.padding(innerPadding))
            is LoadState.Failed -> ErrorPane(
                message = state.message,
                retryable = state.retryable,
                onRetry = viewModel::retry,
                modifier = Modifier.padding(innerPadding),
            )
            is LoadState.Ready -> {
                if (state.value.stages.isEmpty()) {
                    EmptyPane("这场比赛暂时没有可评分选手", Modifier.padding(innerPadding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding,
                    ) {
                        item { MatchHero(match) }
                        item {
                            SectionHeading("${state.value.stages.size} 局比赛")
                        }
                        itemsIndexed(
                            items = state.value.stages,
                            key = { index, stage -> "stage-$index-${stage.name}" },
                            contentType = { _, _ -> "stage" },
                        ) { index, stage ->
                            MatchStageCard(
                                stage = stage,
                                onClick = { viewModel.openStage(match, stage, index + 1) },
                                onPlayerClick = viewModel::openComments,
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MatchHero(match: MatchSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        cornerRadius = 24.dp,
        insideMargin = PaddingValues(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MiuixTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "喵评分",
                    color = MiuixTheme.colorScheme.onPrimary,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = match.teams.joinToString(" vs ") { it.name }.ifBlank { match.name },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                overallMatchScore(match)?.let { score ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = score,
                        style = MiuixTheme.textStyles.title3,
                        color = MiuixTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = match.introduction.ifBlank { match.esport.title },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagPill(match.status)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = match.startTimeLabel,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

internal fun overallMatchScore(match: MatchSummary): String? {
    if (match.teams.size != 2) return null
    val bigScores = match.teams.map { it.bigScore?.takeIf(String::isNotBlank) }
    val scores = bigScores.takeIf { values -> values.all { it != null } }
        ?: match.teams.map { it.score?.takeIf(String::isNotBlank) }
    return scores.takeIf { values -> values.all { it != null } }
        ?.joinToString(" : ") { it.orEmpty() }
}
