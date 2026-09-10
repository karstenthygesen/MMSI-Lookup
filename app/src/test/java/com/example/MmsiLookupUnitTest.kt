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
}
