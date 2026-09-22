package com.example.swara_browser.data

object VirusTotalEngine {

    enum class SafetyStatus {
        CLEAN,
        CAUTION,
        MALICIOUS
    }

    private val knownMalwareKeywords = listOf(
        "malware", "phishing", "trojan", "ransomware", "spyware", "keylogger",
        "bit.ly/malicious", "crack.exe", "free-money.xyz"
    )

    fun scanUrl(url: String): SafetyStatus {
        val lower = url.lowercase()
        if (knownMalwareKeywords.any { lower.contains(it) }) {
            return SafetyStatus.MALICIOUS
        }
        if (lower.startsWith("http://") && !lower.contains("localhost")) {
            return SafetyStatus.CAUTION
        }
        return SafetyStatus.CLEAN
    }
}
