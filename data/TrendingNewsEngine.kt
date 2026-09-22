package com.example.swara_browser.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

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

    private val thumbnailPool = listOf(
        "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500",
        "https://images.unsplash.com/photo-1517976487492-5750f3195933?w=500",
        "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=500",
        "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500",
        "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=500",
        "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
        "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500",
        "https://images.unsplash.com/photo-1532601224476-15c79f2f7a51?w=500"
    )

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
                            } else if (insideItem && name != null) {
                                when (name.lowercase()) {
                                    "title" -> currentTitle = parser.nextText()
                                    "link" -> currentLink = parser.nextText()
                                    "pubdate" -> currentPubDate = parser.nextText()
                                    "source" -> currentSource = parser.nextText()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (name != null && name.equals("item", ignoreCase = true) && insideItem) {
                                insideItem = false
                                if (currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                    val cleanSource = if (currentSource.isNotBlank()) currentSource else extractSourceFromTitle(currentTitle)
                                    val cleanTitle = currentTitle.substringBefore(" - ").trim()

                                    liveItems.add(
                                        NewsItem(
                                            id = "live_${System.currentTimeMillis()}_${liveItems.size}",
                                            title = cleanTitle,
                                            source = cleanSource,
                                            category = NewsCategory.TOP_INDIA,
                                            timeAgo = "Live",
                                            url = currentLink,
                                            imageUrl = thumbnailPool[liveItems.size % thumbnailPool.size],
                                            badgeLabel = "🇮🇳 Top News"
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
        return if (parts.size > 1) parts.last().trim() else "Top News"
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
            ),
            NewsItem(
                id = "th_3",
                title = "DRAM & NAND Flash Contract Price Surge Drives Up Component Costs for Laptops",
                source = "Ars Technica",
                category = NewsCategory.TECH_HARDWARE,
                timeAgo = "2h ago",
                url = "https://arstechnica.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=500",
                badgeLabel = "💻 Tech & RAM"
            ),
            NewsItem(
                id = "wg_2",
                title = "Geopolitical Audit: International Maritime Trade Routes Secure After Escort Patrols",
                source = "Reuters",
                category = NewsCategory.WAR_GEOPOLITICS,
                timeAgo = "2h ago",
                url = "https://www.reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                badgeLabel = "🌍 War News"
            ),
            NewsItem(
                id = "rt_3",
                title = "Right to Information Audit Uncovers Suppressed Municipal Water Quality Reports",
                source = "RTI Investigative Feed",
                category = NewsCategory.REVEALED_TRUTHS,
                timeAgo = "3h ago",
                url = "https://rtionline.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1532601224476-15c79f2f7a51?w=500",
                badgeLabel = "👁️ Revealed Truth"
            ),
            NewsItem(
                id = "in_2",
                title = "ISRO Prepares Next Generation Space Science Launch Programs",
                source = "NDTV News",
                category = NewsCategory.TOP_INDIA,
                timeAgo = "3h ago",
                url = "https://www.ndtv.com/india",
                imageUrl = "https://images.unsplash.com/photo-1517976487492-5750f3195933?w=500",
                badgeLabel = "🇮🇳 India Top"
            ),
            NewsItem(
                id = "fs_3",
                title = "Pesticide Residue Inspection Triggers Food Standards Compliance Warning",
                source = "FSSAI News",
                category = NewsCategory.FOOD_SAFETY,
                timeAgo = "3h ago",
                url = "https://www.fssai.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=500",
                badgeLabel = "🥗 Food Safety"
            ),
            NewsItem(
                id = "th_4",
                title = "Next-Gen On-Device AI Models Revolutionize Mobile Performance",
                source = "Wired",
                category = NewsCategory.TECH_HARDWARE,
                timeAgo = "4h ago",
                url = "https://www.wired.com",
                imageUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
                badgeLabel = "💻 Tech & RAM"
            ),
            NewsItem(
                id = "fc_3",
                title = "Fact Check: Fake Government Subsidy Link Circling on Messaging Apps Scams Users",
                source = "Cyber Crime Alert",
                category = NewsCategory.FACT_CHECK,
                timeAgo = "4h ago",
                url = "https://cybercrime.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
                badgeLabel = "🛡️ Fact Check"
            ),
            NewsItem(
                id = "in_3",
                title = "Railways Expand Vande Bharat Network Across Major Corridors",
                source = "Times of India",
                category = NewsCategory.TOP_INDIA,
                timeAgo = "4h ago",
                url = "https://timesofindia.indiatimes.com/india",
                imageUrl = "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=500",
                badgeLabel = "🇮🇳 India Top"
            ),
            NewsItem(
                id = "wg_3",
                title = "Defense Technology Summit Focuses on Autonomous Drone Detection Systems",
                source = "Defense World",
                category = NewsCategory.WAR_GEOPOLITICS,
                timeAgo = "5h ago",
                url = "https://www.reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                badgeLabel = "🌍 War News"
            ),
            NewsItem(
                id = "rt_4",
                title = "Energy Grid Audit Exposes Hidden Commercial Tariff Surcharges",
                source = "Energy Transparency",
                category = NewsCategory.REVEALED_TRUTHS,
                timeAgo = "5h ago",
                url = "https://powermin.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500",
                badgeLabel = "👁️ Revealed Truth"
            ),
            NewsItem(
                id = "th_5",
                title = "Next-Gen GPU & Desktop Hardware Memory Requirements Double for 2025 Applications",
                source = "Tom's Hardware",
                category = NewsCategory.TECH_HARDWARE,
                timeAgo = "6h ago",
                url = "https://arstechnica.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=500",
                badgeLabel = "💻 Tech & RAM"
            ),
            NewsItem(
                id = "fs_4",
                title = "Adulterated Milk & Dairy Product Seizure Conducted in Nationwide Food Safety Drive",
                source = "Dairy Safety India",
                category = NewsCategory.FOOD_SAFETY,
                timeAgo = "6h ago",
                url = "https://www.fssai.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=500",
                badgeLabel = "🥗 Food Safety"
            ),
            NewsItem(
                id = "fc_4",
                title = "Fact Check: False Medical Cure Claims Disproved by Health Research Board",
                source = "Medical Fact Check",
                category = NewsCategory.FACT_CHECK,
                timeAgo = "7h ago",
                url = "https://www.icmr.gov.in",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=500",
                badgeLabel = "🛡️ Fact Check"
            ),
            NewsItem(
                id = "wg_4",
                title = "Diplomatic Peace Talks Progress Monitored by International Security Observers",
                source = "Global News",
                category = NewsCategory.WAR_GEOPOLITICS,
                timeAgo = "8h ago",
                url = "https://www.reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                badgeLabel = "🌍 War News"
            ),
            NewsItem(
                id = "in_4",
                title = "National Highway Infrastructure Expansion Accelerates Logistics Throughput",
                source = "Economic Times",
                category = NewsCategory.TOP_INDIA,
                timeAgo = "8h ago",
                url = "https://economictimes.indiatimes.com",
                imageUrl = "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=500",
                badgeLabel = "🇮🇳 India Top"
            )
        )
    }

    fun getNewsForCategory(category: NewsCategory): List<NewsItem> {
        return getDefaultCombinedFeed().filter { it.category == category }
    }
}
