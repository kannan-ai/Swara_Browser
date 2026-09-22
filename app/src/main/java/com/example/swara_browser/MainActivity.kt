package com.example.swara_browser

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.swara_browser.data.NewsNotificationManager
import com.example.swara_browser.data.SelfDestructSecurityManager
import com.example.swara_browser.ui.BrowserScreen
import com.example.swara_browser.ui.BrowserViewModel
import com.example.swara_browser.ui.theme.Swara_BrowserTheme

class MainActivity : ComponentActivity() {

    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SelfDestructSecurityManager(this).checkTamperAndEnforceDestruct()
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val themeMode by browserViewModel.themeMode.collectAsState()
            val isPrivateMode by browserViewModel.isPrivateMode.collectAsState()

            LaunchedEffect(isPrivateMode) {
                if (isPrivateMode) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE,
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            Swara_BrowserTheme(themeMode = themeMode) {
                BrowserScreen(
                    viewModel = browserViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SelfDestructSecurityManager(this).checkTamperAndEnforceDestruct()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val newsUrl = intent?.getStringExtra(NewsNotificationManager.EXTRA_NEWS_URL)
        if (!newsUrl.isNullOrBlank()) {
            browserViewModel.newTab()
            browserViewModel.openExternalUrl(newsUrl)
            return
        }
        val action = intent?.action
        val data = intent?.dataString
        if (action == Intent.ACTION_VIEW && !data.isNullOrBlank()) {
            browserViewModel.openExternalUrl(data)
        }
    }
}
