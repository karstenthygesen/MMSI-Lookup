package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MmsiDao {
    @Query("SELECT * FROM mmsi_codes ORDER BY mid ASC")
    fun getAllCodes(): Flow<List<MmsiEntity>>

    @Query("SELECT * FROM mmsi_codes WHERE mid = :mid LIMIT 1")
    fun getCodeByMid(mid: String): Flow<MmsiEntity?>

    @Query("SELECT * FROM mmsi_codes WHERE mid LIKE :search || '%' OR countryName LIKE '%' || :search || '%' OR isoCode LIKE :search || '%' ORDER BY mid ASC")
    fun searchCodes(search: String): Flow<List<MmsiEntity>>

    @Query("SELECT * FROM mmsi_codes WHERE countryName = :countryName ORDER BY mid ASC")
    suspend fun getCodesByCountry(countryName: String): List<MmsiEntity>

    @Query("SELECT * FROM mmsi_codes WHERE region = :region ORDER BY mid ASC")
    fun getCodesByRegion(region: String): Flow<List<MmsiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(codes: List<MmsiEntity>)

    @Query("SELECT COUNT(*) FROM mmsi_codes")
    suspend fun getCount(): Int

    // Recent lookups
    @Query("SELECT * FROM recent_lookups ORDER BY timestamp DESC LIMIT 8")
    fun getRecentLookups(): Flow<List<RecentLookupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentLookup(lookup: RecentLookupEntity)

    @Query("DELETE FROM recent_lookups WHERE mid = :mid")
    suspend fun deleteRecentLookup(mid: String)

    @Query("DELETE FROM recent_lookups")
    suspend fun clearRecentLookups()
}
