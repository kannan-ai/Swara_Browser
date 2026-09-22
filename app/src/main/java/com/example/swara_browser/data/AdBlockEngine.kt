package com.example.swara_browser.data

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockEngine {

    private val adDomainsSet = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "adform.net",
        "adnxs.com",
        "amazon-adsystem.com",
        "scorecardresearch.com",
        "admob.com",
        "exponential.com",
        "rubiconproject.com",
        "criteo.com",
        "pubmatic.com"
    )

    private val trackerHashSet = hashSetOf(
        "doubleclick.net",
        "google-analytics.com",
        "googletagservices.com",
        "googlesyndication.com",
        "segment.com",
        "mixpanel.com",
        "facebook.net",
        "scorecardresearch.com",
        "hotjar.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "quantserve.com",
        "chartbeat.com",
        "moatads.com"
    )

    private val strictAdKeywords = listOf(
        "/pagead/",
        "/adservice/",
        "/ads/",
        "/adserver/",
        "google-analytics.com",
        "analytics.js",
        "googletagmanager.com",
        "facebook.net/en_US/fbevents.js",
        "pixel.wp.com",
        "youtube.com/api/stats/ads",
        "youtube.com/pagead/",
        "youtube.com/get_midroll_info"
    )

    private fun extractHost(url: String): String {
        if (url.isBlank()) return ""
        val lower = url.lowercase()
        val start = lower.indexOf("://")
        val startIndex = if (start != -1) start + 3 else 0
        val pathStart = lower.indexOf('/', startIndex)
        val hostPort = if (pathStart != -1) lower.substring(startIndex, pathStart) else lower.substring(startIndex)
        return hostPort.substringBefore(':').substringBefore('?')
    }

    fun isTrackerUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val host = extractHost(url)
        if (host.isBlank()) return false
        if (trackerHashSet.contains(host) || trackerHashSet.any { host.endsWith(".$it") }) return true
        return isAdUrl(url, true)
    }

    fun isAdUrl(url: String, isStrict100Percent: Boolean = true): Boolean {
        val host = extractHost(url)

        if (host.isNotBlank()) {
            if (adDomainsSet.contains(host)) return true
            if (adDomainsSet.any { host.endsWith(".$it") }) return true
        }

        val lower = url.lowercase()
        if (isStrict100Percent) {
            return strictAdKeywords.any { lower.contains(it) }
        }
        return false
    }

    fun getEmptyWebResourceResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    fun getYouTubeAdBlockScript(): String {
        return """
            (function() {
                function autoSkipYouTubePrerolls() {
                    var adShowing = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay');
                    var video = document.querySelector('video');
                    if (adShowing && video) {
                        video.muted = true;
                        if (!isNaN(video.duration) && video.duration > 0) {
                            video.currentTime = video.duration;
                        }
                    } else if (video && video.muted) {
                        video.muted = false;
                    }
                    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button-slot, .ytp-skip-ad-button');
                    if (skipBtn) {
                        skipBtn.click();
                    }
                    var banners = document.querySelectorAll('.ytp-ad-overlay-container, #player-ads, ytd-promoted-sparkles-web-renderer, ytd-banner-promo-renderer, .ytd-display-ad-renderer');
                    banners.forEach(function(el) {
                        el.style.display = 'none';
                    });
                }
                setInterval(autoSkipYouTubePrerolls, 300);
            })();
        """.trimIndent()
    }

    fun getRemoveAnnotationsScript(): String {
        return """
            (function() {
                function removeAnnotations() {
                    var elements = document.querySelectorAll('.ytp-ce-element, .ytp-annotation, .annotation-type-custom, .ytp-pause-overlay, .ytp-ce-covering-overlay');
                    elements.forEach(function(el) {
                        el.style.display = 'none';
                    });
                }
                setInterval(removeAnnotations, 800);
            })();
        """.trimIndent()
    }
}
