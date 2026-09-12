package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MmsiEntity::class, RecentLookupEntity::class, AppSettingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mmsiDao(): MmsiDao

    companion object {
        const val KEY_LAST_UPDATED = "key_last_updated"
        const val KEY_DB_VERSION = "key_db_version"
        const val KEY_BUILD_TIMESTAMP = "key_build_timestamp"

        val DEFAULT_LAST_UPDATED: String
            get() = try { BuildConfig.BUILD_DATE } catch (e: Throwable) { "September 12, 2026" }

        val DEFAULT_DB_VERSION: String
            get() = try { BuildConfig.DB_RELEASE_VERSION } catch (e: Throwable) { "2026.09 (ITU-R M.585-9)" }

        val BUILD_TIMESTAMP: Long
            get() = try { BuildConfig.BUILD_TIMESTAMP } catch (e: Throwable) { 0L }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mmsi_maritime.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = INSTANCE?.mmsiDao() ?: return@launch
                                dao.insertAll(MmsiDataSeed.allCodes)
                                dao.setSetting(AppSettingEntity(KEY_LAST_UPDATED, DEFAULT_LAST_UPDATED))
                                dao.setSetting(AppSettingEntity(KEY_DB_VERSION, DEFAULT_DB_VERSION))
                                dao.setSetting(AppSettingEntity(KEY_BUILD_TIMESTAMP, BUILD_TIMESTAMP.toString()))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
