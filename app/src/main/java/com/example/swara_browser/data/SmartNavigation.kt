package com.example.swara_browser.data

import android.webkit.WebBackForwardList
import android.webkit.WebView

object SmartNavigation {

    /**
     * Inspects the history stack backwards and skips tracking/ad hops
     * and duplicate pushState fragments to avoid loop traps.
     */
    fun smartGoBack(webView: WebView): Boolean {
        val history: WebBackForwardList = webView.copyBackForwardList()
        val currentIndex = history.currentIndex
        if (currentIndex <= 0) return false

        val currentItem = history.getItemAtIndex(currentIndex) ?: return false
        val currentUrl = currentItem.url.orEmpty()
        val cleanCurrentUrl = stripUrlNoise(currentUrl)

        var stepsBack = -1

        for (i in (currentIndex - 1) downTo 0) {
            val item = history.getItemAtIndex(i) ?: continue
            val itemUrl = item.url.orEmpty()
            val cleanItemUrl = stripUrlNoise(itemUrl)

            val isDuplicate = cleanItemUrl == cleanCurrentUrl
            val isTrackerOrRedirect = isRedirectPattern(itemUrl)

            if (!isDuplicate && !isTrackerOrRedirect) {
                stepsBack = i - currentIndex
                break
            }
        }

        if (webView.canGoBackOrForward(stepsBack)) {
            webView.goBackOrForward(stepsBack)
            return true
        }

        return false
    }

    private fun stripUrlNoise(url: String): String {
        return url.substringBefore("#").substringBefore("?")
    }

    private fun isRedirectPattern(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("/aclk?") ||
                lower.contains("google.com/url?") ||
                lower.contains("/pagead/") ||
                lower.contains("&adurl=") ||
                lower.contains("/interstitial") ||
                lower.contains("doubleclick.net")
    }
}
