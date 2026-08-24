package dev.kiritoxd.miaopu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.Esport
import dev.kiritoxd.miaopu.data.EsportCatalog
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
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
fun SubscriptionScreen(viewModel: MiaopuViewModel) {
    BackHandler(onBack = viewModel::goBack)
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            SmallTopAppBar(
                title = "赛事订阅",
                subtitle = "只收录虎扑当前有赛程的项目",
                navigationIcon = {
                    IconButton(onClick = viewModel::goBack) {
                        Icon(MiuixIcons.ChevronBackward, contentDescription = "返回我的")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item {
                SubscriptionSummary(viewModel.subscribedEsports.size)
            }
            item {
                SectionHeading("可订阅项目")
            }
            items(
                items = EsportCatalog.all,
                key = Esport::businessId,
            ) { esport ->
                SubscriptionItem(
                    esport = esport,
                    subscribed = esport in viewModel.subscribedEsports,
                    canUnsubscribe = viewModel.subscribedEsports.size > 1,
                    onToggle = { viewModel.toggleSubscription(esport) },
                )
            }
        }
    }
}

@Composable
private fun SubscriptionSummary(count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        insideMargin = PaddingValues(20.dp),
        cornerRadius = 24.dp,
    ) {
        Text(
            text = "我的赛事",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "已订阅 $count 个项目",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "首页只展示你的订阅；赛程为空的电竞项目不会进入这个列表。",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun SubscriptionItem(
    esport: Esport,
    subscribed: Boolean,
    canUnsubscribe: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        insideMargin = PaddingValues(16.dp),
        cornerRadius = 20.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        if (subscribed) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.surfaceContainerHigh,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = esport.shortTitle,
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (subscribed) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(esport.title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (subscribed && !canUnsubscribe) {
                        "当前唯一订阅 · 至少保留一个项目"
                    } else {
                        "虎扑赛程 · 选手评分 · 热评"
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onToggle,
                enabled = !subscribed || canUnsubscribe,
                colors = if (subscribed) ButtonDefaults.buttonColors()
                else ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    when {
                        !subscribed -> "订阅"
                        canUnsubscribe -> "取消订阅"
                        else -> "已订阅"
                    },
                )
            }
        }
    }
}
