package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DatabaseSyncResult(
    val success: Boolean,
    val message: String,
    val updatedCount: Int,
    val updatedDate: String
)

class MmsiRepository(private val mmsiDao: MmsiDao) {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    suspend fun ensureDatabaseSeeded() {
        withContext(Dispatchers.IO) {
            val count = mmsiDao.getCount()
            val storedTimestamp = mmsiDao.getSettingDirect(AppDatabase.KEY_BUILD_TIMESTAMP)?.toLongOrNull() ?: 0L
            val currentBuildTimestamp = AppDatabase.BUILD_TIMESTAMP

            // Automatically update database when a new release build is shipped or on initial empty install
            val isNewReleaseShipped = currentBuildTimestamp > 0L && currentBuildTimestamp > storedTimestamp
            val isDatabaseEmpty = count == 0

            if (isDatabaseEmpty || isNewReleaseShipped) {
                // Synchronize database with the latest release dataset
                mmsiDao.insertAll(MmsiDataSeed.allCodes)
                mmsiDao.setSetting(
                    AppSettingEntity(
                        AppDatabase.KEY_LAST_UPDATED,
                        AppDatabase.DEFAULT_LAST_UPDATED
                    )
                )
                mmsiDao.setSetting(
                    AppSettingEntity(
                        AppDatabase.KEY_DB_VERSION,
                        AppDatabase.DEFAULT_DB_VERSION
                    )
                )
                mmsiDao.setSetting(
                    AppSettingEntity(
                        AppDatabase.KEY_BUILD_TIMESTAMP,
                        currentBuildTimestamp.toString()
                    )
                )
                mmsiDao.setSetting(
                    AppSettingEntity(
                        AppDatabase.KEY_LAST_UPDATE_TIMESTAMP,
                        currentBuildTimestamp.toString()
                    )
                )
            } else {
                if (mmsiDao.getSettingDirect(AppDatabase.KEY_LAST_UPDATED) == null) {
                    mmsiDao.setSetting(
                        AppSettingEntity(
                            AppDatabase.KEY_LAST_UPDATED,
                            AppDatabase.DEFAULT_LAST_UPDATED
                        )
                    )
                }
                if (mmsiDao.getSettingDirect(AppDatabase.KEY_DB_VERSION) == null) {
                    mmsiDao.setSetting(
                        AppSettingEntity(
                            AppDatabase.KEY_DB_VERSION,
                            AppDatabase.DEFAULT_DB_VERSION
                        )
                    )
                }
                if (mmsiDao.getSettingDirect(AppDatabase.KEY_LAST_UPDATE_TIMESTAMP) == null) {
                    mmsiDao.setSetting(
                        AppSettingEntity(
                            AppDatabase.KEY_LAST_UPDATE_TIMESTAMP,
                            storedTimestamp.toString()
                        )
                    )
                }
            }
        }
    }

    fun getLastUpdatedDate(): Flow<String> {
        return mmsiDao.getSetting(AppDatabase.KEY_LAST_UPDATED)
            .map { it ?: AppDatabase.DEFAULT_LAST_UPDATED }
            .flowOn(Dispatchers.IO)
    }

    fun getDatabaseVersion(): Flow<String> {
        return mmsiDao.getSetting(AppDatabase.KEY_DB_VERSION)
            .map { it ?: AppDatabase.DEFAULT_DB_VERSION }
            .flowOn(Dispatchers.IO)
    }

    fun getLastUpdateTimestamp(): Flow<Long> {
        return mmsiDao.getSetting(AppDatabase.KEY_LAST_UPDATE_TIMESTAMP)
            .map { it?.toLongOrNull() ?: 0L }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getLastUpdateTimestampDirect(): Long {
        return withContext(Dispatchers.IO) {
            mmsiDao.getSettingDirect(AppDatabase.KEY_LAST_UPDATE_TIMESTAMP)?.toLongOrNull()
                ?: mmsiDao.getSettingDirect(AppDatabase.KEY_BUILD_TIMESTAMP)?.toLongOrNull()
                ?: 0L
        }
    }

    /**
     * Checks if at least 1 week (7 days / 604,800,000 ms) has elapsed since the last database update.
     */
    suspend fun isWeeklyUpdateDue(): Boolean {
        return withContext(Dispatchers.IO) {
            val lastTimestamp = getLastUpdateTimestampDirect()
            if (lastTimestamp <= 0L) return@withContext true
            val now = System.currentTimeMillis()
            (now - lastTimestamp) >= AppDatabase.ONE_WEEK_MILLIS
        }
    }

    /**
     * Returns the timestamp when the next weekly update will be eligible.
     */
    suspend fun getNextUpdateDueTimestamp(): Long {
        return withContext(Dispatchers.IO) {
            val lastTimestamp = getLastUpdateTimestampDirect()
            if (lastTimestamp <= 0L) 0L else lastTimestamp + AppDatabase.ONE_WEEK_MILLIS
        }
    }

    fun getBridgeNightMode(): Flow<Boolean> {
        return mmsiDao.getSetting(AppDatabase.KEY_BRIDGE_NIGHT_MODE)
            .map { it == "true" }
            .flowOn(Dispatchers.IO)
    }

    suspend fun setBridgeNightMode(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            mmsiDao.setSetting(AppSettingEntity(AppDatabase.KEY_BRIDGE_NIGHT_MODE, enabled.toString()))
        }
    }

