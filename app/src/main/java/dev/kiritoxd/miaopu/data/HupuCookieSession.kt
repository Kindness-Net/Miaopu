package dev.kiritoxd.miaopu.data

import android.content.Context
import android.webkit.CookieManager

class HupuCookieSession(context: Context) {
    private val secureStore = SecureCookieStore(context.applicationContext)
    private val cookieManager = CookieManager.getInstance()

    init {
        cookieManager.setAcceptCookie(true)
    }

    fun restore() {
        val stored = secureStore.read().orEmpty()
        if (stored.isBlank()) return
        splitCookieHeader(stored).forEach { cookie ->
            HupuUrls.cookieOrigins.forEach { origin -> cookieManager.setCookie(origin, cookie) }
        }
        cookieManager.flush()
    }

    fun capture(): Boolean {
        val merged = linkedMapOf<String, String>()
        HupuUrls.cookieOrigins.forEach { origin ->
            splitCookieHeader(cookieManager.getCookie(origin).orEmpty()).forEach { cookie ->
                val name = cookie.substringBefore('=').trim()
                if (name.isNotBlank()) merged[name] = cookie
            }
        }
        if (merged.isEmpty()) return false
        val header = merged.values.joinToString("; ")
        val authenticated = isAuthenticated(header)
        if (authenticated) secureStore.write(header)
        cookieManager.flush()
        return authenticated
    }

    fun cookieHeader(): String = HupuUrls.cookieOrigins
        .asSequence()
        .mapNotNull(cookieManager::getCookie)
        .firstOrNull(::isAuthenticated)
        ?: secureStore.read().orEmpty()

    fun isAuthenticated(): Boolean = isAuthenticated(cookieHeader())

    fun clear(onCleared: () -> Unit = {}) {
        secureStore.clear()
        cookieManager.removeAllCookies {
            cookieManager.flush()
            onCleared()
        }
    }

    companion object {
        fun splitCookieHeader(header: String): List<String> = header
            .split(';')
            .map(String::trim)
            .filter { it.contains('=') && it.substringBefore('=').isNotBlank() }

        fun isAuthenticated(header: String): Boolean = splitCookieHeader(header)
            .any { it.substringBefore('=').trim().equals("ua", ignoreCase = true) && it.substringAfter('=').isNotBlank() }
    }
}
