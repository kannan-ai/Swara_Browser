package com.example.swara_browser

import android.app.Application
import android.webkit.WebView

class SwaraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Must be called prior to creating any WebView instances to support full-page rendering
        WebView.enableSlowWholeDocumentDraw()
    }
}
