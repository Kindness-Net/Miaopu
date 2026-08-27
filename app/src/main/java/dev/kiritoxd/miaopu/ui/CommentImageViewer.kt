package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun CommentImages(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) return
    var previewIndex by remember(imageUrls) { mutableStateOf<Int?>(null) }
    Spacer(Modifier.height(10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(
            items = imageUrls,
            key = { index, imageUrl -> "$index-$imageUrl" },
            contentType = { _, _ -> "comment-image" },
        ) { index, imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "查看评论图片 ${index + 1}/${imageUrls.size}",
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant)
                    .clickable(role = Role.Button) { previewIndex = index },
                contentScale = ContentScale.Crop,
            )
        }
    }
    previewIndex?.let { index ->
        CommentImageViewer(
            imageUrls = imageUrls,
            initialIndex = index,
            onDismiss = { previewIndex = null },
        )
    }
}

@Composable
internal fun CommentImageViewer(
    imageUrls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    if (imageUrls.isEmpty()) return
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(imageUrls.indices),
        pageCount = { imageUrls.size },
    )
    var zoomedPage by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = zoomedPage == null,
                key = { "$it-${imageUrls[it]}" },
            ) { page ->
                ZoomableCommentImage(
                    imageUrl = imageUrls[page],
                    onZoomChanged = { zoomed ->
                        zoomedPage = if (zoomed) page else null
                    },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopStart),
                    backgroundColor = Color.Black.copy(alpha = 0.45f),
                    minWidth = 48.dp,
                    minHeight = 48.dp,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "关闭图片预览",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MiuixTheme.textStyles.body1,
                    color = Color.White,
                )
                Text(
                    text = if (imageUrls.size > 1) "左右滑动 · 双击缩放" else "双击缩放",
                    modifier = Modifier.align(Alignment.BottomCenter),
                    style = MiuixTheme.textStyles.footnote1,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun ZoomableCommentImage(
    imageUrl: String,
    onZoomChanged: (Boolean) -> Unit,
) {
    var scale by remember(imageUrl) { mutableStateOf(1f) }
    var offset by remember(imageUrl) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember(imageUrl) { mutableStateOf(IntSize.Zero) }
    val latestScale by rememberUpdatedState(scale)
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (nextScale == 1f) {
            Offset.Zero
        } else {
            val maxX = viewportSize.width * (nextScale - 1f) / 2f
            val maxY = viewportSize.height * (nextScale - 1f) / 2f
            Offset(
                x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                y = (offset.y + panChange.y).coerceIn(-maxY, maxY),
            )
        }
        scale = nextScale
        onZoomChanged(nextScale > 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewportSize = it }
            .pointerInput(imageUrl) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (latestScale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                        onZoomChanged(scale > 1f)
                    },
                )
            }
            .transformable(
                state = transformState,
                enabled = scale > 1f,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "评论图片大图",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit,
        )
    }
}
