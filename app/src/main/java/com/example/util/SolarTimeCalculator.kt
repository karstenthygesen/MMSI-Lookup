package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class SolarTwilightInfo(
    val isAfterSundown: Boolean,
    val sunriseTimeText: String,
    val sunsetTimeText: String,
    val timeZoneId: String,
    val statusDescription: String
)

/**
 * Calculates solar sunrise and sunset times based strictly on the device's
 * configured TimeZone and calendar date, without requiring ANY GPS or location permissions.
 *
 * Uses the standard NOAA Solar Position algorithm.
 */
object SolarTimeCalculator {

    // Representative coordinates for global maritime and major time zones
    private val timeZoneCoordinates: Map<String, Pair<Double, Double>> = mapOf(
        // Europe & North Sea / Baltic / Mediterranean
        "Europe/Copenhagen" to Pair(55.6761, 12.5683),
        "Europe/London" to Pair(51.5074, -0.1278),
        "Europe/Berlin" to Pair(52.5200, 13.4050),
        "Europe/Paris" to Pair(48.8566, 2.3522),
        "Europe/Amsterdam" to Pair(52.3676, 4.9041),
        "Europe/Oslo" to Pair(59.9139, 10.7522),
        "Europe/Stockholm" to Pair(59.3293, 18.0686),
        "Europe/Helsinki" to Pair(60.1699, 24.9384),
        "Europe/Madrid" to Pair(40.4168, -3.7038),
        "Europe/Lisbon" to Pair(38.7223, -9.1393),
        "Europe/Rome" to Pair(41.9028, 12.4964),
        "Europe/Athens" to Pair(37.9838, 23.7275),
        "Europe/Dublin" to Pair(53.3498, -6.2603),
        "Europe/Brussels" to Pair(50.8503, 4.3517),
        "Europe/Vienna" to Pair(48.2082, 16.3738),
        "Europe/Warsaw" to Pair(52.2297, 21.0122),
        "Europe/Reykjavik" to Pair(64.1466, -21.9426),
        "Atlantic/Reykjavik" to Pair(64.1466, -21.9426),

        // Americas & Caribbean / Panama
        "America/New_York" to Pair(40.7128, -74.0060),
        "America/Chicago" to Pair(41.8781, -87.6298),
        "America/Denver" to Pair(39.7392, -104.9903),
        "America/Los_Angeles" to Pair(34.0522, -118.2437),
        "America/Anchorage" to Pair(61.2181, -149.9003),
        "America/Honolulu" to Pair(21.3069, -157.8583),
        "America/Toronto" to Pair(43.6532, -79.3832),
        "America/Vancouver" to Pair(49.2827, -123.1207),
        "America/Halifax" to Pair(44.6488, -63.5752),
        "America/Panama" to Pair(8.9824, -79.5199),
        "America/Sao_Paulo" to Pair(-23.5505, -46.6333),
        "America/Buenos_Aires" to Pair(-34.6037, -58.3816),
        "America/Santiago" to Pair(-33.4489, -70.6693),
        "America/Lima" to Pair(-12.0464, -77.0428),
        "America/Bogota" to Pair(4.7110, -74.0721),
        "America/Mexico_City" to Pair(19.4326, -99.1332),

        // Asia & Middle East
        "Asia/Tokyo" to Pair(35.6762, 139.6503),
        "Asia/Singapore" to Pair(1.3521, 103.8198),
        "Asia/Hong_Kong" to Pair(22.3193, 114.1694),
        "Asia/Shanghai" to Pair(31.2304, 121.4737),
        "Asia/Seoul" to Pair(37.5665, 126.9780),
        "Asia/Bangkok" to Pair(13.7563, 100.5018),
        "Asia/Jakarta" to Pair(-6.2088, 106.8456),
        "Asia/Dubai" to Pair(25.2048, 55.2708),
        "Asia/Kolkata" to Pair(22.5726, 88.3639),
        "Asia/Karachi" to Pair(24.8607, 67.0011),

        // Oceania
        "Australia/Sydney" to Pair(-33.8688, 151.2093),
        "Australia/Melbourne" to Pair(-37.8136, 144.9631),
        "Australia/Brisbane" to Pair(-27.4698, 153.0251),
        "Australia/Perth" to Pair(-31.9505, 115.8605),
        "Pacific/Auckland" to Pair(-36.8485, 174.7633),

        // Africa
        "Africa/Cairo" to Pair(30.0444, 31.2357),
        "Africa/Johannesburg" to Pair(-26.2041, 28.0473),
        "Africa/Cape_Town" to Pair(-33.9249, 18.4241),
        "Africa/Lagos" to Pair(6.5244, 3.3792),
        "Africa/Nairobi" to Pair(-1.2921, 36.8219),
        "Africa/Casablanca" to Pair(33.5731, -7.5898)
    )

    fun isAfterSundownBeforeSunrise(context: Context? = null): Boolean {
        val info = getSolarTwilightInfo(context)
        return info.isAfterSundown
    }

