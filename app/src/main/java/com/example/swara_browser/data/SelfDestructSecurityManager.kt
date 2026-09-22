package com.example.swara_browser.data

import android.content.Context
import android.os.Debug
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.security.crypto.MasterKey
import com.example.swara_browser.ui.theme.ThemeMode
import java.io.File
import java.security.KeyStore
import kotlin.system.exitProcess

class SelfDestructSecurityManager(private val context: Context) {

    private val settingsRepository = BrowserSettingsRepository(context)
    private val pointsRepository = AstraPointsRepository(context)
    private val autofillManager = AutofillCryptoManager(context)

    fun checkTamperAndEnforceDestruct() {
        if (isTamperDetected()) {
            executeEmergencySelfDestruct()
        }
    }

    private fun isTamperDetected(): Boolean {
        if (Debug.isDebuggerConnected()) return true
        return try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText().lowercase()
                content.contains("frida") || content.contains("xposed") || content.contains("substrate")
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun executeEmergencySelfDestruct(webView: WebView? = null) {
        autofillManager.wipeAutofillVault()

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (_: Exception) {
            // Ignore
        }

        try {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            webView?.let {
                it.clearCache(true)
                it.clearFormData()
                it.clearHistory()
                it.clearSslPreferences()
            }
        } catch (_: Exception) {
            // Ignore
        }

        try {
            context.filesDir?.deleteRecursively()
        } catch (_: Exception) {
            // Ignore
        }

        exitProcess(0)
    }

    suspend fun executeSelfDestructWipe(webView: WebView? = null) {
        settingsRepository.setThemeMode(ThemeMode.SYSTEM)
        settingsRepository.setSearchEngine(SearchEngine.GOOGLE)
        settingsRepository.setPrivateMode(false)
        pointsRepository.wipeAllPointsData()
        autofillManager.wipeAutofillVault()

        try {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            webView?.let {
                it.clearCache(true)
                it.clearFormData()
                it.clearHistory()
                it.clearSslPreferences()
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
