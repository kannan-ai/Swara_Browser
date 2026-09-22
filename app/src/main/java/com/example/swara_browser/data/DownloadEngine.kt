package com.example.swara_browser.data

import android.content.Context
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Failure(val errorMessage: String) : DownloadResult()
}

sealed class DownloadStatus {
    object Pending : DownloadStatus()
    object Downloading : DownloadStatus()
    object Completed : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
}

data class DownloadItem(
    val id: String,
    val url: String,
    val fileName: String,
    val progress: Int,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedMbPerSec: String,
    val status: DownloadStatus
)

object DownloadEngine {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val _aggregateSpeedMbPerSec = MutableStateFlow("0.0 MB/s")
    val aggregateSpeedMbPerSec: StateFlow<String> = _aggregateSpeedMbPerSec.asStateFlow()

    suspend fun downloadUrl(
        context: Context,
        url: String,
        pageReferer: String?,
        userAgent: String,
        onProgress: (percent: Int) -> Unit = {}
    ): DownloadResult = withContext(Dispatchers.IO) {
        if (url.startsWith("data:image/")) {
            val res = ProtectedImageSaver.saveBase64Payload(url)
            return@withContext if (res.isSuccess) {
                DownloadResult.Success(res.getOrThrow())
            } else {
                DownloadResult.Failure(res.exceptionOrNull()?.localizedMessage ?: "Failed base64 save")
            }
        }

        var tempFile: File? = null
        try {
            val cookie = CookieManager.getInstance().getCookie(url)
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent.ifBlank { "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36" })

            if (!cookie.isNullOrBlank()) {
                requestBuilder.header("Cookie", cookie)
            }
            if (!pageReferer.isNullOrBlank()) {
                requestBuilder.header("Referer", pageReferer)
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful || response.body == null) {
                val code = response.code
                response.close()
                return@withContext DownloadResult.Failure("Server rejected transfer: HTTP $code")
            }

            val disposition = response.header("Content-Disposition")
            val contentType = response.header("Content-Type")
            val rawFileName = URLUtil.guessFileName(url, disposition, contentType)
            val fileName = sanitizeFileName(rawFileName)

            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            tempFile = File(downloadDir, fileName)

            val responseBody = response.body!!
            val contentLength = responseBody.contentLength()
            var totalBytesRead: Long = 0

            responseBody.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        totalBytesRead += read
                        if (contentLength > 0) {
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            if (totalBytesRead <= 0L || !tempFile.exists() || tempFile.length() <= 0L) {
                tempFile.delete()
                return@withContext DownloadResult.Failure("Transfer produced 0 bytes. File discarded.")
            }

            return@withContext DownloadResult.Success(tempFile)
        } catch (e: Exception) {
            tempFile?.delete()
            return@withContext DownloadResult.Failure(e.localizedMessage ?: "Unknown streaming error")
        }
    }

    fun enqueueDownload(
        context: Context,
        downloadUrl: String,
        pageUrl: String,
        userAgent: String,
        contentDisposition: String? = null,
        mimeType: String? = null
    ) {
        if (downloadUrl.startsWith("data:image/")) {
            scope.launch {
                val res = ProtectedImageSaver.saveBase64Payload(downloadUrl)
                withContext(Dispatchers.Main) {
                    if (res.isSuccess) {
                        Toast.makeText(context, "Saved image to Pictures/Swara! 🖼️", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return
        }

        val downloadId = "dl_${System.currentTimeMillis()}"
        val initialFileName = parseFileName(downloadUrl, contentDisposition, mimeType)

        val initialItem = DownloadItem(
            id = downloadId,
            url = downloadUrl,
            fileName = initialFileName,
            progress = 0,
            totalBytes = 0L,
            downloadedBytes = 0L,
            speedMbPerSec = "0.0 MB/s",
            status = DownloadStatus.Pending
        )

        _downloads.value = _downloads.value + initialItem
        Toast.makeText(context, "📥 Download Enqueued: $initialFileName", Toast.LENGTH_SHORT).show()

        scope.launch {
            val res = downloadUrl(context, downloadUrl, pageUrl, userAgent, {})
            if (res is DownloadResult.Success) {
                updateItemStatus(downloadId) {
                    it.copy(
                        fileName = res.file.name,
                        progress = 100,
                        totalBytes = res.file.length(),
                        downloadedBytes = res.file.length(),
                        speedMbPerSec = "Completed",
                        status = DownloadStatus.Completed
                    )
                }
            } else if (res is DownloadResult.Failure) {
                updateItemStatus(downloadId) {
                    it.copy(status = DownloadStatus.Failed(res.errorMessage))
                }
            }
        }
    }

    fun saveBase64Image(context: Context, dataUri: String): Boolean {
        scope.launch {
            val res = ProtectedImageSaver.saveBase64Payload(dataUri)
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    Toast.makeText(context, "Saved image to Pictures/Swara! 🖼️", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    private fun updateItemStatus(id: String, transform: (DownloadItem) -> DownloadItem) {
        _downloads.value = _downloads.value.map { if (it.id == id) transform(it) else it }
    }

    fun removeDownload(id: String) {
        _downloads.value = _downloads.value.filter { it.id != id }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun parseFileName(url: String, disposition: String?, mimeType: String?): String {
        if (!disposition.isNullOrBlank()) {
            val matcher = Pattern.compile("filename\\*?=['\"]?(?:UTF-8'')?([^'\";]+)['\"]?", Pattern.CASE_INSENSITIVE).matcher(disposition)
            if (matcher.find()) {
                val found = matcher.group(1)
                if (!found.isNullOrBlank()) return sanitizeFileName(found)
            }
        }
        return sanitizeFileName(URLUtil.guessFileName(url, disposition, mimeType))
    }
}
