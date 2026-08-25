package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.kiritoxd.miaopu.data.RatingTarget
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun RatingTargetPortrait(
    target: RatingTarget,
    size: Dp,
    cornerRadius: Dp,
    championSize: Dp,
    contentDescription: String? = null,
) {
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(MiuixTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (target.imageUrl.isNullOrBlank()) {
                Text(
                    text = target.name.take(1),
                    color = MiuixTheme.colorScheme.primary,
                    style = if (size >= 60.dp) MiuixTheme.textStyles.title2 else MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                AsyncImage(
                    model = target.imageUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        target.championImageUrl?.takeIf(String::isNotBlank)?.let { championImageUrl ->
            val championShape = RoundedCornerShape(championSize / 4f)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(championSize)
                    .clip(championShape)
                    .background(MiuixTheme.colorScheme.surface)
                    .padding(2.dp),
            ) {
                AsyncImage(
                    model = championImageUrl,
                    contentDescription = "${target.name}本局使用的英雄",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(championSize / 5f)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
