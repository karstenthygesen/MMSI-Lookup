package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mmsi_codes")
data class MmsiEntity(
    @PrimaryKey val mid: String,
    val countryName: String,
    val isoCode: String,
    val flagEmoji: String,
    val region: String,
    val notes: String = ""
)

@Entity(tableName = "recent_lookups")
data class RecentLookupEntity(
    @PrimaryKey val mid: String,
    val countryName: String,
    val flagEmoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
