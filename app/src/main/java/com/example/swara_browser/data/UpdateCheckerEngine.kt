package com.example.swara_browser.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object UpdateCheckerEngine {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Returns Pair(VersionTag, DownloadUrl)
    suspend fun getLatestRelease(): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/kannan-ai/Swara_Browser/releases/latest")
                .header("User-Agent", "SwaraBrowser-App")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.isNotBlank()) {
                    val json = JSONObject(bodyStr)
                    val tagName = json.optString("tag_name", "")
                    val htmlUrl = json.optString("html_url", "")
                    if (tagName.isNotBlank() && htmlUrl.isNotBlank()) {
                        return@withContext Pair(tagName, htmlUrl)
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