    fun getSolarTwilightInfo(context: Context? = null): SolarTwilightInfo {
        val tz = TimeZone.getDefault()
        val calendar = Calendar.getInstance(tz)
        val tzId = tz.id

        val (lat, lon) = getCoordinatesForTimeZone(tz)

        // Calculate solar sunrise and sunset
        val (sunriseMinute, sunsetMinute) = calculateSunriseSunsetMinutes(calendar, lat, lon, tz)

        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val isDaytime = if (sunsetMinute > sunriseMinute) {
            currentMinuteOfDay in sunriseMinute until sunsetMinute
        } else {
            // Polar day / polar night edge cases
            false
        }

        val isAfterSundown = !isDaytime

        val sunriseFormatted = String.format(Locale.ENGLISH, "%02d:%02d", sunriseMinute / 60, sunriseMinute % 60)
        val sunsetFormatted = String.format(Locale.ENGLISH, "%02d:%02d", sunsetMinute / 60, sunsetMinute % 60)

        val status = if (isAfterSundown) {
            "Nighttime (After sundown ~${sunsetFormatted}, sunrise ~${sunriseFormatted})"
        } else {
            "Daylight (Sunrise ~${sunriseFormatted}, sunset ~${sunsetFormatted})"
        }

        return SolarTwilightInfo(
            isAfterSundown = isAfterSundown,
            sunriseTimeText = sunriseFormatted,
            sunsetTimeText = sunsetFormatted,
            timeZoneId = tzId,
            statusDescription = status
        )
    }

    private fun getCoordinatesForTimeZone(timeZone: TimeZone): Pair<Double, Double> {
        val id = timeZone.id
        timeZoneCoordinates[id]?.let { return it }

        // Fallback approximation by timezone ID prefix and offset
        val rawOffsetHours = timeZone.rawOffset / (1000.0 * 60 * 60)
        val lon = (rawOffsetHours * 15.0).coerceIn(-180.0, 180.0)

        val lat = when {
            id.startsWith("Europe") -> 52.0
            id.startsWith("America") -> if (rawOffsetHours < -3) 38.0 else -15.0
            id.startsWith("Asia") -> 30.0
            id.startsWith("Australia") -> -28.0
            id.startsWith("Africa") -> 10.0
            id.startsWith("Pacific") -> -15.0
            else -> 45.0
        }

        return Pair(lat, lon)
    }

    /**
     * Standard NOAA Solar Zenith Sunrise/Sunset calculation.
     * Returns Pair(sunriseMinuteOfDay, sunsetMinuteOfDay).
     */
    fun calculateSunriseSunsetMinutes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone
    ): Pair<Int, Int> {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        val sunriseMin = calculateSolarEventMinute(calendar, latitude, longitude, timeZone, isSunrise = true)
        val sunsetMin = calculateSolarEventMinute(calendar, latitude, longitude, timeZone, isSunrise = false)

        return Pair(sunriseMin, sunsetMin)
    }

    private fun calculateSolarEventMinute(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone,
        isSunrise: Boolean
    ): Int {
        try {
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val lngHour = longitude / 15.0
            val t = if (isSunrise) {
                dayOfYear + ((6.0 - lngHour) / 24.0)
            } else {
                dayOfYear + ((18.0 - lngHour) / 24.0)
            }

            val mDeg = (0.9856 * t) - 3.289
            val mRad = Math.toRadians(mDeg)

            var lDeg = mDeg + (1.916 * sin(mRad)) + (0.020 * sin(2.0 * mRad)) + 282.634
            lDeg = (lDeg % 360.0 + 360.0) % 360.0
            val lRad = Math.toRadians(lDeg)

            var raDeg = Math.toDegrees(kotlin.math.atan(0.91764 * tan(lRad)))
            raDeg = (raDeg % 360.0 + 360.0) % 360.0

            val lQuadrant = floor(lDeg / 90.0) * 90.0
            val raQuadrant = floor(raDeg / 90.0) * 90.0
            raDeg += (lQuadrant - raQuadrant)
            val raHours = raDeg / 15.0

            val sinDec = 0.39782 * sin(lRad)
            val cosDec = cos(asin(sinDec))

            val zenithDeg = 90.8333 // Official sunset/sunrise zenith
            val zenithRad = Math.toRadians(zenithDeg)
            val latRad = Math.toRadians(latitude)

            val cosH = (cos(zenithRad) - (sinDec * sin(latRad))) / (cosDec * cos(latRad))

            if (cosH > 1.0) {
                // Polar night: Sun never rises
                return if (isSunrise) 12 * 60 else 12 * 60
            }
            if (cosH < -1.0) {
                // Midnight sun: Sun never sets
                return if (isSunrise) 0 else 24 * 60
            }

            val hDeg = if (isSunrise) {
                360.0 - Math.toDegrees(acos(cosH))
            } else {
                Math.toDegrees(acos(cosH))
            }
            val hHours = hDeg / 15.0

            val meanTime = hHours + raHours - (0.06571 * t) - 6.622
            var ut = meanTime - lngHour
            ut = (ut % 24.0 + 24.0) % 24.0

            val offsetHours = timeZone.getOffset(calendar.timeInMillis) / 3600000.0
            val localHour = (ut + offsetHours + 24.0) % 24.0

            val totalMinutes = (localHour * 60).toInt().coerceIn(0, 1439)
            return totalMinutes
        } catch (e: Exception) {
            // Default robust fallback: sunrise 06:00, sunset 18:30
            return if (isSunrise) 6 * 60 else 18 * 60 + 30
        }
    }
}
