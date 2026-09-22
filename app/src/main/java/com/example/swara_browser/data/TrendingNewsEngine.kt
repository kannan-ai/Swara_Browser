package com.example.swara_browser.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val category: NewsCategory,
    val timeAgo: String,
    val url: String,
    val imageUrl: String = "",
    val badgeLabel: String = ""
)

enum class NewsCategory(val displayName: String) {
    TOP_INDIA("🇮🇳 India Top"),
    FOOD_SAFETY("🥗 Food Safety"),
    FACT_CHECK("🛡️ Fact Check"),
    TECH_HARDWARE("💻 Tech & RAM"),
    WAR_GEOPOLITICS("🌍 War News"),
    REVEALED_TRUTHS("👁️ Revealed Truth")
}

object TrendingNewsEngine {

    fun stripTrackingParameters(url: String): String {
        if (url.isBlank()) return ""
        return try {
            val queryStart = url.indexOf('?')
            if (queryStart == -1) return url
            val baseUrl = url.substring(0, queryStart)
            val queryString = url.substring(queryStart + 1)
            val fragmentStart = queryString.indexOf('#')
            val query = if (fragmentStart != -1) queryString.substring(0, fragmentStart) else queryString
            val fragment = if (fragmentStart != -1) queryString.substring(fragmentStart) else ""

            val params = query.split("&")
            val cleanParams = params.filter { p ->
                val paramName = p.substringBefore("=").lowercase()
                !paramName.startsWith("utm_") &&
                paramName != "ref" &&
                paramName != "fbclid" &&
                paramName != "gclid" &&
                paramName != "gws_rd" &&
                paramName != "ncid" &&
                paramName != "cmp" &&
                paramName != "mc_cid" &&
                paramName != "mc_eid"
            }

            if (cleanParams.isEmpty()) {
                baseUrl + fragment
            } else {
                "$baseUrl?${cleanParams.joinToString("&")}$fragment"
            }
        } catch (_: Exception) {
            url
        }
    }

    suspend fun resolveCanonicalNewsUrl(rawUrl: String): String = withContext(Dispatchers.IO) {
        val cleanUrl = stripTrackingParameters(rawUrl)
        if (!cleanUrl.contains("news.google.com/rss/articles/") && !cleanUrl.contains("google.com/url")) {
            return@withContext cleanUrl
        }

        return@withContext try {
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val finalUrl = response.request.url.toString()
            response.close()
            stripTrackingParameters(finalUrl)
        } catch (_: Exception) {
            cleanUrl
        }
    }

    fun filterValidImageUrl(url: String): String {
        if (url.isBlank()) return ""
        val lower = url.lowercase()
        val isPlaceholder = lower.contains("placeholder") ||
                lower.contains("default_share") ||
                lower.contains("logo") ||
                lower.contains("banner") ||
                lower.contains("brand") ||
                lower.contains("avatar") ||
                lower.contains("favicon") ||
                lower.contains("icon")
        return if (isPlaceholder) "" else url
    }

    suspend fun fetchLiveRssNews(): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://news.google.com/rss?hl=en-IN&gl=IN&ceid=IN:en")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0")

