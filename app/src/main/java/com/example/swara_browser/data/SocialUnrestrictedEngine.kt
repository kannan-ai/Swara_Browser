package com.example.swara_browser.data

object SocialUnrestrictedEngine {

    fun isSocialMediaSite(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("twitter.com") ||
                lower.contains("x.com") ||
                lower.contains("instagram.com") ||
                lower.contains("reddit.com") ||
                lower.contains("facebook.com") ||
                lower.contains("quora.com") ||
                lower.contains("pinterest.com")
    }

    fun getUnrestrictedSocialScript(): String {
        return """
            (function() {
                function bypassSocialLoginWalls() {
                    document.body.style.overflow = 'auto';
                    document.documentElement.style.overflow = 'auto';

                    var twitterModals = document.querySelectorAll('#layers [role="dialog"], [data-testid="sheetDialog"], [data-testid="BottomBar"]');
                    twitterModals.forEach(function(el) { el.remove(); });

                    var igModals = document.querySelectorAll('div._a9-z, .login-overlay, ._a8-a');
                    igModals.forEach(function(el) { el.remove(); });

                    var redditBanners = document.querySelectorAll('reddit-app-banner, .open-in-app, .x-promo-container');
                    redditBanners.forEach(function(el) { el.remove(); });

                    var fbModals = document.querySelectorAll('#mobile_login_bar, .login_popup, div[role="dialog"]');
                    fbModals.forEach(function(el) {
                        if (el.innerText && (el.innerText.indexOf('Log In') !== -1 || el.innerText.indexOf('Create') !== -1)) {
                            el.remove();
                        }
                    });

                    var quoraModals = document.querySelectorAll('.quora-login-dialog, .SignupModal');
                    quoraModals.forEach(function(el) { el.remove(); });
                    var quoraBlurs = document.querySelectorAll('.qu-blur_2px, .qu-filter_blur');
                    quoraBlurs.forEach(function(el) {
                        el.style.filter = 'none';
                        el.style.webkitFilter = 'none';
                    });
                }
                setInterval(bypassSocialLoginWalls, 500);
            })();
        """.trimIndent()
    }
}