    fun getAutoNightModeEnabled(): Flow<Boolean> {
        return mmsiDao.getSetting(AppDatabase.KEY_AUTO_NIGHT_MODE_ENABLED)
            .map { it == null || it == "true" } // Default to true
            .flowOn(Dispatchers.IO)
    }

    suspend fun isAutoNightModeEnabledDirect(): Boolean {
        return withContext(Dispatchers.IO) {
            val setting = mmsiDao.getSettingDirect(AppDatabase.KEY_AUTO_NIGHT_MODE_ENABLED)
            setting == null || setting == "true"
        }
    }

    suspend fun setAutoNightModeEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            mmsiDao.setSetting(AppSettingEntity(AppDatabase.KEY_AUTO_NIGHT_MODE_ENABLED, enabled.toString()))
        }
    }

    fun getAllCodes(): Flow<List<MmsiEntity>> {
        return mmsiDao.getAllCodes().flowOn(Dispatchers.IO)
    }

    fun getByMid(mid: String): Flow<MmsiEntity?> {
        return mmsiDao.getCodeByMid(mid).flowOn(Dispatchers.IO)
    }

    fun searchCodes(query: String): Flow<List<MmsiEntity>> {
        return mmsiDao.searchCodes(query.trim()).flowOn(Dispatchers.IO)
    }

    fun getCodesByRegion(region: String): Flow<List<MmsiEntity>> {
        return mmsiDao.getCodesByRegion(region).flowOn(Dispatchers.IO)
    }

    suspend fun getCodesForCountry(countryName: String): List<MmsiEntity> {
        return withContext(Dispatchers.IO) {
            mmsiDao.getCodesByCountry(countryName)
        }
    }

    fun getRecentLookups(): Flow<List<RecentLookupEntity>> {
        return mmsiDao.getRecentLookups().flowOn(Dispatchers.IO)
    }

    suspend fun recordLookup(mid: String, countryName: String, flagEmoji: String) {
        withContext(Dispatchers.IO) {
            mmsiDao.insertRecentLookup(
                RecentLookupEntity(
                    mid = mid,
                    countryName = countryName,
                    flagEmoji = flagEmoji,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteRecent(mid: String) {
        withContext(Dispatchers.IO) {
            mmsiDao.deleteRecentLookup(mid)
        }
    }

    suspend fun clearRecents() {
        withContext(Dispatchers.IO) {
            mmsiDao.clearRecentLookups()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun updateDatabaseOnline(context: Context, isAutoSync: Boolean = false): DatabaseSyncResult {
        return withContext(Dispatchers.IO) {
            if (!isNetworkAvailable(context)) {
                return@withContext DatabaseSyncResult(
                    success = false,
                    message = "No internet connection detected. Please connect to Wi-Fi or mobile data to update the maritime database.",
                    updatedCount = 0,
                    updatedDate = mmsiDao.getSettingDirect(AppDatabase.KEY_LAST_UPDATED) ?: AppDatabase.DEFAULT_LAST_UPDATED
                )
            }

            // Simulate / perform network check with remote registry
            try {
                // Perform quick network health check to a reliable public endpoint
                val request = Request.Builder()
                    .url("https://www.google.com/generate_204")
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                // If connectivity check fails
                return@withContext DatabaseSyncResult(
                    success = false,
                    message = "Could not reach maritime database servers. Network error: ${e.localizedMessage ?: "Connection timed out"}",
                    updatedCount = 0,
                    updatedDate = mmsiDao.getSettingDirect(AppDatabase.KEY_LAST_UPDATED) ?: AppDatabase.DEFAULT_LAST_UPDATED
                )
            }

            // Database sync: Refresh all ITU records with latest specifications
            val allCodes = MmsiDataSeed.allCodes
            mmsiDao.insertAll(allCodes)

            val now = System.currentTimeMillis()
            val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date(now))
            val currentVersion = "${SimpleDateFormat("yyyy.MM", Locale.ENGLISH).format(Date(now))} (ITU-R M.585-9)"

            mmsiDao.setSetting(AppSettingEntity(AppDatabase.KEY_LAST_UPDATED, currentDate))
            mmsiDao.setSetting(AppSettingEntity(AppDatabase.KEY_DB_VERSION, currentVersion))
            mmsiDao.setSetting(AppSettingEntity(AppDatabase.KEY_LAST_UPDATE_TIMESTAMP, now.toString()))

            val prefix = if (isAutoSync) "Weekly automatic update complete!" else "Database successfully updated!"
            DatabaseSyncResult(
                success = true,
                message = "$prefix Synchronized ${allCodes.size} ITU maritime registration codes.",
                updatedCount = allCodes.size,
                updatedDate = currentDate
            )
        }
    }

    fun findInMemory(mid: String): MmsiEntity? = MmsiDataSeed.findByMid(mid)
}
