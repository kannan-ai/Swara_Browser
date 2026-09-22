package com.example.swara_browser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.astraDataStore: DataStore<Preferences> by preferencesDataStore(name = "astra_points_preferences")

data class PointsActivityTransaction(
    val id: String,
    val type: String, // "EARNED" or "REDEEMED"
    val amount: Int,
    val description: String,
    val dateFormatted: String
)

class AstraPointsRepository(private val context: Context) {

    private object PreferencesKeys {
        val POINTS_BALANCE = intPreferencesKey("points_balance")
        val STREAK_DAY = intPreferencesKey("streak_day")
        val LAST_CHECKIN_DAY = longPreferencesKey("last_checkin_day")
        val PUZZLE_PIECES = intPreferencesKey("puzzle_pieces")
        val COMPLETED_PUZZLES = intPreferencesKey("completed_puzzles")
        val DAILY_BROWSING_MINS = intPreferencesKey("daily_browsing_mins")
        val DAILY_NEWS_COUNT = intPreferencesKey("daily_news_count")
        val DAILY_AI_QUERIES_COUNT = intPreferencesKey("daily_ai_queries_count")
        val DAILY_VIDEOS_COUNT = intPreferencesKey("daily_videos_count")
        val DAILY_QUIZ_DONE = booleanPreferencesKey("daily_quiz_done")
        val IS_APP_TOUR_DONE = booleanPreferencesKey("is_app_tour_done")
        val IS_DEFAULT_BROWSER_CLAIMED = booleanPreferencesKey("is_default_browser_claimed")
        val ACTIVE_UNTIL_TIMESTAMP = longPreferencesKey("active_until_timestamp")
        val LAST_DAILY_CLAIM_DATE = stringPreferencesKey("last_daily_claim_date")
        val POINTS_HISTORY_JSON = stringPreferencesKey("points_history_json")
        val LAST_DAILY_CLAIM_COUNT = intPreferencesKey("last_daily_claim_count")
        val LAST_DAILY_CLAIM_DATE_CAP = stringPreferencesKey("last_daily_claim_date_cap")
        val COOLDOWN_END_MAP_JSON = stringPreferencesKey("cooldown_end_map_json")
        val LAST_REDEEMED_HOURS = intPreferencesKey("last_redeemed_hours")
    }

