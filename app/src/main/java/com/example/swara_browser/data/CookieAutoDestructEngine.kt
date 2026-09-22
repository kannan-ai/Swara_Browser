package com.example.swara_browser.data

import android.webkit.CookieManager
import android.webkit.WebStorage

enum class CookieAutoDestructDuration(val displayName: String) {
    OFF("Off (Manual Wipe)"),
    ON_APP_CLOSE("Instantly on App Close"),
    MINUTES_15("After 15 Minutes"),
    HOURS_1("After 1 Hour"),
    HOURS_24("After 24 Hours")
}

object CookieAutoDestructEngine {

    fun executeCookiePurge() {
        try {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
        } catch (e: Exception) {
            // Fallback
        }
    }
}
