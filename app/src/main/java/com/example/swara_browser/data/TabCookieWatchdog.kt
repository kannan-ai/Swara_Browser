package com.example.swara_browser.data

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TabCookieWatchdog {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val tabInactivityMap = mutableMapOf<String, Long>()

    fun recordTabActivity(tabId: String) {
        if (tabId.isBlank()) return
        tabInactivityMap[tabId] = System.currentTimeMillis()
    }

    fun onTabDismissed(tabId: String, url: String) {
        tabInactivityMap.remove(tabId)
        if (url.isBlank()) return
        scope.launch {
            purgeTabAnalyticalStorage(url)
        }
    }

    fun startInactivityWatchdog(context: Context) {
        scope.launch {
            while (true) {
                delay(5 * 60 * 1000L) // Check every 5 minutes
                val now = System.currentTimeMillis()
                val timeoutThreshold = 30 * 60 * 1000L // 30 minutes threshold

                val expiredTabs = tabInactivityMap.filter { now - it.value > timeoutThreshold }.keys
                if (expiredTabs.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        try {
                            WebStorage.getInstance().deleteAllData()
                            CookieManager.getInstance().flush()
                        } catch (_: Exception) {
                            // Ignore
                        }
                    }
                    for (tabId in expiredTabs) {
                        tabInactivityMap.remove(tabId)
                    }
                }
            }
        }
    }

    private suspend fun purgeTabAnalyticalStorage(url: String) = withContext(Dispatchers.IO) {
        try {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(url) ?: return@withContext
            if (cookies.contains("analytics") || cookies.contains("tracker") || cookies.contains("_ga") || cookies.contains("_fbp")) {
                withContext(Dispatchers.Main) {
                    cookieManager.setAcceptThirdPartyCookies(null, false)
                    cookieManager.flush()
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
