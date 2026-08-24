package dev.kiritoxd.miaopu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun MiaopuApp(viewModel: MiaopuViewModel) {
    val themeController = remember { ThemeController(ColorSchemeMode.System) }
    MiuixTheme(controller = themeController) {
        Box(Modifier.fillMaxSize()) {
            when (val screen = viewModel.screen) {
                AppScreen.Schedule -> ScheduleScreen(viewModel)
                AppScreen.Subscriptions -> SubscriptionScreen(viewModel)
                is AppScreen.Ratings -> RatingsScreen(viewModel, screen.match)
                is AppScreen.Stage -> MatchStageScreen(
                    viewModel = viewModel,
                    match = screen.match,
                    stage = screen.stage,
                    stageNumber = screen.stageNumber,
                    showStageNumber = screen.returnToStagePicker,
                )
                is AppScreen.Comments -> CommentsScreen(viewModel, screen.target)
                is AppScreen.Web -> HupuWebScreen(
                    title = screen.title,
                    url = screen.url,
                    login = screen.login,
                    onBack = viewModel::goBack,
                    onLoginDetected = viewModel::finishLogin,
                )
            }

            AnimatedVisibility(
                visible = viewModel.message != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(16.dp),
            ) {
                Card(
                    onClick = viewModel::dismissMessage,
                    showIndication = true,
                    insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    cornerRadius = 18.dp,
                ) {
                    Text(text = viewModel.message.orEmpty())
                }
            }
        }
    }
}
