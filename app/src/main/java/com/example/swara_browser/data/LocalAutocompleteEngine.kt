package com.example.swara_browser.data

object LocalAutocompleteEngine {

    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isEndOfWord = false
        var fullWord: String? = null
    }

    private val root = TrieNode()

    private val initialDictionary = listOf(
        "flipkart.com",
        "amazon.in",
        "irctc.co.in",
        "cricbuzz.com",
        "swiggy.com",
        "zomato.com",
        "meesho.com",
        "hotstar.com",
        "timesofindia.indiatimes.com",
        "ndtv.com",
        "paytm.com",
        "jiocinema.com",
        "bookmyshow.com",
        "google.co.in",
        "youtube.com",
        "wikipedia.org",
        "github.com",
        "reddit.com",
        "twitter.com",
        "instagram.com",
        "facebook.com",
        "quora.com",
        "pinterest.com",
        "linkedin.com",
        "bing.com",
        "duckduckgo.com",
        "brave.com",
        "yahoo.com",
        "ecosia.org",
        "startpage.com"
    )

    init {
        initialDictionary.forEach { insert(it) }
    }

    fun getTrendingSearches(): List<String> = listOf(
        "🔥 Trending India News",
        "🔥 Tech & AI Innovations",
        "🔥 Live Cricket Scores",
        "🔥 Indian Market Trends",
        "🔥 Swara Browser Features"
    )

    fun insert(word: String) {
        val lower = word.lowercase().trim()
        if (lower.isBlank()) return
        var current = root
        for (ch in lower) {
            current = current.children.getOrPut(ch) { TrieNode() }
        }
        current.isEndOfWord = true
        current.fullWord = lower
    }

    fun getSuggestions(prefix: String, limit: Int = 5): List<String> {
        val query = prefix.lowercase().trim()
        if (query.isBlank()) return emptyList()

        var current = root
        for (ch in query) {
            current = current.children[ch] ?: return emptyList()
        }

        val results = mutableListOf<String>()
        collectWords(current, results, limit)
        return results
    }

    private fun collectWords(node: TrieNode, results: MutableList<String>, limit: Int) {
        if (results.size >= limit) return
        if (node.isEndOfWord) {
            node.fullWord?.let { results.add(it) }
        }
        for (child in node.children.values) {
            if (results.size >= limit) break
            collectWords(child, results, limit)
        }
    }
}
