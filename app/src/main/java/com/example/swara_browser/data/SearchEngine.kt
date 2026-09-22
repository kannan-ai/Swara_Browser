package com.example.swara_browser.data

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class SearchEngine(
    val displayName: String,
    val homeUrl: String,
    val searchPrefix: String
) {
    GOOGLE(
        displayName = "Google India",
        homeUrl = "https://www.google.co.in",
        searchPrefix = "https://www.google.co.in/search?gl=in&hl=en&q="
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo India",
        homeUrl = "https://duckduckgo.com/?kl=in-en",
        searchPrefix = "https://duckduckgo.com/?kl=in-en&q="
    ),
    BING(
        displayName = "Bing India",
        homeUrl = "https://www.bing.com/?cc=IN",
        searchPrefix = "https://www.bing.com/search?cc=IN&q="
    ),
    BRAVE(
        displayName = "Brave Search",
        homeUrl = "https://search.brave.com",
        searchPrefix = "https://search.brave.com/search?q="
    ),
    YAHOO(
        displayName = "Yahoo India",
        homeUrl = "https://in.search.yahoo.com",
        searchPrefix = "https://in.search.yahoo.com/search?q="
    ),
    ECOSIA(
        displayName = "Ecosia India",
        homeUrl = "https://www.ecosia.org",
        searchPrefix = "https://www.ecosia.org/search?q="
    ),
    STARTPAGE(
        displayName = "Startpage",
        homeUrl = "https://www.startpage.com",
        searchPrefix = "https://www.startpage.com/sp/search?query="
    );

    fun buildUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return homeUrl

        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed
        }

        val domainRegex = Regex("""^([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(/.*)?$""")
        val localhostRegex = Regex("""^localhost(:[0-9]+)?(/.*)?$""", RegexOption.IGNORE_CASE)
        val ipRegex = Regex("""^([0-9]{1,3}\.){3}[0-9]{1,3}(:[0-9]+)?(/.*)?$""")

        if (domainRegex.matches(trimmed) || ipRegex.matches(trimmed)) {
            return "https://$trimmed"
        }
        if (localhostRegex.matches(trimmed)) {
            return "http://$trimmed"
        }

        val encodedQuery = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name())
        return "$searchPrefix$encodedQuery"
    }
}
