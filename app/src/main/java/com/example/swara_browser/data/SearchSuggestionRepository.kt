package com.example.swara_browser.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object SearchSuggestionRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val trendingTags = listOf(
        "🔥 Trending India News",
        "🔥 Tech & AI Innovations",
        "🔥 Live Cricket Scores",
        "🔥 Indian Market Trends",
        "🔥 Swara Browser Features"
    )

    suspend fun fetchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext trendingTags
        }

        return@withContext try {
            val url = "https://suggestqueries.google.com/complete/search?client=firefox&q=${Uri.encode(cleanQuery)}"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.isNotBlank()) {
                    val jsonArray = JSONArray(bodyStr)
                    if (jsonArray.length() >= 2) {
                        val suggestionsArray = jsonArray.getJSONArray(1)
                        val results = mutableListOf<String>()
                        for (i in 0 until suggestionsArray.length()) {
                            results.add(suggestionsArray.getString(i))
                        }
                        if (results.isNotEmpty()) return@withContext results
                    }
                }
            }
            LocalAutocompleteEngine.getSuggestions(cleanQuery)
        } catch (_: Exception) {
            LocalAutocompleteEngine.getSuggestions(cleanQuery)
        }
    }
}
