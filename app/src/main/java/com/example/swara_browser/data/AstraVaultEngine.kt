package com.example.swara_browser.data

import android.webkit.WebSettings
import java.util.Locale

object AstraVaultEngine {

    fun isProtectionActive(activeUntilTimestamp: Long): Boolean {
        return activeUntilTimestamp > System.currentTimeMillis()
    }

    fun getRemainingTimeFormatted(activeUntilTimestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = activeUntilTimestamp - now
        if (diffMs <= 0) return "00h 00m 00s"

        val totalSeconds = diffMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds)
    }

    fun applyAstraShieldSettings(settings: WebSettings, isShieldActive: Boolean) {
        if (isShieldActive) {
            settings.apply {
                allowContentAccess = false
                allowFileAccess = false
                mediaPlaybackRequiresUserGesture = true
            }
        }
    }
}
