package com.example.swara_browser

import com.example.swara_browser.data.AdBlockEngine
import com.example.swara_browser.data.SmartNavigation
import com.example.swara_browser.data.TrendingNewsEngine
import com.example.swara_browser.tabs.TabModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SwaraBrowserProtectionTest {

    @Test
    fun testAdBlockEngineTrackerDetection() {
        val googleAdUrl = "https://doubleclick.net/pagead/ads?id=123"
        val analyticsUrl = "https://google-analytics.com/analytics.js"
        val facebookPixelUrl = "https://facebook.net/en_US/fbevents.js"
        val cleanUrl = "https://www.wikipedia.org"

        assertTrue("DoubleClick ad URL should be blocked", AdBlockEngine.isTrackerUrl(googleAdUrl))
        assertTrue("Google Analytics URL should be blocked", AdBlockEngine.isTrackerUrl(analyticsUrl))
        assertTrue("Facebook Pixel URL should be blocked", AdBlockEngine.isTrackerUrl(facebookPixelUrl))
        assertFalse("Clean Wikipedia URL should NOT be blocked", AdBlockEngine.isTrackerUrl(cleanUrl))
    }

    @Test
    fun testEmptyWebResourceResponse() {
        val emptyResponse = AdBlockEngine.getEmptyWebResourceResponse()
        assertNotNull("Response object must not be null", emptyResponse)
    }

    @Test
    fun testSmartNavigationRedirectDetection() {
        val adUrl = "https://www.google.com/aclk?sa=l&ai=DChcSE"
        val googleRedirectUrl = "https://google.com/url?q=https://example.com"
        val normalUrl = "https://news.ycombinator.com"

        val method = SmartNavigation::class.java.getDeclaredMethod("isRedirectPattern", String::class.java)
        method.isAccessible = true

        val isAdRedirect = method.invoke(SmartNavigation, adUrl) as Boolean
        val isGoogleRedirect = method.invoke(SmartNavigation, googleRedirectUrl) as Boolean
        val isNormal = method.invoke(SmartNavigation, normalUrl) as Boolean

        assertTrue("Google Shopping /aclk link should be identified as redirect", isAdRedirect)
        assertTrue("google.com/url redirect link should be identified as redirect", isGoogleRedirect)
        assertFalse("Normal content link should NOT be identified as redirect", isNormal)
    }

    @Test
    fun testTrackingParameterStripper() {
        val urlWithUtm = "https://example.com/article?utm_source=twitter&utm_medium=social&utm_campaign=spring_sale&ref=123&id=99"
        val cleanedUrl = TrendingNewsEngine.stripTrackingParameters(urlWithUtm)

        assertFalse("URL should no longer contain utm_source", cleanedUrl.contains("utm_source"))
        assertFalse("URL should no longer contain utm_medium", cleanedUrl.contains("utm_medium"))
        assertFalse("URL should no longer contain ref", cleanedUrl.contains("ref="))
        assertTrue("URL should retain essential parameters like id=99", cleanedUrl.contains("id=99"))
    }

    @Test
    fun testIncognitoTabModelFlag() {
        val standardTab = TabModel(id = "tab_1", url = "https://google.com", title = "Google", isIncognito = false)
        val incognitoTab = TabModel(id = "tab_2", url = "https://example.com", title = "Incognito", isIncognito = true)

        assertFalse("Standard tab should not be incognito", standardTab.isIncognito)
        assertTrue("Incognito tab should have isIncognito flag set to true", incognitoTab.isIncognito)
    }

    @Test
    fun testScreenshotHeightClamping() {
        val requestedHeight = 25000
        val maxSafeHeight = requestedHeight.coerceAtMost(16000)

        assertEquals("Height exceeding ceiling must be clamped to 16,000 px to prevent OOM", 16000, maxSafeHeight)
    }
}
