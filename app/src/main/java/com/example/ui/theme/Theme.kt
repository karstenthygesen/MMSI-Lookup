package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalBridgeNightMode = compositionLocalOf { false }

// Coast Guard Bridge Night Vision Color Scheme (SCOTOPIC / ROD-SAFE)
// Pure black background for zero AMOLED glow; blood-crimson containers; high-contrast luminous red for all text & UI
private val BridgeNightColorScheme = darkColorScheme(
    primary = BridgeRedLuminous,
    onPrimary = BridgeRedPitchBlack,
    primaryContainer = BridgeRedSurfaceVariant,
    onPrimaryContainer = BridgeRedTextActive,
    secondary = BridgeRedSecondary,
    onSecondary = BridgeRedPitchBlack,
    secondaryContainer = Color(0xFF2D0707),
    onSecondaryContainer = BridgeRedLuminous,
    tertiary = BridgeRedPrimary,
    onTertiary = BridgeRedPitchBlack,
    background = BridgeRedPitchBlack,
    onBackground = BridgeRedPrimary,
    surface = BridgeRedSurface,
    onSurface = BridgeRedPrimary,
    surfaceVariant = BridgeRedSurfaceVariant,
    onSurfaceVariant = BridgeRedMuted,
    outline = BridgeRedOutline,
    outlineVariant = BridgeRedOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = MaritimeCyan,
    onPrimary = OceanNavyDark,
    primaryContainer = OceanNavySurface,
    onPrimaryContainer = MaritimeCyanLight,
    secondary = NauticalGold,
    onSecondary = OceanNavyDark,
    secondaryContainer = Color(0xFF452B00),
    onSecondaryContainer = NauticalGoldLight,
    tertiary = MaritimeBlue,
    onTertiary = Color.White,
    background = OceanNavyDark,
    onBackground = SlateTextPrimary,
    surface = OceanNavyCard,
    onSurface = SlateTextPrimary,
    surfaceVariant = OceanNavySurface,
    onSurfaceVariant = SlateTextSecondary,
    outline = Color(0xFF2E4B72),
    outlineVariant = Color(0xFF1E3554)
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlueLightPrimary,
    onPrimary = Color.White,
    primaryContainer = OceanLightSurfaceVariant,
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = MaritimeBlue,
    onTertiary = Color.White,
    background = OceanLightBackground,
    onBackground = OceanLightText,
    surface = OceanLightSurface,
    onSurface = OceanLightText,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

object MaritimeTheme {
    val isBridgeNight: Boolean
        @Composable get() = LocalBridgeNightMode.current

    val accent: Color
        @Composable get() = if (isBridgeNight) BridgeRedLuminous else MaritimeCyan

    val gold: Color
        @Composable get() = if (isBridgeNight) BridgeRedSecondary else NauticalGold

    val success: Color
        @Composable get() = if (isBridgeNight) BridgeRedTextActive else MaritimeGreen

    val textSecondary: Color
        @Composable get() = if (isBridgeNight) BridgeRedMuted else SlateTextSecondary

    val textMuted: Color
        @Composable get() = if (isBridgeNight) BridgeRedDark else SlateTextMuted

    val border: Color
        @Composable get() = if (isBridgeNight) BridgeRedOutline else Color(0xFF2E4B72)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    bridgeNightMode: Boolean = false,
    dynamicColor: Boolean = false, // Keep distinctive maritime styling by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        bridgeNightMode -> BridgeNightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalBridgeNightMode provides bridgeNightMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
