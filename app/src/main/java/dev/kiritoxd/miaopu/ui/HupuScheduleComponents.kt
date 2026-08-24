package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.MatchSummary
import dev.kiritoxd.miaopu.data.ScheduleDay
import dev.kiritoxd.miaopu.data.Team
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
internal fun ScheduleDateStrip(
    days: List<ScheduleDay>,
    selectedDayKey: String,
    state: LazyListState,
    onDaySelected: (Int) -> Unit,
) {
    LazyRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = days,
            key = { index, day -> "date-${day.date}-$index" },
        ) { index, day ->
            val selected = day.date == selectedDayKey
            Card(
                modifier = Modifier
                    .widthIn(min = 92.dp)
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                        contentDescription = "${day.label.ifBlank { day.date }}，${day.matches.size} 场比赛"
                    },
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                cornerRadius = 16.dp,
                colors = CardDefaults.defaultColors(
                    color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = if (selected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface,
                ),
                onClick = { onDaySelected(index) },
                pressFeedbackType = PressFeedbackType.Sink,
                showIndication = true,
            ) {
                Text(
                    text = day.label.ifBlank { day.date },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MiuixTheme.textStyles.footnote1,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${day.matches.size} 场",
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (selected) {
                        MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                    } else {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    },
                )
            }
        }
    }
}

@Composable
internal fun ScheduleDayBand(day: ScheduleDay, isFocused: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFocused) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MiuixTheme.colorScheme.primary),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = day.label.ifBlank { day.date },
            modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.footnote1,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${day.matches.size} 场",
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
internal fun HupuScheduleMatchCard(match: MatchSummary, onClick: () -> Unit) {
    val canOpen = match.hasRatings
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (canOpen) {
                    Modifier.semantics {
                        role = Role.Button
                        contentDescription = "查看 ${match.name.ifBlank { match.teams.joinToString(" 对 ") { it.name } }}"
                    }
                } else {
                    Modifier
                },
            ),
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
        cornerRadius = 16.dp,
        onClick = if (canOpen) onClick else null,
        pressFeedbackType = if (canOpen) PressFeedbackType.Sink else PressFeedbackType.None,
        showIndication = canOpen,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(76.dp)) {
                Text(
                    text = match.startTimeLabel.ifBlank { "待定" },
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = match.introduction.ifBlank { match.name },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                if (match.teams.isEmpty()) {
                    Text(
                        text = match.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    match.teams.take(2).forEach { team -> ScheduleTeamLine(team) }
                }
            }

            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .width(1.dp)
                    .height(54.dp)
                    .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.22f)),
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.width(70.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = match.status.ifBlank { "待定" },
                    maxLines = 1,
                    style = MiuixTheme.textStyles.body2,
                    color = statusColor(match),
                    fontWeight = if (match.isLive) FontWeight.Bold else FontWeight.Normal,
                )
                match.scoreCountText?.takeIf { it.isNotBlank() }?.let { scoreCount ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = scoreCount,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTeamLine(team: Team) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TeamLogo(team = team, size = 28.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = team.name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.body2,
            fontWeight = if (team.winner) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = team.score ?: "—",
            modifier = Modifier.width(24.dp),
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun statusColor(match: MatchSummary): Color = when {
    match.isLive -> MiuixTheme.colorScheme.error
    match.statusCode == "COMPLETED" || match.status.contains("结束") || match.status.contains("完赛") ->
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    else -> MiuixTheme.colorScheme.onSurface
}

private val MatchSummary.hasRatings: Boolean
    get() = !outBizType.isNullOrBlank() && !outBizNo.isNullOrBlank()

private val MatchSummary.isLive: Boolean
    get() = statusCode in setOf("LIVE", "ONGOING", "PROCESSING") ||
        status.contains("进行") || status.contains("直播")
