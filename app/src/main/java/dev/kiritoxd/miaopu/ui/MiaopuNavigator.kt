package dev.kiritoxd.miaopu.ui

import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.navBackStackOf

internal class MiaopuNavigator(
    initialScreen: AppScreen = AppScreen.Schedule,
) {
    val backStack: NavBackStack = navBackStackOf(initialScreen)

    val currentScreen: AppScreen
        get() = backStack.last() as AppScreen

    fun push(screen: AppScreen) {
        if (backStack.none { (it as AppScreen).navigationContentKey == screen.navigationContentKey }) {
            backStack.add(screen)
        }
    }

    fun replace(screen: AppScreen) {
        if (currentScreen != screen) backStack[backStack.lastIndex] = screen
    }

    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeLastOrNull()
        return true
    }
}

internal val AppScreen.navigationContentKey: String
    get() = when (this) {
        AppScreen.Schedule -> "schedule"
        AppScreen.Subscriptions -> "subscriptions"
        is AppScreen.Ratings -> "ratings:${match.id}"
        is AppScreen.Stage -> buildString {
            append("stage:")
            append(match.id)
            append(':')
            append(stage.outBizType ?: match.outBizType)
            append(':')
            append(stage.outBizNo ?: match.outBizNo)
            append(':')
            append(stage.nodeId ?: stage.name)
        }
        is AppScreen.Comments -> "comments:${target.outBizType}:${target.outBizNo}"
        is AppScreen.Web -> "web:$login:$url"
    }
