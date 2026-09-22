package com.example.swara_browser.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

sealed class ScreenshotResult {
    data class Success(val uri: Uri, val fileName: String) : ScreenshotResult()
    data class Failure(val error: String) : ScreenshotResult()
}

object ScreenshotCaptureEngine {

    /**
     * Renders the entire scrollable height of the WebView to a Bitmap and saves to Pictures/Swara.
     */
    suspend fun captureFullPage(
        context: Context,
        webView: WebView
    ): ScreenshotResult = withContext(Dispatchers.IO) {
        try {
            val contentWidth = webView.width
            val contentHeight = (webView.contentHeight * webView.scale).toInt()

            if (contentWidth <= 0 || contentHeight <= 0) {
                return@withContext ScreenshotResult.Failure("Invalid page dimensions")
            }

            // Cap maximum height to prevent OutOfMemory on extremely long infinite-scroll feeds
            val safeHeight = contentHeight.coerceAtMost(16000)

            val bitmap = Bitmap.createBitmap(contentWidth, safeHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            withContext(Dispatchers.Main) {
                webView.draw(canvas)
            }

            val fileName = "SWARA_SHOT_${System.currentTimeMillis()}.png"
            val savedUri = saveBitmapToMediaStore(context, bitmap, fileName)

            bitmap.recycle()

            if (savedUri != null) {
                ScreenshotResult.Success(savedUri, fileName)
            } else {
                ScreenshotResult.Failure("Failed to write screenshot to storage")
            }
        } catch (_: OutOfMemoryError) {
            ScreenshotResult.Failure("Page too large to capture in full resolution (OOM).")
        } catch (e: Exception) {
            ScreenshotResult.Failure(e.localizedMessage ?: "Screenshot capture failed")
        }
    }

    private fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Swara")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        return try {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }
}
