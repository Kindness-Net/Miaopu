package dev.kiritoxd.miaopu.data

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

internal sealed interface CommentImageSaveResult {
    data class Saved(val location: String) : CommentImageSaveResult
    data class Failed(val message: String) : CommentImageSaveResult
}

internal suspend fun saveCommentImage(
    context: Context,
    imageUrl: String,
): CommentImageSaveResult = withContext(Dispatchers.IO) {
    try {
        saveCommentImageBlocking(context, imageUrl)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        CommentImageSaveResult.Failed(error.message ?: "图片保存失败")
    }
}

private fun saveCommentImageBlocking(context: Context, imageUrl: String): CommentImageSaveResult {
    val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 30_000
        instanceFollowRedirects = true
        setRequestProperty("User-Agent", "Miaopu/${Build.VERSION.SDK_INT}")
    }
    return try {
        val responseCode = connection.responseCode
        check(responseCode in 200..299) { "图片下载失败（HTTP $responseCode）" }
        val mimeType = connection.contentType
            ?.substringBefore(';')
            ?.trim()
            ?.takeIf { it.startsWith("image/") }
            ?: imageMimeType(imageUrl)
        val extension = imageExtension(mimeType)
        val displayName = "miaopu_${System.currentTimeMillis()}.$extension"
        connection.inputStream.use { input ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(context, displayName, mimeType) { output -> input.copyTo(output) }
            } else {
                saveToAppPictures(context, displayName, mimeType) { output -> input.copyTo(output) }
            }
        }
    } finally {
        connection.disconnect()
    }
}

private fun saveToMediaStore(
    context: Context,
    displayName: String,
    mimeType: String,
    write: (java.io.OutputStream) -> Unit,
): CommentImageSaveResult {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Miaopu")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: error("无法创建图片文件")
    return try {
        resolver.openOutputStream(uri, "w")?.use(write) ?: error("无法写入图片文件")
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
            null,
            null,
        )
        CommentImageSaveResult.Saved("相册的 Miaopu 文件夹")
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}

@Suppress("DEPRECATION")
private fun saveToAppPictures(
    context: Context,
    displayName: String,
    mimeType: String,
    write: (java.io.OutputStream) -> Unit,
): CommentImageSaveResult {
    val pictures = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ?: error("外部图片目录不可用")
    val directory = File(pictures, "Miaopu")
    check(directory.exists() || directory.mkdirs()) { "无法创建图片目录" }
    val file = File(directory, displayName)
    try {
        FileOutputStream(file).use(write)
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mimeType), null)
        return CommentImageSaveResult.Saved("设备图片目录")
    } catch (error: Throwable) {
        file.delete()
        throw error
    }
}

internal fun imageMimeType(imageUrl: String): String = when (
    imageUrl.substringBefore('?').substringAfterLast('.', missingDelimiterValue = "").lowercase()
) {
    "png" -> "image/png"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    else -> "image/jpeg"
}

private fun imageExtension(mimeType: String): String = when (mimeType.lowercase()) {
    "image/png" -> "png"
    "image/gif" -> "gif"
    "image/webp" -> "webp"
    else -> "jpg"
}
