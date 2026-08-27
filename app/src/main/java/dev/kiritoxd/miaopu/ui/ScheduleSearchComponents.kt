package dev.kiritoxd.miaopu.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kiritoxd.miaopu.data.MatchSummary
import dev.kiritoxd.miaopu.data.Schedule
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar

@Composable
internal fun EventsScheduleSearchBar(
    expanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        inputField = {
            InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onQueryChange,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                label = "搜索赛事、轮次、战队或选手",
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {}
}

@Composable
internal fun EventsScheduleSearchResults(
    schedule: Schedule,
    query: String,
    bottomPadding: Dp,
    onMatchClick: (MatchSummary) -> Unit,
) {
    if (schedule.days.isEmpty()) {
        EmptyPane(
            message = "没有找到“${query.trim()}”相关赛程",
            modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
    ) {
        schedule.days.forEach { day ->
            item(key = "search-day-${day.date}", contentType = "day") {
                ScheduleDayBand(day = day, isFocused = false)
            }
            itemsIndexed(
                items = day.matches,
                key = { index, match -> "search-${day.date}-${match.uniqueKey}-$index" },
                contentType = { _, _ -> "schedule-match" },
            ) { _, match ->
                HupuScheduleMatchCard(match = match, onClick = { onMatchClick(match) })
            }
        }
    }
}
