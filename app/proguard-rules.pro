# ===================================================================
# Swara Browser - Hardened R8 / ProGuard Configuration
# ===================================================================

# --- Room SQLite Persistence ---
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# Preserve AutoValue / Room schemas
-dontwarn androidx.room.paging.**

# --- Jetpack DataStore Preferences & Security Crypto ---
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }
-dontwarn androidx.datastore.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**

# --- OkHttp & Networking ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- WebView & JavaScript Bridge Hardening ---
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# --- Coil Image Pipeline ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Kotlin Coroutines Flow & Reflection ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve Line Numbers for Crash Reporting
-keepattributes SourceFile,LineNumberTable
