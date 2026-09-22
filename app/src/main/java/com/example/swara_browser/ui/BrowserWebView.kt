package com.example.swara_browser.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ActionMode
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.swara_browser.data.AdBlockEngine
import com.example.swara_browser.data.AstraVaultEngine
import com.example.swara_browser.data.AutofillCryptoManager
import com.example.swara_browser.data.DownloadEngine
import com.example.swara_browser.data.SmartNavigation
import com.example.swara_browser.data.SocialUnrestrictedEngine
import com.example.swara_browser.data.TabCookieWatchdog
import com.example.swara_browser.data.VirusTotalEngine
import com.example.swara_browser.ui.theme.ThemeMode

fun extractImageSourceAtPoint(webView: WebView, x: Float, y: Float, callback: (String?) -> Unit) {
    val js = """
        (function() {
            var els = document.elementsFromPoint($x, $y);
            for (var i = 0; i < els.length; i++) {
                var el = els[i];
                if (el.tagName === 'IMG' && el.src) return el.src;
                var bg = window.getComputedStyle(el).backgroundImage;
                if (bg && bg !== 'none') {
                    var match = bg.match(/url\(['"]?([^'"]+)['"]?\)/);
                    if (match && match[1]) return match[1];
                }
            }
            return null;
        })();
    """.trimIndent()

    webView.evaluateJavascript(js) { result ->
        val clean = result?.trim('"')?.takeIf { it.isNotBlank() && it != "null" }
        callback(clean)
    }
}

fun smartGoBack(webView: WebView): Boolean {
    return SmartNavigation.smartGoBack(webView)
}

class SwaraCustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var onSearchSelectedText: ((String) -> Unit)? = null
    var onExplainSelectedText: ((String) -> Unit)? = null

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        val parentActionMode = super.startActionMode(callback, type)
        return wrapActionMode(parentActionMode)
    }

    override fun startActionMode(callback: ActionMode.Callback?): ActionMode? {
        val parentActionMode = super.startActionMode(callback)
        return wrapActionMode(parentActionMode)
    }

    private fun wrapActionMode(parent: ActionMode?): ActionMode? {
        if (parent == null) return null
        val menu = parent.menu
        menu.add("Search Swara").setOnMenuItemClickListener {
            getSelectedText { text ->
                if (text.isNotBlank()) onSearchSelectedText?.invoke(text)
            }
            parent.finish()
            true
        }
        menu.add("Swara AI Explain").setOnMenuItemClickListener {
            getSelectedText { text ->
                if (text.isNotBlank()) onExplainSelectedText?.invoke(text)
            }
            parent.finish()
            true
        }
        return parent
    }

    private fun getSelectedText(onText: (String) -> Unit) {
        evaluateJavascript("(function(){return window.getSelection().toString();})()") { value ->
            val clean = value?.trim('"')?.replace("\\n", "\n") ?: ""
            onText(clean)
        }
    }
}

private class MediaSnifferJSInterface(val onMediaFound: (String) -> Unit) {
    @JavascriptInterface
    fun onMediaDetected(videoUrl: String) {
        if (videoUrl.isNotBlank() && (
                videoUrl.contains(".mp4") || videoUrl.contains(".webm") ||
                videoUrl.contains(".m4v") || videoUrl.contains(".mkv") ||
                videoUrl.contains(".m3u8") || videoUrl.contains(".mpd") ||
                videoUrl.contains(".ts") || videoUrl.contains("/videoplayback")
            )) {
            onMediaFound(videoUrl)
        }
    }
}

private fun getCanvasFingerprintNoiseScript(): String {
    return """
        (function() {
            if (window.__swara_canvas_protected__) return;
            window.__swara_canvas_protected__ = true;
            
            try {
                var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                HTMLCanvasElement.prototype.toDataURL = function() {
                    var ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0) {
                        try {
                            var imgData = ctx.getImageData(0, 0, this.width, this.height);
                            for (var i = 0; i < imgData.data.length; i += 4) {
                                imgData.data[i] = imgData.data[i] ^ 1;
                            }
                            ctx.putImageData(imgData, 0, 0);
                        } catch(e) {}
                    }
                    return origToDataURL.apply(this, arguments);
                };

                var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
                CanvasRenderingContext2D.prototype.getImageData = function() {
                    var res = origGetImageData.apply(this, arguments);
                    if (res && res.data) {
                        for (var i = 0; i < res.data.length; i += 4) {
                            res.data[i] = res.data[i] ^ 1;
                        }
                    }
                    return res;
                };
            } catch(e) {}
        })();
    """.trimIndent()
}

