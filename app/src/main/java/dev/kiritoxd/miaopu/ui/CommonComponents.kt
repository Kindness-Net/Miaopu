package dev.kiritoxd.miaopu.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun LoadingPane(label: String, modifier: Modifier = Modifier) {
    CenteredMessage(
        title = "正在加载",
        summary = label,
        modifier = modifier,
        action = { CircularProgressIndicator() },
    )
}

@Composable
fun ErrorPane(
    message: String,
    retryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        title = "加载失败",
        summary = message,
        modifier = modifier,
        action = if (retryable) {
            {
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColorsPrimary()) {
                    Text("重新加载")
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun EmptyPane(message: String, modifier: Modifier = Modifier) {
    CenteredMessage(
        title = "暂时空空的",
        summary = message,
        modifier = modifier,
    )
}
