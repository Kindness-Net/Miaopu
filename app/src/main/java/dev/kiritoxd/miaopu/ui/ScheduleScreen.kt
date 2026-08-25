package dev.kiritoxd.miaopu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.kiritoxd.miaopu.data.EsportCatalog
import dev.kiritoxd.miaopu.data.Schedule
import dev.kiritoxd.miaopu.data.focusMatchId
import dev.kiritoxd.miaopu.data.fullScheduleInitialDayIndex
import dev.kiritoxd.miaopu.data.homeInitialItemIndex
import dev.kiritoxd.miaopu.data.homeWindowAround
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ContactsCircle
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Tasks
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun ScheduleScreen(viewModel: MiaopuViewModel) {
    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = viewModel.selectedMainSection != MainSection.HOME,
        onBackCompleted = { viewModel.selectMainSection(MainSection.HOME) },
    )
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        bottomBar = {
            MainNavigationBar(
                selected = viewModel.selectedMainSection,
                onSelect = viewModel::selectMainSection,
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = viewModel.selectedMainSection,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                val enter = slideInHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetX = { fullWidth -> direction * fullWidth / 5 },
                ) + fadeIn(animationSpec = tween(durationMillis = 240))
                val exit = slideOutHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    targetOffsetX = { fullWidth -> -direction * fullWidth / 5 },
                ) + fadeOut(animationSpec = tween(durationMillis = 180))
                enter togetherWith exit
            },
            label = "main-section",
        ) { section ->
            MainSectionContent(
                viewModel = viewModel,
                section = section,
                innerPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun MainSectionContent(
    viewModel: MiaopuViewModel,
    section: MainSection,
    innerPadding: PaddingValues,
) {
    if (section == MainSection.PROFILE) {
        ProfileContent(viewModel = viewModel, innerPadding = innerPadding)
        return
    }

    when (val state = viewModel.scheduleState) {
        LoadState.Loading -> ScheduleLoadingContent(viewModel, innerPadding, section)
        is LoadState.Failed -> ErrorPane(
            message = state.message,
            retryable = state.retryable,
            onRetry = viewModel::retry,
            modifier = Modifier.padding(innerPadding),
        )
        is LoadState.Ready -> when (section) {
            MainSection.HOME -> HomeContent(viewModel, state.value, innerPadding)
            MainSection.EVENTS -> EventsContent(viewModel, state.value, innerPadding)
            MainSection.PROFILE -> Unit
        }
    }
}

@Composable
private fun ScheduleLoadingContent(
    viewModel: MiaopuViewModel,
    innerPadding: PaddingValues,
    section: MainSection,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
    ) {
        when (section) {
            MainSection.HOME -> HomeHeader(viewModel)
            MainSection.EVENTS -> EventsHeader(viewModel)
            MainSection.PROFILE -> Unit
        }
        LoadingPane(
            label = "正在同步${viewModel.selectedEsport.title}赛程",
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun MainNavigationBar(
    selected: MainSection,
    onSelect: (MainSection) -> Unit,
) {
    NavigationBar(showDivider = false) {
        NavigationBarItem(
            selected = selected == MainSection.HOME,
            onClick = { onSelect(MainSection.HOME) },
            icon = MiuixIcons.Home,
            label = "首页",
        )
        NavigationBarItem(
            selected = selected == MainSection.EVENTS,
            onClick = { onSelect(MainSection.EVENTS) },
            icon = MiuixIcons.Tasks,
            label = "赛事",
        )
        NavigationBarItem(
            selected = selected == MainSection.PROFILE,
            onClick = { onSelect(MainSection.PROFILE) },
            icon = MiuixIcons.ContactsCircle,
            label = "我的",
        )
    }
}

@Composable
private fun HomeContent(
    viewModel: MiaopuViewModel,
    schedule: Schedule,
    innerPadding: PaddingValues,
) {
    val nowMillis = remember(schedule) { System.currentTimeMillis() }
    val homeSchedule = remember(schedule, nowMillis) { schedule.homeWindowAround(nowMillis) }

    if (homeSchedule.days.isEmpty()) {
        EmptyPane("暂无赛程数据", Modifier.padding(innerPadding))
        return
    }
    val esport = viewModel.selectedEsport
    val listState = rememberSaveable(esport.businessId, saver = LazyListState.Saver) {
        LazyListState(firstVisibleItemIndex = homeSchedule.homeInitialItemIndex(nowMillis))
    }

    LaunchedEffect(homeSchedule, listState) {
        val lastIndex = (homeSchedule.days.sumOf { 1 + it.matches.size } - 1).coerceAtLeast(0)
        if (listState.firstVisibleItemIndex > lastIndex) listState.scrollToItem(lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
    ) {
        HomeHeader(viewModel)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
        ) {
            homeSchedule.days.forEach { day ->
                item(key = "home-day-${day.date}", contentType = "day") {
                    ScheduleDayBand(
                        day = day,
                        isFocused = day.matches.any { it.id == homeSchedule.anchorMatchId },
                    )
                }
                itemsIndexed(
                    items = day.matches,
                    key = { index, match -> "home-${day.date}-${match.id}-$index" },
                    contentType = { _, _ -> "schedule-match" },
                ) { _, match ->
                    HupuScheduleMatchCard(match = match, onClick = { viewModel.openMatch(match) })
                }
            }
        }
    }
}

@Composable
private fun EventsContent(
    viewModel: MiaopuViewModel,
    schedule: Schedule,
    innerPadding: PaddingValues,
) {
    if (schedule.days.isEmpty()) {
        EmptyPane("暂无赛程数据", Modifier.padding(innerPadding))
        return
    }

    val esport = viewModel.selectedEsport
    val nowMillis = remember(schedule) { System.currentTimeMillis() }
    val focusMatchId = remember(schedule, nowMillis) { schedule.focusMatchId(nowMillis) }
    val anchorDayIndex = remember(schedule, focusMatchId) {
        schedule.days.indexOfFirst { day -> day.matches.any { it.id == focusMatchId } }
            .coerceAtLeast(0)
    }
    val dayItemIndices = remember(schedule) {
        buildList {
            var itemIndex = 0
            schedule.days.forEach { day ->
                add(itemIndex)
                itemIndex += 1 + day.matches.size
            }
        }
    }
    val initialDayIndex = remember(schedule, nowMillis) { schedule.fullScheduleInitialDayIndex(nowMillis) }
    val savedViewport = remember(esport, schedule) { viewModel.scheduleViewport(esport) }
    val totalListItems = remember(schedule) { schedule.days.sumOf { 1 + it.matches.size } }
    val restoredListIndex = savedViewport?.listIndex?.coerceIn(0, (totalListItems - 1).coerceAtLeast(0))
    val restoredDateIndex = savedViewport?.dateStripIndex?.coerceIn(0, schedule.days.lastIndex)
    val listState = rememberSaveable(esport.businessId, "events-list", saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = restoredListIndex ?: dayItemIndices.getOrElse(initialDayIndex) { 0 },
            firstVisibleItemScrollOffset = savedViewport?.listOffset?.coerceAtLeast(0) ?: 0,
        )
    }
    val dateStripState = rememberSaveable(esport.businessId, "events-dates", saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = restoredDateIndex ?: initialDayIndex,
            firstVisibleItemScrollOffset = savedViewport?.dateStripOffset?.coerceAtLeast(0) ?: 0,
        )
    }
    val scope = rememberCoroutineScope()
    var selectedDayKey by rememberSaveable(esport.businessId, "events-day") {
        mutableStateOf(
            savedViewport?.selectedDayKey
                ?.takeIf { saved -> schedule.days.any { it.date == saved } }
                ?: schedule.days.getOrNull(initialDayIndex)?.date.orEmpty(),
        )
    }
    val latestSelectedDayKey by rememberUpdatedState(selectedDayKey)

    LaunchedEffect(esport, schedule, listState, dateStripState) {
        val lastListIndex = (totalListItems - 1).coerceAtLeast(0)
        if (listState.firstVisibleItemIndex > lastListIndex) listState.scrollToItem(lastListIndex)
        if (dateStripState.firstVisibleItemIndex > schedule.days.lastIndex) {
            dateStripState.scrollToItem(schedule.days.lastIndex)
        }
        if (schedule.days.none { it.date == selectedDayKey }) {
            selectedDayKey = schedule.days.getOrNull(initialDayIndex)?.date.orEmpty()
        }
    }

    DisposableEffect(esport, schedule, listState, dateStripState) {
        onDispose {
            viewModel.saveScheduleViewport(
                esport,
                ScheduleViewportSnapshot(
                    listIndex = listState.firstVisibleItemIndex,
                    listOffset = listState.firstVisibleItemScrollOffset,
                    dateStripIndex = dateStripState.firstVisibleItemIndex,
                    dateStripOffset = dateStripState.firstVisibleItemScrollOffset,
                    selectedDayKey = latestSelectedDayKey,
                ),
            )
        }
    }

    LaunchedEffect(esport, schedule, savedViewport) {
        if (savedViewport == null) dateStripState.centerItem(initialDayIndex, animated = false)
    }

    LaunchedEffect(esport, schedule, listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { itemIndex -> dayItemIndices.indexOfLast { it <= itemIndex }.coerceAtLeast(0) }
            .distinctUntilChanged()
            .collect { dayIndex -> selectedDayKey = schedule.days[dayIndex].date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
    ) {
        EventsHeader(viewModel)
        ScheduleDateStrip(
            days = schedule.days,
            selectedDayKey = selectedDayKey,
            state = dateStripState,
            onDaySelected = { index ->
                selectedDayKey = schedule.days[index].date
                scope.launch { listState.animateScrollToItem(dayItemIndices[index]) }
                scope.launch { dateStripState.centerItem(index, animated = true) }
            },
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
        ) {
            schedule.days.forEachIndexed { dayIndex, day ->
                item(key = "day-${day.date}", contentType = "day") {
                    ScheduleDayBand(day = day, isFocused = dayIndex == anchorDayIndex)
                }
                itemsIndexed(
                    items = day.matches,
                    key = { index, match -> "${day.date}-${match.id}-$index" },
                    contentType = { _, _ -> "schedule-match" },
                ) { _, match ->
                    HupuScheduleMatchCard(match = match, onClick = { viewModel.openMatch(match) })
                }
            }
        }
    }
}

private suspend fun LazyListState.centerItem(index: Int, animated: Boolean) {
    if (animated) animateScrollToItem(index) else scrollToItem(index)
    val item = snapshotFlow {
        layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    }.filterNotNull().first()
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    val itemCenter = item.offset + item.size / 2
    val delta = (itemCenter - viewportCenter).toFloat()
    if (animated) animateScrollBy(delta) else scrollBy(delta)
}

@Composable
private fun HomeHeader(viewModel: MiaopuViewModel) {
    PageHeading(
        eyebrow = "HUPU ESPORTS",
        title = "近期赛程",
        actions = {
            IconButton(
                onClick = viewModel::refreshSchedule,
                backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
            ) {
                Icon(MiuixIcons.Refresh, contentDescription = "刷新赛程")
            }
        },
    )
    EsportSelector(viewModel)
}

@Composable
private fun EventsHeader(viewModel: MiaopuViewModel) {
    PageHeading(
        eyebrow = "MATCH CENTER",
        title = "完整赛程",
        actions = {
            IconButton(
                onClick = viewModel::refreshSchedule,
                backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
            ) {
                Icon(MiuixIcons.Refresh, contentDescription = "刷新赛程")
            }
        },
    )
    EsportSelector(viewModel)
}

@Composable
private fun EsportSelector(viewModel: MiaopuViewModel) {
    val subscriptions = EsportCatalog.all.filter { it in viewModel.subscribedEsports }
    TabRow(
        tabs = subscriptions.map { it.shortTitle },
        selectedTabIndex = subscriptions.indexOf(viewModel.selectedEsport).coerceAtLeast(0),
        onTabSelected = { viewModel.selectEsport(subscriptions[it]) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        colors = dataSourceTabRowColors(),
    )
}

@Composable
private fun ProfileContent(viewModel: MiaopuViewModel, innerPadding: PaddingValues) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = pagePadding(innerPadding),
    ) {
        item {
            PageHeading(
                eyebrow = "ACCOUNT",
                title = "我的喵扑",
            )
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                cornerRadius = 24.dp,
                insideMargin = PaddingValues(20.dp),
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("喵扑用户", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (viewModel.isLoggedIn) "已连接虎扑账号" else "登录后即可参与选手评分",
                            color = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                        )
                    }
                    Text("🐱", style = MiuixTheme.textStyles.title1)
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = if (viewModel.isLoggedIn) viewModel::logout else viewModel::openLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.onPrimary,
                        contentColor = MiuixTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (viewModel.isLoggedIn) "退出登录" else "登录虎扑账号")
                }
            }
        }
        item { SectionHeading("赛事订阅") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "管理赛事订阅，当前 ${viewModel.subscribedEsports.size} 个项目"
                    },
                insideMargin = PaddingValues(18.dp),
                cornerRadius = 20.dp,
                onClick = viewModel::openSubscriptions,
                pressFeedbackType = PressFeedbackType.Sink,
                showIndication = true,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("管理赛事项目", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "已订阅 ${viewModel.subscribedEsports.size} 个有赛程的电竞项目",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Text(
                        "管理  ›",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item { SectionHeading("应用") }
        item {
            val updateState = viewModel.updateCheckState
            val summary = when (updateState) {
                UpdateCheckState.Idle -> "当前版本 ${viewModel.currentVersion}"
                UpdateCheckState.Checking -> "正在查询 GitHub Release…"
                is UpdateCheckState.UpToDate -> "已是最新版本 · ${updateState.latestTag}"
                is UpdateCheckState.Available -> "发现新版本 ${updateState.release.tagName}"
                is UpdateCheckState.Failed -> updateState.message
            }
            ProfileActionCard(
                title = "检查更新",
                summary = summary,
                action = when (updateState) {
                    UpdateCheckState.Checking -> "检查中"
                    is UpdateCheckState.Available -> "下载 ›"
                    is UpdateCheckState.Failed -> "重试 ›"
                    else -> "检查 ›"
                },
                enabled = updateState != UpdateCheckState.Checking,
                onClick = {
                    if (updateState is UpdateCheckState.Available) {
                        uriHandler.openUri(updateState.release.pageUrl)
                    } else {
                        viewModel.checkForUpdates()
                    }
                },
            )
        }
        item {
            ProfileActionCard(
                title = "关于",
                summary = "github.com/KiritoXDone/Miaopu · ${viewModel.currentVersion}",
                action = "查看 ›",
                onClick = { uriHandler.openUri(viewModel.repositoryUrl) },
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    summary: String,
    action: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .semantics {
                role = Role.Button
                contentDescription = "$title，$summary"
            },
        insideMargin = PaddingValues(18.dp),
        cornerRadius = 20.dp,
        onClick = onClick.takeIf { enabled },
        pressFeedbackType = if (enabled) PressFeedbackType.Sink else PressFeedbackType.None,
        showIndication = enabled,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    summary,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                action,
                style = MiuixTheme.textStyles.footnote1,
                color = if (enabled) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun pagePadding(innerPadding: PaddingValues) = PaddingValues(
    top = innerPadding.calculateTopPadding(),
    bottom = innerPadding.calculateBottomPadding() + 16.dp,
)
