package com.example.swara_browser.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AutofillCryptoManager(context: Context) {

    private val sharedPreferences: SharedPreferences? = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "encrypted_autofill_vault",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Throwable) {
        null
    }

    fun saveCredentials(domain: String, username: String, password: String) {
        if (domain.isBlank()) return
        sharedPreferences?.edit()
            ?.putString("${domain}_user", username)
            ?.putString("${domain}_pass", password)
            ?.apply()
    }

    fun getCredentials(domain: String): Pair<String, String>? {
        if (domain.isBlank()) return null
        val prefs = sharedPreferences ?: return null
        val user = prefs.getString("${domain}_user", null) ?: return null
        val pass = prefs.getString("${domain}_pass", null) ?: return null
        return Pair(user, pass)
    }

    fun deleteCredentials(domain: String) {
        if (domain.isBlank()) return
        sharedPreferences?.edit()
            ?.remove("${domain}_user")
            ?.remove("${domain}_pass")
            ?.apply()
    }

    fun wipeAutofillVault() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
}
