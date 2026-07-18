package com.mehei.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════
// MEHEI Light Theme — Deep henna maroon, terracotta, gold
// ═══════════════════════════════════════════════════════════════
private val MeheiLightColorScheme = lightColorScheme(
    primary = MeheiPrimary,
    onPrimary = MeheiOnPrimary,
    primaryContainer = MeheiPrimaryLight,
    onPrimaryContainer = MeheiPrimaryDark,

    secondary = MeheiSecondary,
    onSecondary = MeheiOnSecondary,
    secondaryContainer = MeheiSecondaryLight,
    onSecondaryContainer = MeheiSecondaryDark,

    tertiary = MeheiTertiary,
    onTertiary = MeheiOnTertiary,
    tertiaryContainer = MeheiTertiaryLight,
    onTertiaryContainer = MeheiTertiaryDark,

    background = MeheiBackground,
    onBackground = MeheiOnBackground,
    surface = MeheiSurface,
    onSurface = MeheiOnSurface,
    surfaceVariant = MeheiSurfaceVariant,
    onSurfaceVariant = MeheiOnSurfaceVar,

    outline = MeheiOutline,
    outlineVariant = MeheiOutlineVariant,

    error = MeheiError,
    onError = MeheiOnError,
    errorContainer = MeheiErrorContainer,
    onErrorContainer = MeheiError,
)

// ═══════════════════════════════════════════════════════════════
// MEHEI Dark Theme — Warm charcoal with henna accents
// ═══════════════════════════════════════════════════════════════
private val MeheiDarkColorScheme = darkColorScheme(
    primary = MeheiPrimaryLight,
    onPrimary = MeheiPrimaryDark,
    primaryContainer = MeheiPrimary,
    onPrimaryContainer = MeheiPrimaryLight,

    secondary = MeheiSecondaryLight,
    onSecondary = MeheiSecondaryDark,
    secondaryContainer = MeheiSecondary,
    onSecondaryContainer = MeheiSecondaryLight,

    tertiary = MeheiTertiaryLight,
    onTertiary = MeheiTertiaryDark,
    tertiaryContainer = MeheiTertiary,
    onTertiaryContainer = MeheiTertiaryLight,

    background = MeheiDarkBackground,
    onBackground = MeheiDarkOnBg,
    surface = MeheiDarkSurface,
    onSurface = MeheiDarkOnSurface,
    surfaceVariant = MeheiDarkSurfaceVar,
    onSurfaceVariant = MeheiOnSurfaceVar,

    outline = MeheiDarkOutline,
    outlineVariant = MeheiDarkOutline,

    error = MeheiError,
    onError = MeheiOnError,
    errorContainer = MeheiErrorContainer,
    onErrorContainer = MeheiError,
)

// ═══════════════════════════════════════════════════════════════
// MEHEI Shape System — 8dp grid, rounded aesthetic
// ═══════════════════════════════════════════════════════════════
val MeheiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),        // Cards, chips
    large = RoundedCornerShape(16.dp),         // Large cards
    extraLarge = RoundedCornerShape(24.dp),    // Bottom sheets
)

@Composable
fun MeheiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to preserve MEHEI's branded palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MeheiDarkColorScheme
        else -> MeheiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MeheiTypography,
        shapes = MeheiShapes,
        content = content,
    )
}
