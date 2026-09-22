package com.example.swara_browser.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.swara_browser.MainActivity
import com.example.swara_browser.R

object NewsNotificationManager {

    const val CHANNEL_ID = "swara_news_channel"
    const val CHANNEL_NAME = "Swara Breaking News"
    const val EXTRA_NEWS_URL = "extra_news_url"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for top breaking news and daily stories"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun postNewsNotification(context: Context, newsItem: NewsItem) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NEWS_URL, newsItem.url)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            newsItem.url.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📰 ${newsItem.source} • ${newsItem.badgeLabel.ifBlank { "Top Story" }}")
            .setContentText(newsItem.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(newsItem.title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(newsItem.url.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+
        }
    }
}
