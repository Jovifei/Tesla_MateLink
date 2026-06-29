package com.teslamatelink.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFF5F6368), onSecondary = Color.White,
    surface = Color(0xFFF8F9FA), onSurface = Color(0xFF1F1F1F),
    background = Color.White, onBackground = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFE8EAED), onSurfaceVariant = Color(0xFF444746),
    outline = Color(0xFF747775), outlineVariant = Color(0xFFC4C7C5),
    error = Color(0xFFDC2626), onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8), onPrimary = Color(0xFF003A70),
    primaryContainer = Color(0xFF004A9F),
    secondary = Color(0xFFC4C7C5), onSecondary = Color(0xFF303234),
    surface = Color(0xFF1E1E1E), onSurface = Color(0xFFE3E3E3),
    background = Color(0xFF121212), onBackground = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF444746), onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E918F), outlineVariant = Color(0xFF444746),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

/**
 * Main app theme composable.
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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val carAccent = accentForExteriorColor(exteriorColor, darkTheme)
    val carColorPalette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme)

    CompositionLocalProvider(
        LocalCarAccent provides carAccent,
        LocalCarColorPalette provides carColorPalette
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
