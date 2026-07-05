package com.teslamatelink.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Legacy accent palette -- kept for backward compatibility.
 * Prefer [CarColorPalettes.forExteriorColor] for new code.
 */
data class CarAccentPalette(
    val accent: Color,
    val accentDim: Color,
    val progressTrack: Color
)

/**
 * Legacy function -- delegates to [CarColorPalettes.forExteriorColor]
 * and extracts accent colors from the full palette.
 */
fun accentForExteriorColor(exteriorColor: String?, isDark: Boolean): CarAccentPalette {
    val palette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme = isDark)
    return CarAccentPalette(
        accent = palette.accent,
        accentDim = palette.accentDim,
        progressTrack = palette.progressTrack
    )
}

/**
 * CompositionLocal providing the resolved CarColorPalette for the current car.
 * Wire this via CompositionLocalProvider in screens that display car-specific theming.
 */
val LocalCarAccent = staticCompositionLocalOf {
    CarAccentPalette(Color(0xFF1A73E8), Color(0xFF1A73E8).copy(0.3f), Color.LightGray)
}

// ── Stitch typography tokens ──────────────────────────────────────────────────
// Inter is the default sans family on Android; we reference it via the system
// default FontFamily so we don't need to bundle Inter ourselves (it ships with
// the OS / Google Sans stack). JetBrains Mono is loaded from R.font.* once
// Task 3 lands the font files.

// Fallback until Task 3 adds font files
val JetBrainsMonoFamily: FontFamily = FontFamily.Monospace
// TODO: Task 3 - replace with:
// val JetBrainsMonoFamily = FontFamily(
//     Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
//     Font(R.font.jetbrains_mono_medium, FontWeight.Medium)
// )

private val InterFamily: FontFamily = FontFamily.Default

/**
 * Stitch typography scale. Mirrors shared/design-tokens.json:
 *   - displayLarge   → Inter 32 / 700 / -0.02em / 1.2
 *   - headlineMedium → Inter 24 / 600 / -0.01em / 1.3
 *   - bodyLarge      → Inter 16 / 400 / 0      / 1.6
 *   - bodySmall      → Inter 14 / 400 / 0      / 1.5
 *   - labelSmall     → Inter 12 / 700 / 0.05em / 1.0  (caps)
 */
val StitchTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.64).sp,    // -0.02em * 32
        lineHeight = 38.sp             // 1.2 * 32
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.24).sp,    // -0.01em * 24
        lineHeight = 31.sp             // 1.3 * 24
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 26.sp             // 1.6 * 16
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.sp,
        lineHeight = 21.sp             // 1.5 * 14
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,        // 0.05em * 12
        lineHeight = 12.sp             // 1.0 * 12
    )
)

// ── Stitch color scheme ───────────────────────────────────────────────────────
private val StitchLightColors = lightColorScheme(
    primary = StitchColors.Primary,
    onPrimary = StitchColors.OnPrimary,
    primaryContainer = StitchColors.PrimaryContainer,
    onPrimaryContainer = StitchColors.OnPrimaryContainer,
    secondary = StitchColors.Secondary,
    onSecondary = StitchColors.OnSecondary,
    secondaryContainer = StitchColors.SecondaryContainer,
    onSecondaryContainer = StitchColors.OnSecondaryContainer,
    tertiary = StitchColors.Tertiary,
    onTertiary = StitchColors.OnTertiary,
    tertiaryContainer = StitchColors.TertiaryContainer,
    onTertiaryContainer = StitchColors.OnTertiaryContainer,
    error = StitchColors.Error,
    onError = StitchColors.OnError,
    errorContainer = StitchColors.ErrorContainer,
    onErrorContainer = StitchColors.OnErrorContainer,
    background = StitchColors.Background,
    onBackground = StitchColors.OnSurface,
    surface = StitchColors.Surface,
    onSurface = StitchColors.OnSurface,
    surfaceVariant = StitchColors.SurfaceContainerHighest,
    onSurfaceVariant = StitchColors.OnSurfaceVariant,
    outline = StitchColors.Outline,
    outlineVariant = StitchColors.OutlineVariant,
    inverseSurface = StitchColors.InverseSurface,
    inverseOnSurface = StitchColors.InverseOnSurface,
    inversePrimary = StitchColors.InversePrimary,
    surfaceTint = StitchColors.SurfaceTint
)

private val StitchDarkColors = darkColorScheme(
    primary = StitchColors.InversePrimary,
    onPrimary = StitchColors.OnPrimary,
    primaryContainer = StitchColors.PrimaryContainer,
    onPrimaryContainer = StitchColors.OnPrimaryContainer,
    secondary = StitchColors.SecondaryContainer,
    onSecondary = StitchColors.OnSecondary,
    secondaryContainer = StitchColors.SecondaryContainer,
    onSecondaryContainer = StitchColors.OnSecondaryContainer,
    tertiary = StitchColors.TertiaryContainer,
    onTertiary = StitchColors.OnTertiary,
    tertiaryContainer = StitchColors.TertiaryContainer,
    onTertiaryContainer = StitchColors.OnTertiaryContainer,
    error = StitchColors.Error,
    onError = StitchColors.OnError,
    errorContainer = StitchColors.ErrorContainer,
    onErrorContainer = StitchColors.OnErrorContainer,
    background = StitchColors.InverseSurface,
    onBackground = StitchColors.InverseOnSurface,
    surface = StitchColors.InverseSurface,
    onSurface = StitchColors.InverseOnSurface,
    surfaceVariant = StitchColors.SurfaceContainerHighest,
    onSurfaceVariant = StitchColors.OnSurfaceVariant,
    outline = StitchColors.Outline,
    outlineVariant = StitchColors.OutlineVariant,
    inverseSurface = StitchColors.Surface,
    inverseOnSurface = StitchColors.OnSurface,
    inversePrimary = StitchColors.Primary,
    surfaceTint = StitchColors.SurfaceTint
)

/**
 * Main app theme composable.
 *
 * Uses the Stitch white-minimal palette (light scheme is the source of truth).
 * Dark theme falls back to an inverted variant of the same palette; dynamic
 * color (Material You) is still honored when [dynamicColor] is true on Android 12+.
 *
 * @param darkTheme Whether to use dark color scheme (defaults to system setting).
 * @param dynamicColor Whether to use Material You dynamic colors (Android 12+).
 * @param exteriorColor Optional car exterior color for palette customization.
 *   When provided, [LocalCarColorPalette] is populated via [CarColorPalettes.forExteriorColor].
 */
@Composable
fun MateLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    exteriorColor: String? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StitchDarkColors
        else -> StitchLightColors
    }
    val carAccent = accentForExteriorColor(exteriorColor, darkTheme)
    val carColorPalette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme)

    CompositionLocalProvider(
        LocalCarAccent provides carAccent,
        LocalCarColorPalette provides carColorPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StitchTypography,
            content = content
        )
    }
}