private fun getFetchXhrInterceptorScript(): String {
    return """
        (function() {
            if (window.__swara_fetch_patched__) return;
            window.__swara_fetch_patched__ = true;

            function checkMediaUrl(url) {
                if (!url || typeof url !== 'string') return;
                var lower = url.toLowerCase();
                if (lower.indexOf('.m3u8') !== -1 || lower.indexOf('.mpd') !== -1 ||
                    lower.indexOf('.mp4') !== -1 || lower.indexOf('.webm') !== -1 ||
                    lower.indexOf('/videoplayback') !== -1) {
                    if (window.AndroidMediaSniffer && window.AndroidMediaSniffer.onMediaDetected) {
                        window.AndroidMediaSniffer.onMediaDetected(url);
                    }
                }
            }

            try {
                var origFetch = window.fetch;
                if (origFetch) {
                    window.fetch = function(input, init) {
                        var url = (typeof input === 'string') ? input : (input && input.url ? input.url : '');
                        checkMediaUrl(url);
                        return origFetch.apply(this, arguments);
                    };
                }

                var origOpen = XMLHttpRequest.prototype.open;
                if (origOpen) {
                    XMLHttpRequest.prototype.open = function(method, url) {
                        checkMediaUrl(url);
                        return origOpen.apply(this, arguments);
                    };
                }
            } catch(e) {}
        })();
    """.trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    url: String,
    tabId: String = "",
    javascriptEnabled: Boolean,
    desktopMode: Boolean,
    isPrivateMode: Boolean,
    antiTrackingEnabled: Boolean,
    httpsOnlyEnabled: Boolean,
    isAstraShieldActive: Boolean,
    strictAdBlockEnabled: Boolean,
    youtubeAdBlockEnabled: Boolean,
    removeAnnotationsEnabled: Boolean,
    unrestrictedSocialModeEnabled: Boolean,
    isAutofillEnabled: Boolean,
    isAdultContentBlocked: Boolean = true,
    dataSaverEnabled: Boolean,
    themeMode: ThemeMode,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onTitleReceived: (String) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onAdBlocked: () -> Unit,
    onVideoDetected: (String) -> Unit = {},
    onShowLinkContextMenu: (String) -> Unit = {},
    onShowImageContextMenu: (String) -> Unit = {},
    onShowImageLinkContextMenu: (String, String) -> Unit = { _, _ -> },
    onSearchText: (String) -> Unit = {},
    onExplainText: (String) -> Unit = {},
    onPromptAppRedirect: (appName: String, onConfirmLaunch: () -> Unit, onStayInBrowser: () -> Unit) -> Unit = { _, _, _ -> },
    onShowPermissionPrompt: (domain: String, permissionName: String, riskLevel: PermissionRiskLevel, reason: String, consequence: String, onAllow: () -> Unit, onDeny: () -> Unit) -> Unit = { _, _, _, _, _, _, _ -> },
    onTabProcessTerminated: (tabId: String, didCrash: Boolean, reason: String) -> Unit = { _, _, _ -> },
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCrashed by remember { mutableStateOf(false) }

    val isOled = themeMode == ThemeMode.OLED
    val isDark = themeMode == ThemeMode.DARK || isOled

    val mobileUserAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    val webView = remember {
        SwaraCustomWebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            onSearchSelectedText = { text -> onSearchText(text) }
            onExplainSelectedText = { text -> onExplainText(text) }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = !isPrivateMode
                @Suppress("DEPRECATION")
                databaseEnabled = !isPrivateMode
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                allowFileAccess = false
                allowContentAccess = false
                setGeolocationEnabled(false)
                mediaPlaybackRequiresUserGesture = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = mobileUserAgent
                @Suppress("DEPRECATION")
                saveFormData = !isPrivateMode
            }

            addJavascriptInterface(MediaSnifferJSInterface { onVideoDetected(it) }, "AndroidMediaSniffer")

            setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                DownloadEngine.enqueueDownload(
                    context = context,
                    downloadUrl = downloadUrl,
                    pageUrl = url,
                    userAgent = userAgent ?: settings.userAgentString,
                    contentDisposition = contentDisposition,
                    mimeType = mimetype
                )
            }

            setOnLongClickListener {
                val result = hitTestResult
                when (result.type) {
                    WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                        val linkUrl = result.extra ?: ""
                        if (linkUrl.isNotBlank()) onShowLinkContextMenu(linkUrl)
                        true
                    }
                    WebView.HitTestResult.IMAGE_TYPE -> {
                        val imageUrl = result.extra ?: ""
                        if (imageUrl.isNotBlank()) onShowImageContextMenu(imageUrl)
                        true
                    }
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                        val handler = Handler(Looper.getMainLooper())
                        val message = handler.obtainMessage()
                        requestFocusNodeHref(message)
                        val linkUrl = message.data.getString("url") ?: ""
                        val imageUrl = result.extra ?: message.data.getString("src") ?: ""
                        onShowImageLinkContextMenu(imageUrl, linkUrl)
                        true
                    }
                    else -> false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgressChanged(newProgress)
                    view?.let {
                        onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    title?.let { onTitleReceived(it) }
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    onShowPermissionPrompt(
                        origin ?: "Website",
                        "Precise GPS Location",
                        PermissionRiskLevel.HIGH_RISK,
                        "The site requests real-time GPS coordinates for localized features.",
                        "Location-based maps and search results will not reflect your exact position.",
                        { callback?.invoke(origin, true, false) },
                        { callback?.invoke(origin, false, false) }
                    )
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    val resources = request?.resources ?: emptyArray()
                    val isCam = resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                    val isMic = resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)

                    val name = when {
                        isCam && isMic -> "Camera & Microphone"
                        isCam -> "Camera"
                        isMic -> "Microphone"
                        else -> "Hardware Sensor"
                    }

                    onShowPermissionPrompt(
                        request?.origin?.host ?: "Website",
                        name,
                        PermissionRiskLevel.HIGH_RISK,
                        "The site requests access to video/audio streams for recording or calls.",
                        "Webcam and audio capture will be blocked for this site.",
                        { request?.grant(request.resources) },
                        { request?.deny() }
                    )
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    isCrashed = true
                    val didCrash = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        detail?.didCrash() ?: true
                    } else {
                        true
                    }
                    val reason = if (didCrash) "Low memory crash" else "System killed render process"
                    onTabProcessTerminated(tabId, didCrash, reason)
                    view?.destroy()
                    return true
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    if (httpsOnlyEnabled) {
                        handler?.cancel()
                        Toast.makeText(context, "🔒 HTTPS-Only Safety: Insecure SSL connection blocked", Toast.LENGTH_SHORT).show()
                    } else {
                        handler?.proceed()
                    }
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString() ?: return null

                    if (strictAdBlockEnabled && AdBlockEngine.isTrackerUrl(reqUrl)) {
                        onAdBlocked()
                        return AdBlockEngine.getEmptyWebResourceResponse()
                    }

                    if (isAdultContentBlocked) {
                        val host = try { Uri.parse(reqUrl).host?.lowercase() ?: "" } catch (_: Exception) { "" }
                        if (host.contains("pornhub") || host.contains("xvideos") || host.contains("xnxx") || host.contains("xhamster") || host.contains("youporn") || host.contains("redtube")) {
                            return AdBlockEngine.getEmptyWebResourceResponse()
                        }
                    }

                    val lowerReq = reqUrl.lowercase()
                    if (lowerReq.contains(".mp4") || lowerReq.contains(".webm") || lowerReq.contains(".m4v") || lowerReq.contains(".mkv") || lowerReq.contains(".m3u8") || lowerReq.contains(".mpd") || lowerReq.contains("/videoplayback")) {
                        if (!lowerReq.contains("google-analytics") && !lowerReq.contains("doubleclick")) {
                            onVideoDetected(reqUrl)
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    url?.let {
                        onPageStarted(it)
                        TabCookieWatchdog.recordTabActivity(tabId)
                        val status = VirusTotalEngine.scanUrl(it)
                        if (status == VirusTotalEngine.SafetyStatus.MALICIOUS) {
                            Toast.makeText(context, "⚠️ Threat Warning: Potential malicious site!", Toast.LENGTH_LONG).show()
                        }
                    }
                    view?.let {
                        onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                        if (antiTrackingEnabled || isPrivateMode) {
                            it.evaluateJavascript(getCanvasFingerprintNoiseScript(), null)
                        }
                        it.evaluateJavascript(getFetchXhrInterceptorScript(), null)
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    url?.let {
                        onPageFinished(it)
                        TabCookieWatchdog.recordTabActivity(tabId)
                    }
                    view?.let {
                        onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                        val pageUrl = it.url ?: ""

                        // Inject anti-contextmenu blocker bypass
                        val antiBlockerJs = """
                            (function() {
                                var events = ['contextmenu', 'selectstart', 'dragstart'];
                                events.forEach(function(evt) {
                                    window.addEventListener(evt, function(e) { e.stopPropagation(); }, true);
                                });
                            })();
                        """.trimIndent()
                        it.evaluateJavascript(antiBlockerJs, null)

                        if (antiTrackingEnabled || isPrivateMode) {
                            it.evaluateJavascript(getCanvasFingerprintNoiseScript(), null)
                        }
                        it.evaluateJavascript(getFetchXhrInterceptorScript(), null)

                        if (youtubeAdBlockEnabled && (pageUrl.contains("youtube.com") || pageUrl.contains("youtube-nocookie.com"))) {
                            it.evaluateJavascript(AdBlockEngine.getYouTubeAdBlockScript(), null)
                        }
                        if (removeAnnotationsEnabled && (pageUrl.contains("youtube.com") || pageUrl.contains("youtube-nocookie.com"))) {
                            it.evaluateJavascript(AdBlockEngine.getRemoveAnnotationsScript(), null)
                        }
                        if (unrestrictedSocialModeEnabled && SocialUnrestrictedEngine.isSocialMediaSite(pageUrl)) {
                            it.evaluateJavascript(SocialUnrestrictedEngine.getUnrestrictedSocialScript(), null)
                        }
                        if (isAutofillEnabled && pageUrl.isNotBlank()) {
                            val host = try { Uri.parse(pageUrl).host?.lowercase() ?: "" } catch (_: Exception) { "" }
                            if (host.isNotBlank()) {
                                val creds = AutofillCryptoManager(context).getCredentials(host)
                                if (creds != null) {
                                    val autofillJs = """
                                        (function() {
                                            var u = document.querySelector('input[type="text"], input[type="email"], input[name*="user"]');
                                            var p = document.querySelector('input[type="password"]');
                                            if (u && !u.value) u.value = "${creds.first}";
                                            if (p && !p.value) p.value = "${creds.second}";
                                        })();
                                    """.trimIndent()
                                    it.evaluateJavascript(autofillJs, null)
                                }
                            }
                        }

                        val mediaScanJs = """
                            (function() {
                                var videos = document.querySelectorAll('video');
                                videos.forEach(function(v) {
                                    var src = v.currentSrc || v.src;
                                    if (src && src.startsWith('http')) {
                                        window.AndroidMediaSniffer.onMediaDetected(src);
                                    }
                                });
                            })();
                        """.trimIndent()
                        it.evaluateJavascript(mediaScanJs, null)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val requestUrl = request?.url?.toString() ?: return false

                    // Intercept external app schemes
                    if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
                        if (requestUrl.startsWith("intent://")) {
                            try {
                                val parsedIntent = Intent.parseUri(requestUrl, Intent.URI_INTENT_SCHEME)
                                val fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url")
                                val appPkg = parsedIntent.`package` ?: "External App"

                                onPromptAppRedirect(
                                    appPkg,
                                    { context.startActivity(parsedIntent) },
                                    {
                                        if (!fallbackUrl.isNullOrEmpty()) {
                                            view?.loadUrl(fallbackUrl)
                                        }
                                    }
                                )
                                return true
                            } catch (_: Exception) {
                                return true
                            }
                        } else {
                            val targetScheme = request.url?.scheme ?: "App"
                            onPromptAppRedirect(
                                targetScheme,
                                {
                                    try {
                                        val customIntent = Intent(Intent.ACTION_VIEW, Uri.parse(requestUrl))
                                        context.startActivity(customIntent)
                                    } catch (_: Exception) {
                                        // Ignore
                                    }
                                },
                                {}
                            )
                            return true
                        }
                    }
                    return false
                }
            }

            onWebViewCreated(this)
        }
    }

    LaunchedEffect(dataSaverEnabled) {
        webView.settings.blockNetworkImage = dataSaverEnabled
    }

    LaunchedEffect(isAstraShieldActive) {
        AstraVaultEngine.applyAstraShieldSettings(webView.settings, isAstraShieldActive)
    }

    LaunchedEffect(isPrivateMode, antiTrackingEnabled) {
        val cookieManager = CookieManager.getInstance()
        if (antiTrackingEnabled || isPrivateMode) {
            cookieManager.setAcceptThirdPartyCookies(webView, false)
        } else {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        if (isPrivateMode) {
            webView.clearCache(true)
            webView.clearFormData()
            webView.clearHistory()
            cookieManager.removeAllCookies(null)
        }
    }

    LaunchedEffect(isDark, isOled) {
        if (isOled) {
            webView.setBackgroundColor(AndroidColor.BLACK)
        } else if (isDark) {
            webView.setBackgroundColor(AndroidColor.parseColor("#121212"))
        } else {
            webView.setBackgroundColor(AndroidColor.WHITE)
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            try {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, isDark || isOled)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(javascriptEnabled) {
        webView.settings.javaScriptEnabled = javascriptEnabled
    }

    LaunchedEffect(desktopMode) {
        if (desktopMode) {
            webView.settings.userAgentString = desktopUserAgent
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
        } else {
            webView.settings.userAgentString = mobileUserAgent
        }
        if (webView.url != null) {
            webView.reload()
        }
    }

    LaunchedEffect(url) {
        if (url.isNotBlank() && webView.url != url) {
            webView.loadUrl(url)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        if (isCrashed) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Page crashed due to low memory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The web view render process was killed by the system.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isCrashed = false
                            webView.reload()
                        }
                    ) {
                        Text("Reload Page 🔄", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
