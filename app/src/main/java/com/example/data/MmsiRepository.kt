package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MmsiRepository(private val mmsiDao: MmsiDao) {

    suspend fun ensureDatabaseSeeded() {
        withContext(Dispatchers.IO) {
            val count = mmsiDao.getCount()
            if (count == 0) {
                mmsiDao.insertAll(MmsiDataSeed.allCodes)
            }
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

    // Direct synchronous in-memory fallback helper
    fun findInMemory(mid: String): MmsiEntity? = MmsiDataSeed.findByMid(mid)
}
