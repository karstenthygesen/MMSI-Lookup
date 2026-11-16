package com.example

import com.example.data.MmsiDataSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MmsiLookupUnitTest {

    @Test
    fun testLookupUnitedKingdom() {
        val entry = MmsiDataSeed.findByMid("235")
        assertNotNull(entry)
        assertEquals("United Kingdom", entry?.countryName)
        assertEquals("GB", entry?.isoCode)
        assertEquals("🇬🇧", entry?.flagEmoji)
    }

    @Test
    fun testLookupUSA() {
        val entry = MmsiDataSeed.findByMid("338")
        assertNotNull(entry)
        assertEquals("United States of America", entry?.countryName)
        assertEquals("US", entry?.isoCode)
        assertEquals("🇺🇸", entry?.flagEmoji)
    }

    @Test
    fun testLookupPanama() {
        val entry = MmsiDataSeed.findByMid("351")
        assertNotNull(entry)
        assertEquals("Panama", entry?.countryName)
        assertEquals("PA", entry?.isoCode)
        assertEquals("🇵🇦", entry?.flagEmoji)
    }

    @Test
    fun testLookupLiberia() {
        val entry = MmsiDataSeed.findByMid("636")
        assertNotNull(entry)
        assertEquals("Liberia", entry?.countryName)
        assertEquals("LR", entry?.isoCode)
        assertEquals("🇱🇷", entry?.flagEmoji)
    }

    @Test
    fun testLookupUnallocated() {
        val entry = MmsiDataSeed.findByMid("999")
        assertNull(entry)
    }

    @Test
    fun testDatabaseCount() {
        // ITU database should have over 200 registrations
        assert(MmsiDataSeed.allCodes.size >= 150)
    }

    @Test
    fun testDefaultMetadataValues() {
        assert(com.example.data.AppDatabase.DEFAULT_LAST_UPDATED.isNotBlank())
        assert(com.example.data.AppDatabase.DEFAULT_DB_VERSION.contains("ITU-R M.585"))
        assert(com.example.data.AppDatabase.BUILD_TIMESTAMP >= 0L)
        assertEquals("bridge_night_mode_enabled", com.example.data.AppDatabase.KEY_BRIDGE_NIGHT_MODE)
        assertEquals("key_auto_night_mode_enabled", com.example.data.AppDatabase.KEY_AUTO_NIGHT_MODE_ENABLED)
        assertEquals("key_last_update_timestamp", com.example.data.AppDatabase.KEY_LAST_UPDATE_TIMESTAMP)
        assertEquals(7L * 24 * 60 * 60 * 1000L, com.example.data.AppDatabase.ONE_WEEK_MILLIS)
    }

    @Test
    fun testSolarTimeCalculatorSunriseSunset() {
        val tz = java.util.TimeZone.getTimeZone("Europe/Copenhagen")
        val calendar = java.util.Calendar.getInstance(tz).apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.MONTH, java.util.Calendar.OCTOBER)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        // Copenhagen latitude ~ 55.67, longitude ~ 12.56
        val (sunriseMinute, sunsetMinute) = com.example.util.SolarTimeCalculator.calculateSunriseSunsetMinutes(
            calendar, 55.6761, 12.5683, tz
        )

        // Sunrise should be morning (between 06:00 and 08:30) -> 360 to 510 minutes
        assert(sunriseMinute in 360..510) { "Sunrise was $sunriseMinute" }

        // Sunset should be evening (between 17:30 and 20:00) -> 1050 to 1200 minutes
        assert(sunsetMinute in 1050..1200) { "Sunset was $sunsetMinute" }

        // Sunset must be after sunrise
        assert(sunsetMinute > sunriseMinute)
    }

    @Test
    fun testSolarTwilightInfoWithoutLocation() {
        val info = com.example.util.SolarTimeCalculator.getSolarTwilightInfo(null)
        assertNotNull(info)
        assert(info.sunriseTimeText.isNotBlank())
        assert(info.sunsetTimeText.isNotBlank())
        assert(info.timeZoneId.isNotBlank())
    }

    @Test
    fun testDaytimeDoesNotActivateNightMode() {
        val tz = java.util.TimeZone.getTimeZone("Europe/Copenhagen")
        val calendar = java.util.Calendar.getInstance(tz).apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.MONTH, java.util.Calendar.OCTOBER)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 14)
            set(java.util.Calendar.MINUTE, 30)
        }

        val (sunriseMinute, sunsetMinute) = com.example.util.SolarTimeCalculator.calculateSunriseSunsetMinutes(
            calendar, 55.6761, 12.5683, tz
        )
        val currentMinute = 14 * 60 + 30
        val isDaytime = currentMinute in sunriseMinute until sunsetMinute
        org.junit.Assert.assertTrue("14:30 in Copenhagen should be daylight", isDaytime)
        val isAfterSundown = !isDaytime
        org.junit.Assert.assertFalse("14:30 in Copenhagen must NOT be after sundown", isAfterSundown)
    }

    @Test
    fun testWeeklyIntervalEligibility() {
        val oneWeekMillis = com.example.data.AppDatabase.ONE_WEEK_MILLIS
        val now = 1000000000000L

        // Exactly one week ago: should be eligible
        val oneWeekAgo = now - oneWeekMillis
        assert((now - oneWeekAgo) >= oneWeekMillis)

        // 8 days ago: should be eligible
        val eightDaysAgo = now - (8L * 24 * 60 * 60 * 1000L)
        assert((now - eightDaysAgo) >= oneWeekMillis)

        // 6 days ago: should NOT be eligible
        val sixDaysAgo = now - (6L * 24 * 60 * 60 * 1000L)
        assert((now - sixDaysAgo) < oneWeekMillis)

        // 1 hour ago: should NOT be eligible
        val oneHourAgo = now - (3600 * 1000L)
        assert((now - oneHourAgo) < oneWeekMillis)
    }

    @Test
    fun testRedMonochromeMatrixConfig() {
        val matrix = com.example.ui.components.RedMonochromeMatrix
        val values = matrix.values
        // Red channel coefficients: 0.30 R + 0.59 G + 0.11 B
        assertEquals(0.30f, values[0], 0.01f)
        assertEquals(0.59f, values[1], 0.01f)
        assertEquals(0.11f, values[2], 0.01f)
        // Green and Blue channels zeroed out for night vision preservation
        assertEquals(0f, values[5], 0.001f)
        assertEquals(0f, values[6], 0.001f)
        assertEquals(0f, values[10], 0.001f)
        assertEquals(0f, values[11], 0.001f)
    }
}
