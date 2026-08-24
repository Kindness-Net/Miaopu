package dev.kiritoxd.miaopu.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class GitHubRelease(
    val tagName: String,
    val title: String,
    val pageUrl: String,
    val publishedAt: String?,
)

class GitHubReleaseAdapter {
    suspend fun latest(): AdapterResult<GitHubRelease> = withContext(Dispatchers.IO) {
        val source = "github.release.latest"
        val connection = (URL(LATEST_RELEASE_API).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 12_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "Miaopu-Android")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            when {
                status == HttpURLConnection.HTTP_NOT_FOUND -> AdapterResult.failure(
                    source = source,
                    status = AdapterStatus.NOT_CONFIGURED,
                    message = "仓库暂未发布 Release",
                    retryable = false,
                    httpCode = status,
                )
                status !in 200..299 -> AdapterResult.failure(
                    source = source,
                    status = AdapterStatus.TRANSIENT_FAILURE,
                    message = "GitHub 更新服务暂时不可用",
                    retryable = true,
                    httpCode = status,
                )
                else -> runCatching { parseGitHubRelease(body) }
                    .fold(
                        onSuccess = { AdapterResult.success(source, it) },
                        onFailure = {
                            AdapterResult.failure(
                                source = source,
                                status = AdapterStatus.INVALID_RESPONSE,
                                message = it.message ?: "GitHub Release 响应格式已变化",
                                retryable = false,
                                httpCode = status,
                            )
                        },
                    )
            }
        } catch (error: IOException) {
            AdapterResult.failure(
                source = source,
                status = AdapterStatus.TRANSIENT_FAILURE,
                message = error.message ?: "无法连接 GitHub",
                retryable = true,
            )
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val REPOSITORY_URL = "https://github.com/KiritoXDone/Miaopu"
        const val LATEST_RELEASE_API = "https://api.github.com/repos/KiritoXDone/Miaopu/releases/latest"
    }
}

internal fun parseGitHubRelease(json: String): GitHubRelease {
    val source = JSONObject(json)
    val tagName = source.optString("tag_name").takeIf(String::isNotBlank)
        ?: error("Release 缺少 tag_name")
    val pageUrl = source.optString("html_url").takeIf(String::isNotBlank)
        ?: error("Release 缺少 html_url")
    return GitHubRelease(
        tagName = tagName,
        title = source.optString("name").takeIf(String::isNotBlank) ?: tagName,
        pageUrl = pageUrl,
        publishedAt = source.optString("published_at").takeIf(String::isNotBlank),
    )
}

internal fun isNewerVersion(latest: String, current: String): Boolean {
    val latestParts = latest.versionParts()
    val currentParts = current.versionParts()
    val count = maxOf(latestParts.size, currentParts.size)
    for (index in 0 until count) {
        val latestPart = latestParts.getOrElse(index) { 0 }
        val currentPart = currentParts.getOrElse(index) { 0 }
        if (latestPart != currentPart) return latestPart > currentPart
    }
    return false
}

private fun String.versionParts(): List<Int> =
    trim().removePrefix("v").removePrefix("V")
        .split('.', '-', '_')
        .mapNotNull { part -> part.takeWhile(Char::isDigit).toIntOrNull() }
        .ifEmpty { listOf(0) }