            if (conn.responseCode == 200) {
                val liveItems = mutableListOf<NewsItem>()
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(conn.inputStream, "UTF-8")

                var eventType = parser.eventType
                var currentTitle = ""
                var currentLink = ""
                var currentPubDate = ""
                var currentSource = ""
                var currentMediaUrl = ""
                var insideItem = false

                while (eventType != XmlPullParser.END_DOCUMENT && liveItems.size < 20) {
                    val name = parser.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (name != null && name.equals("item", ignoreCase = true)) {
                                insideItem = true
                                currentTitle = ""
                                currentLink = ""
                                currentPubDate = ""
                                currentSource = ""
                                currentMediaUrl = ""
                            } else if (insideItem && name != null) {
                                when (name.lowercase()) {
                                    "title" -> currentTitle = parser.nextText()
                                    "link" -> currentLink = parser.nextText()
                                    "pubdate" -> currentPubDate = parser.nextText()
                                    "source" -> currentSource = parser.nextText()
                                    "media:content", "enclosure" -> {
                                        val mediaUrl = parser.getAttributeValue(null, "url") ?: ""
                                        if (mediaUrl.isNotBlank()) {
                                            currentMediaUrl = filterValidImageUrl(mediaUrl)
                                        }
                                    }
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (name != null && name.equals("item", ignoreCase = true) && insideItem) {
                                insideItem = false
                                if (currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                    val cleanSource = if (currentSource.isNotBlank()) currentSource else extractSourceFromTitle(currentTitle)
                                    val cleanTitle = currentTitle.substringBefore(" - ").trim()
                                    val cleanArticleLink = stripTrackingParameters(currentLink)

                                    liveItems.add(
                                        NewsItem(
                                            id = "live_${cleanArticleLink.hashCode()}",
                                            title = cleanTitle,
                                            source = cleanSource,
                                            category = NewsCategory.TOP_INDIA,
                                            timeAgo = "Live",
                                            url = cleanArticleLink,
                                            imageUrl = currentMediaUrl,
                                            badgeLabel = "🇮🇳 Live Google News"
                                        )
                                    )
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
                conn.disconnect()
                if (liveItems.isNotEmpty()) {
                    return@withContext liveItems + getDefaultCombinedFeed().take(5)
                }
            }
        } catch (_: Exception) {
            // Fallback to local combined feed if offline
        }
        return@withContext getDefaultCombinedFeed()
    }

    private fun extractSourceFromTitle(title: String): String {
        val parts = title.split(" - ")
        return if (parts.size > 1) parts.last().trim() else "Google News"
    }

    fun getDefaultCombinedFeed(): List<NewsItem> {
        return listOf(
            NewsItem(
                id = "fs_1",
                title = "FSSAI Food Safety Audit Uncovers Unregistered Chemical Additives in Popular Food Brands",
                source = "Food Safety Watch",
                category = NewsCategory.FOOD_SAFETY,
                timeAgo = "10m ago",
                url = "https://www.fssai.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=500",
                badgeLabel = "🥗 Food Safety"
            ),
            NewsItem(
                id = "rt_1",
                title = "Data Privacy Audit Uncovers Massive Unadvertised Telecom Tracking Networks",
                source = "Public Interest Report",
                category = NewsCategory.REVEALED_TRUTHS,
                timeAgo = "15m ago",
                url = "https://www.eff.org/issues/privacy",
                imageUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
                badgeLabel = "👁️ Revealed Truth"
            ),
            NewsItem(
                id = "th_1",
                title = "Global Semiconductor Supply Shift Causes 25% Memory & RAM Price Surge",
                source = "Hardware Benchmark",
                category = NewsCategory.TECH_HARDWARE,
                timeAgo = "30m ago",
                url = "https://arstechnica.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=500",
                badgeLabel = "💻 Tech & RAM"
            ),
            NewsItem(
                id = "fc_1",
                title = "Fact Check: Viral Internet Investment Scheme Debunked as Fraudulent Bot Network",
                source = "Fact Check India",
                category = NewsCategory.FACT_CHECK,
                timeAgo = "45m ago",
                url = "https://www.pib.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
                badgeLabel = "🛡️ Fact Check"
            ),
            NewsItem(
                id = "wg_1",
                title = "Global Security Briefing: International Defense Readiness & Border Patrols Monitored",
                source = "World Security Feed",
                category = NewsCategory.WAR_GEOPOLITICS,
                timeAgo = "50m ago",
                url = "https://www.reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                badgeLabel = "🌍 War News"
            ),
            NewsItem(
                id = "rt_2",
                title = "Whistleblower Audit Exposes Hidden Surcharges & Automated Banking Deductions",
                source = "Consumer Watchdog",
                category = NewsCategory.REVEALED_TRUTHS,
                timeAgo = "1h ago",
                url = "https://www.consumerfinance.gov",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500",
                badgeLabel = "👁️ Revealed Truth"
            ),
            NewsItem(
                id = "th_2",
                title = "Flagship AI Smartphone & Foldable Gadget Launch Announced with Next-Gen Chips",
                source = "TechCrunch",
                category = NewsCategory.TECH_HARDWARE,
                timeAgo = "1h ago",
                url = "https://techcrunch.com",
                imageUrl = "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500",
                badgeLabel = "💻 Tech & RAM"
            ),
            NewsItem(
                id = "fs_2",
                title = "Surprise Restaurant Inspection Reveals Unhygienic Storage Practices in Major Metro Outlets",
                source = "Public Health Alert",
                category = NewsCategory.FOOD_SAFETY,
                timeAgo = "1h ago",
                url = "https://www.fssai.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=500",
                badgeLabel = "🥗 Food Safety"
            ),
            NewsItem(
                id = "in_1",
                title = "India's Tech & AI Mission Boosts Digital Infrastructure Across States",
                source = "The Hindu",
                category = NewsCategory.TOP_INDIA,
                timeAgo = "1h ago",
                url = "https://www.thehindu.com/news/national/",
                imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500",
                badgeLabel = "🇮🇳 India Top"
            ),
            NewsItem(
                id = "fc_2",
                title = "Fact Check: Manipulated AI Audio Clip of Public Official Proven Synthetic Deepfake",
                source = "AI Safety Labs",
                category = NewsCategory.FACT_CHECK,
                timeAgo = "2h ago",
                url = "https://www.eff.org/issues/ai",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=500",
                badgeLabel = "🛡️ Fact Check"
            )
        )
    }

    fun getNewsForCategory(category: NewsCategory): List<NewsItem> {
        return getDefaultCombinedFeed().filter { it.category == category }
    }
}
