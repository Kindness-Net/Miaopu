package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.kiritoxd.miaopu.data.Team
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TabRowColors
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val MiaopuGold = Color(0xFFFFB300)

@Composable
internal fun flatTabRowColors(): TabRowColors = TabRowDefaults.tabRowColors(
    backgroundColor = Color.Transparent,
    selectedBackgroundColor = Color.White,
    selectedContentColor = Color(0xFF181818),
)

@Composable
internal fun PageHeading(
    eyebrow: String,
    title: String,
    summary: String? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                eyebrow,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(title, style = MiuixTheme.textStyles.title1, fontWeight = FontWeight.Bold)
            summary?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    it,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        actions?.invoke()
    }
}

@Composable
internal fun SectionHeading(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.title3,
            fontWeight = FontWeight.Bold,
        )
        if (action != null && onAction != null) {
            TextButton(
                text = action,
                onClick = onAction,
                textStyle = MiuixTheme.textStyles.footnote1,
            )
        }
    }
}

@Composable
internal fun TagPill(
    text: String,
    containerColor: Color = MiuixTheme.colorScheme.surfaceVariant,
    contentColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MiuixTheme.textStyles.footnote2, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun TeamLogo(
    team: Team,
    size: Dp,
    onAccent: Boolean = false,
) {
    val fallbackBackground = if (onAccent) Color.White.copy(alpha = 0.18f) else MiuixTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(fallbackBackground),
        contentAlignment = Alignment.Center,
    ) {
        if (team.logoUrl.isNullOrBlank()) {
            Text(
                team.name.take(1),
                color = if (onAccent) Color.White else MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        } else {
            AsyncImage(
                model = team.logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
internal fun ScoreRing(
    score: Double,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    accentColor: Color = MiuixTheme.colorScheme.primary,
    trackColor: Color = MiuixTheme.colorScheme.surfaceVariant,
) {
    val normalized = score.coerceIn(0.0, 10.0)
    Box(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = "评分 %.1f 分".format(normalized) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            drawCircle(trackColor, style = Stroke(width = stroke))
            if (normalized > 0) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = (normalized / 10.0 * 360.0).toFloat(),
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            if (normalized > 0) "%.1f".format(normalized) else "—",
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun CompactTeamLine(team: Team) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TeamLogo(team = team, size = 30.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            team.name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (team.winner) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            team.score ?: "—",
            style = MiuixTheme.textStyles.title4,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun CenteredMessage(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("喵", color = MiuixTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
            Text(
                summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
            )
            action?.invoke()
        }
    }
}
