package com.example.swara_browser.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SwaraDownloadRecord(
    val downloadId: Long,
    val fileName: String,
    val mimeType: String,
    val url: String,
    val timestamp: Long
)

object SwaraDownloadTracker {

    private const val PREF_NAME = "swara_downloads_tracker_prefs"
    private const val KEY_RECORDS = "swara_download_records_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun recordDownload(
        context: Context,
        downloadId: Long,
        fileName: String,
        mimeType: String,
        url: String
    ) {
        if (downloadId <= 0) return
        val currentRecords = getSwaraDownloads(context).toMutableList()
        currentRecords.removeAll { it.downloadId == downloadId }
        currentRecords.add(
            0,
            SwaraDownloadRecord(
                downloadId = downloadId,
                fileName = fileName,
                mimeType = mimeType,
                url = url,
                timestamp = System.currentTimeMillis()
            )
        )

        saveRecords(context, currentRecords)
    }

    fun getSwaraDownloads(context: Context): List<SwaraDownloadRecord> {
        val jsonStr = getPrefs(context).getString(KEY_RECORDS, null) ?: return emptyList()
        val list = mutableListOf<SwaraDownloadRecord>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SwaraDownloadRecord(
                        downloadId = obj.getLong("downloadId"),
                        fileName = obj.optString("fileName", "Download"),
                        mimeType = obj.optString("mimeType", "*/*"),
                        url = obj.optString("url", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }

    fun getSwaraDownloadIds(context: Context): LongArray {
        return getSwaraDownloads(context).map { it.downloadId }.toLongArray()
    }

    fun removeDownloadRecord(context: Context, downloadId: Long) {
        val currentRecords = getSwaraDownloads(context).toMutableList()
        currentRecords.removeAll { it.downloadId == downloadId }
        saveRecords(context, currentRecords)
    }

    private fun saveRecords(context: Context, records: List<SwaraDownloadRecord>) {
        try {
            val jsonArray = JSONArray()
            for (record in records.take(100)) {
                val obj = JSONObject().apply {
                    put("downloadId", record.downloadId)
                    put("fileName", record.fileName)
                    put("mimeType", record.mimeType)
                    put("url", record.url)
                    put("timestamp", record.timestamp)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_RECORDS, jsonArray.toString()).apply()
        } catch (_: Exception) {
            // Ignore
        }
    }
}