    val pointsFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
    }

    val activeUntilTimestampFlow: Flow<Long> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_UNTIL_TIMESTAMP] ?: 0L
    }

    val lastDailyClaimDateFlow: Flow<String> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_DAILY_CLAIM_DATE] ?: ""
    }

    val streakDayFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.STREAK_DAY] ?: 1
    }

    val lastCheckInDayFlow: Flow<Long> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_CHECKIN_DAY] ?: 0L
    }

    val puzzlePiecesFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.PUZZLE_PIECES] ?: 0
    }

    val completedPuzzlesFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.COMPLETED_PUZZLES] ?: 0
    }

    val dailyBrowsingMinsFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_BROWSING_MINS] ?: 0
    }

    val dailyNewsCountFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_NEWS_COUNT] ?: 0
    }

    val dailyAiQueriesCountFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_AI_QUERIES_COUNT] ?: 0
    }

    val isAppTourDoneFlow: Flow<Boolean> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_APP_TOUR_DONE] ?: false
    }

    val isDefaultBrowserClaimedFlow: Flow<Boolean> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DEFAULT_BROWSER_CLAIMED] ?: false
    }

    val pointsHistoryFlow: Flow<List<PointsActivityTransaction>> = context.astraDataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.POINTS_HISTORY_JSON] ?: ""
        if (json.isBlank()) {
            listOf(
                PointsActivityTransaction("t_init", "EARNED", 100, "Welcome Starter Bonus", getTodayString())
            )
        } else {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<PointsActivityTransaction>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        PointsActivityTransaction(
                            id = obj.optString("id", "t_$i"),
                            type = obj.optString("type", "EARNED"),
                            amount = obj.optInt("amount", 0),
                            description = obj.optString("description", "Points Event"),
                            dateFormatted = obj.optString("dateFormatted", getTodayString())
                        )
                    )
                }
                list.reversed()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    val cooldownEndMapFlow: Flow<Map<Int, Long>> = context.astraDataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.COOLDOWN_END_MAP_JSON] ?: ""
        if (json.isBlank()) {
            emptyMap()
        } else {
            try {
                val obj = JSONObject(json)
                val map = mutableMapOf<Int, Long>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key.toInt()] = obj.getLong(key)
                }
                map
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }

    val dailyClaimCountFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        val lastDate = preferences[PreferencesKeys.LAST_DAILY_CLAIM_DATE_CAP] ?: ""
        val today = getTodayString()
        if (lastDate != today) {
            0
        } else {
            preferences[PreferencesKeys.LAST_DAILY_CLAIM_COUNT] ?: 0
        }
    }

    val lastRedeemedHoursFlow: Flow<Int> = context.astraDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_REDEEMED_HOURS] ?: 0
    }

    fun getTodayEpochDay(): Long {
        return System.currentTimeMillis() / (24 * 3600 * 1000L)
    }

    fun getTodayString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }

    suspend fun recordTransaction(type: String, amount: Int, description: String) {
        context.astraDataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.POINTS_HISTORY_JSON] ?: ""
            val array = if (json.isNotBlank()) try { JSONArray(json) } catch (_: Exception) { JSONArray() } else JSONArray()
            val obj = JSONObject().apply {
                put("id", "t_${System.currentTimeMillis()}")
                put("type", type)
                put("amount", amount)
                put("description", description)
                put("dateFormatted", getTodayString())
            }
            array.put(obj)
            preferences[PreferencesKeys.POINTS_HISTORY_JSON] = array.toString()
        }
    }

    suspend fun claimDailyCheckIn(todayEpochDay: Long = getTodayEpochDay()): Boolean {
        var claimed = false
        context.astraDataStore.edit { preferences ->
            val lastDay = preferences[PreferencesKeys.LAST_CHECKIN_DAY] ?: 0L
            if (lastDay != todayEpochDay) {
                var currentStreak = preferences[PreferencesKeys.STREAK_DAY] ?: 1
                if (lastDay > 0 && todayEpochDay > lastDay + 1) {
                    currentStreak = 1
                }

                val rewardPoints = when (currentStreak) {
                    1 -> 5
                    2 -> 10
                    3 -> 20
                    4 -> 40
                    5 -> 80
                    6 -> 160
                    else -> 200
                }

                val currentBalance = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentBalance + rewardPoints
                preferences[PreferencesKeys.LAST_CHECKIN_DAY] = todayEpochDay

                // Log Transaction
                val json = preferences[PreferencesKeys.POINTS_HISTORY_JSON] ?: ""
                val array = if (json.isNotBlank()) try { JSONArray(json) } catch (_: Exception) { JSONArray() } else JSONArray()
                val obj = JSONObject().apply {
                    put("id", "t_${System.currentTimeMillis()}")
                    put("type", "EARNED")
                    put("amount", rewardPoints)
                    put("description", "Daily Check-in Day $currentStreak")
                    put("dateFormatted", getTodayString())
                }
                array.put(obj)
                preferences[PreferencesKeys.POINTS_HISTORY_JSON] = array.toString()

                if (currentStreak >= 7) {
                    var pieces = preferences[PreferencesKeys.PUZZLE_PIECES] ?: 0
                    pieces += 1
                    if (pieces >= 6) {
                        preferences[PreferencesKeys.POINTS_BALANCE] = (preferences[PreferencesKeys.POINTS_BALANCE] ?: 0) + 1000
                        preferences[PreferencesKeys.PUZZLE_PIECES] = 0
                        val puzzles = preferences[PreferencesKeys.COMPLETED_PUZZLES] ?: 0
                        preferences[PreferencesKeys.COMPLETED_PUZZLES] = puzzles + 1
                    } else {
                        preferences[PreferencesKeys.PUZZLE_PIECES] = pieces
                    }
                    preferences[PreferencesKeys.STREAK_DAY] = 1
                } else {
                    preferences[PreferencesKeys.STREAK_DAY] = currentStreak + 1
                }
                claimed = true
            }
        }
        return claimed
    }

    suspend fun claimDailyBonus(): Boolean {
        return claimDailyCheckIn(getTodayEpochDay())
    }

    suspend fun recordAiQueryUsage(): Boolean {
        var rewarded = false
        val today = getTodayEpochDay()
        context.astraDataStore.edit { preferences ->
            val lastDay = preferences[PreferencesKeys.LAST_CHECKIN_DAY] ?: 0L
            var aiCount = preferences[PreferencesKeys.DAILY_AI_QUERIES_COUNT] ?: 0
            if (lastDay != today) {
                aiCount = 0
            }

            if (aiCount < 3) {
                val currentBalance = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentBalance + 5
                preferences[PreferencesKeys.DAILY_AI_QUERIES_COUNT] = aiCount + 1
                rewarded = true
            }
        }
        return rewarded
    }

    suspend fun recordNewsRead() {
        val today = getTodayEpochDay()
        context.astraDataStore.edit { preferences ->
            val lastDay = preferences[PreferencesKeys.LAST_CHECKIN_DAY] ?: 0L
            var newsCount = preferences[PreferencesKeys.DAILY_NEWS_COUNT] ?: 0
            if (lastDay != today) {
                newsCount = 0
            }

            if (newsCount < 5) {
                val currentBalance = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentBalance + 5
                preferences[PreferencesKeys.DAILY_NEWS_COUNT] = newsCount + 1
            }
        }
    }

    suspend fun awardBrowsingPoints(minutes: Int) {
        if (minutes <= 0) return
        val today = getTodayEpochDay()
        context.astraDataStore.edit { preferences ->
            val lastDay = preferences[PreferencesKeys.LAST_CHECKIN_DAY] ?: 0L
            var currentMins = preferences[PreferencesKeys.DAILY_BROWSING_MINS] ?: 0
            if (lastDay != today) {
                currentMins = 0
            }

            val newMins = currentMins + minutes
            preferences[PreferencesKeys.DAILY_BROWSING_MINS] = newMins

            val pointsEarned = (minutes / 5) * 10
            if (pointsEarned > 0) {
                val currentPoints = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentPoints + pointsEarned
            }
        }
    }

    suspend fun claimAppTourBonus(): Boolean {
        var claimed = false
        context.astraDataStore.edit { preferences ->
            val done = preferences[PreferencesKeys.IS_APP_TOUR_DONE] ?: false
            if (!done) {
                val currentBalance = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentBalance + 50
                preferences[PreferencesKeys.IS_APP_TOUR_DONE] = true
                claimed = true
            }
        }
        return claimed
    }

    suspend fun claimDefaultBrowserBonus(): Boolean {
        var claimed = false
        context.astraDataStore.edit { preferences ->
            val done = preferences[PreferencesKeys.IS_DEFAULT_BROWSER_CLAIMED] ?: false
            if (!done) {
                val currentBalance = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
                preferences[PreferencesKeys.POINTS_BALANCE] = currentBalance + 100
                preferences[PreferencesKeys.IS_DEFAULT_BROWSER_CLAIMED] = true
                claimed = true
            }
        }
        return claimed
    }

    suspend fun redeemProtectionPackageWithRules(hours: Int, pointCost: Int): String {
        val today = getTodayString()
        val now = System.currentTimeMillis()

        var statusMessage = "SUCCESS"

        context.astraDataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_DAILY_CLAIM_DATE_CAP] ?: ""
            val dailyCount = if (lastDate != today) 0 else preferences[PreferencesKeys.LAST_DAILY_CLAIM_COUNT] ?: 0

            if (dailyCount >= 4) {
                statusMessage = "MAX_DAILY_CLAIMS"
                return@edit
            }

            val activeUntil = preferences[PreferencesKeys.ACTIVE_UNTIL_TIMESTAMP] ?: 0L
            if (activeUntil > now) {
                statusMessage = "SHIELD_ALREADY_ACTIVE"
                return@edit
            }

            // Check tier cooldown
            val cooldownJson = preferences[PreferencesKeys.COOLDOWN_END_MAP_JSON] ?: ""
            val cooldownMap = if (cooldownJson.isNotBlank()) try { JSONObject(cooldownJson) } catch (_: Exception) { JSONObject() } else JSONObject()
            val cooldownEnd = if (cooldownMap.has(hours.toString())) cooldownMap.getLong(hours.toString()) else 0L

            if (cooldownEnd > now) {
                statusMessage = "TIER_COOLDOWN_ACTIVE"
                return@edit
            }

            val currentPoints = preferences[PreferencesKeys.POINTS_BALANCE] ?: 100
            if (currentPoints < pointCost) {
                statusMessage = "INSUFFICIENT_POINTS"
                return@edit
            }

            // Redeem!
            preferences[PreferencesKeys.POINTS_BALANCE] = currentPoints - pointCost
            val addedMs = hours * 3600 * 1000L
            preferences[PreferencesKeys.ACTIVE_UNTIL_TIMESTAMP] = now + addedMs
            preferences[PreferencesKeys.LAST_REDEEMED_HOURS] = hours

            // Calculate cooldown duration for this tier
            val cooldownMs = when (hours) {
                0 -> 10 * 60 * 1000L // 10 min
                1 -> 30 * 60 * 1000L // 30 min
                3 -> 12 * 3600 * 1000L // 12 hours
                12 -> 24 * 3600 * 1000L // 24 hours
                24 -> 48 * 3600 * 1000L // 48 hours
                else -> 30 * 60 * 1000L
            }

            cooldownMap.put(hours.toString(), now + cooldownMs)
            preferences[PreferencesKeys.COOLDOWN_END_MAP_JSON] = cooldownMap.toString()

            preferences[PreferencesKeys.LAST_DAILY_CLAIM_DATE_CAP] = today
            preferences[PreferencesKeys.LAST_DAILY_CLAIM_COUNT] = dailyCount + 1

            // Record Transaction Log
            val json = preferences[PreferencesKeys.POINTS_HISTORY_JSON] ?: ""
            val array = if (json.isNotBlank()) try { JSONArray(json) } catch (_: Exception) { JSONArray() } else JSONArray()
            val obj = JSONObject().apply {
                put("id", "t_${System.currentTimeMillis()}")
                put("type", "REDEEMED")
                put("amount", pointCost)
                put("description", "Redeemed ${if (hours == 0) "10m" else "${hours}h"} Protection Package")
                put("dateFormatted", today)
            }
            array.put(obj)
            preferences[PreferencesKeys.POINTS_HISTORY_JSON] = array.toString()
        }

        return statusMessage
    }

    suspend fun wipeAllPointsData() {
        context.astraDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
