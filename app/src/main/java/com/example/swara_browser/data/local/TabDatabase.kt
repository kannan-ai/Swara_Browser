package com.example.swara_browser.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "browser_tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val position: Int,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface TabDao {
    @Query("SELECT * FROM browser_tabs ORDER BY position ASC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM browser_tabs ORDER BY position ASC")
    suspend fun getTabsSnapshot(): List<TabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTab(tab: TabEntity)

    @Query("DELETE FROM browser_tabs WHERE id = :tabId")
    suspend fun deleteTab(tabId: String)

    @Query("DELETE FROM browser_tabs")
    suspend fun clearAllTabs()
}

class DataStoreTabDao(context: Context) : TabDao {
    private val tabSessionPrefs = TabSessionPreferences(context)

    override fun getAllTabs(): Flow<List<TabEntity>> {
        return flow { emit(getTabsSnapshot()) }
    }

    override suspend fun getTabsSnapshot(): List<TabEntity> {
        val json = tabSessionPrefs.rawTabJsonFlow.firstOrNull() ?: ""
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<TabEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TabEntity(
                        id = obj.optString("id", "tab_$i"),
                        url = obj.optString("url", ""),
                        title = obj.optString("title", "New Tab"),
                        position = obj.optInt("position", i),
                        lastAccessedTimestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedBy { it.position }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun insertOrUpdateTab(tab: TabEntity) {
        val current = getTabsSnapshot().toMutableList()
        current.removeAll { it.id == tab.id }
        current.add(tab)
        current.sortBy { it.position }

        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("url", t.url)
                put("title", t.title)
                put("position", t.position)
                put("timestamp", t.lastAccessedTimestamp)
            }
            array.put(obj)
        }
        tabSessionPrefs.saveRawTabJson(array.toString())
    }

    override suspend fun deleteTab(tabId: String) {
        val current = getTabsSnapshot().filter { it.id != tabId }
        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("url", t.url)
                put("title", t.title)
                put("position", t.position)
                put("timestamp", t.lastAccessedTimestamp)
            }
            array.put(obj)
        }
        tabSessionPrefs.saveRawTabJson(array.toString())
    }

    override suspend fun clearAllTabs() {
        tabSessionPrefs.saveRawTabJson("")
    }
}

@Database(entities = [TabEntity::class], version = 1, exportSchema = false)
abstract class TabDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: TabDatabase? = null

        fun getDatabase(context: Context): TabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TabDatabase::class.java,
                    "swara_browser_tabs.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object TabDatabaseFactory {
    fun getDao(context: Context): TabDao {
        return DataStoreTabDao(context.applicationContext)
    }
}
