package com.teslamatelink.ui.theme

import androidx.compose.ui.graphics.Color

// Stitch White-Minimal Palette (from shared/design-tokens.json)
object StitchColors {
    val Background = Color(0xFFFDF8F8)
    val Surface = Color(0xFFFDF8F8)
    val SurfaceDim = Color(0xFFDDD9D8)
    val SurfaceBright = Color(0xFFFDF8F8)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF7F3F2)
    val SurfaceContainer = Color(0xFFF1EDEC)
    val SurfaceContainerHigh = Color(0xFFEBE7E6)
    val SurfaceContainerHighest = Color(0xFFE5E2E1)
    val OnSurface = Color(0xFF1C1B1B)
    val OnSurfaceVariant = Color(0xFF444748)
    val InverseSurface = Color(0xFF313030)
    val InverseOnSurface = Color(0xFFF4F0EF)
    val Outline = Color(0xFF747878)
    val OutlineVariant = Color(0xFFC4C7C7)
    val SurfaceTint = Color(0xFF5F5E5E)
    val Primary = Color(0xFF000000)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1C1B1B)
    val OnPrimaryContainer = Color(0xFF858383)
    val InversePrimary = Color(0xFFC8C6C5)
    val Secondary = Color(0xFF895200)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFB157)
    val OnSecondaryContainer = Color(0xFF734400)
    val Tertiary = Color(0xFF000000)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFF1C1B1A)
    val OnTertiaryContainer = Color(0xFF868382)
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)
    val Accent = Color(0xFFA16207)
    val StatusOnline = Color(0xFF059669)
    val StatusOnlineBg = Color(0xFFD1FAE5)
    val StatusOffline = Color(0xFF747878)
    val StatusOfflineBg = Color(0xFFF3F4F6)
    val StatusCharging = Color(0xFFD97706)
    val StatusChargingBg = Color(0xFFFEF3C7)
    val StatusError = Color(0xFFBA1A1A)
    val StatusErrorBg = Color(0xFFFFDAD6)
    val Border = Color(0xFFE5E5E5)
    val White = Color(0xFFFFFFFF)
}

// ── Legacy aliases (back-compat) ──────────────────────────────────────────────
// Mapped onto the Stitch palette so existing call sites keep compiling while
// screens migrate to StitchColors. Prefer StitchColors.* for new code.
//
// Car exterior colors (kept verbatim — they describe real Tesla paint codes
// and have no Stitch equivalent).
val CarDeepBlue = Color(0xFF1E3A8A)
val CarRedMultiCoat = Color(0xFFB91C1C)
val CarUltraRed = Color(0xFFDC2626)
val CarPearlWhite = Color(0xFFF5F5F0)
val CarSolidBlack = Color(0xFF1A1A1A)
val CarMidnightSilver = Color(0xFF374151)
val CarQuicksilver = Color(0xFF9CA3AF)
val CarStealthGrey = Color(0xFF4B5563)
val CarMidnightCherryRed = Color(0xFF7F1D1D)

// Light scheme aliases → Stitch equivalents
val PrimaryLight get() = StitchColors.Primary
val OnPrimaryLight get() = StitchColors.OnPrimary
val PrimaryContainerLight get() = StitchColors.PrimaryContainer
val OnPrimaryContainerLight get() = StitchColors.OnPrimaryContainer
val SecondaryLight get() = StitchColors.Secondary
val OnSecondaryLight get() = StitchColors.OnSecondary
val SecondaryContainerLight get() = StitchColors.SecondaryContainer
val OnSecondaryContainerLight get() = StitchColors.OnSecondaryContainer
val TertiaryLight get() = StitchColors.Tertiary
val OnTertiaryLight get() = StitchColors.OnTertiary
val BackgroundLight get() = StitchColors.Background
val OnBackgroundLight get() = StitchColors.OnSurface
val SurfaceLight get() = StitchColors.Surface
val OnSurfaceLight get() = StitchColors.OnSurface
val SurfaceVariantLight get() = StitchColors.SurfaceContainerHighest
val OnSurfaceVariantLight get() = StitchColors.OnSurfaceVariant
val OutlineLight get() = StitchColors.Outline
val OutlineVariantLight get() = StitchColors.OutlineVariant
val SurfaceContainerLight get() = StitchColors.SurfaceContainer
val SurfaceContainerHighLight get() = StitchColors.SurfaceContainerHigh
val SurfaceContainerHighestLight get() = StitchColors.SurfaceContainerHighest

// Dark scheme aliases → Stitch light palette (dark theme is being deprecated
// in the white-minimal design; these keep old call sites compiling).
val PrimaryDark get() = StitchColors.InversePrimary
val OnPrimaryDark get() = StitchColors.OnPrimary
val PrimaryContainerDark get() = StitchColors.PrimaryContainer
val OnPrimaryContainerDark get() = StitchColors.OnPrimaryContainer
val SecondaryDark get() = StitchColors.SecondaryContainer
val OnSecondaryDark get() = StitchColors.OnSecondary
val SecondaryContainerDark get() = StitchColors.SecondaryContainer
val OnSecondaryContainerDark get() = StitchColors.OnSecondaryContainer
val TertiaryDark get() = StitchColors.TertiaryContainer
val OnTertiaryDark get() = StitchColors.OnTertiary
val BackgroundDark get() = StitchColors.InverseSurface
val OnBackgroundDark get() = StitchColors.InverseOnSurface
val SurfaceDark get() = StitchColors.InverseSurface
val OnSurfaceDark get() = StitchColors.InverseOnSurface
val SurfaceVariantDark get() = StitchColors.SurfaceContainerHighest
val OnSurfaceVariantDark get() = StitchColors.OnSurfaceVariant
val OutlineDark get() = StitchColors.Outline
val OutlineVariantDark get() = StitchColors.OutlineVariant
val SurfaceContainerDark get() = StitchColors.SurfaceContainer
val SurfaceContainerHighDark get() = StitchColors.SurfaceContainerHigh
val SurfaceContainerHighestDark get() = StitchColors.SurfaceContainerHighest

val ErrorLight get() = StitchColors.Error
val OnErrorLight get() = StitchColors.OnError
val ErrorDark get() = StitchColors.Error
val OnErrorDark get() = StitchColors.OnError

// ── State colors (mapped onto Stitch status palette) ──
val StateOnline get() = StitchColors.StatusOnline
val StateCharging get() = StitchColors.StatusCharging
val StateDriving get() = StitchColors.StatusOnline          // driving reuses online green
val StateAsleep get() = StitchColors.StatusOffline
val StateOffline get() = StitchColors.StatusOffline
val StateSuspended get() = StitchColors.StatusOffline

// ── Semantic colors ──
val SuccessGreen get() = StitchColors.StatusOnline
val WarningOrange get() = StitchColors.StatusCharging
val ErrorRed get() = StitchColors.StatusError
val InfoBlue get() = StitchColors.Primary

// ── Charging type colors ──
val AcChargeGreen get() = StitchColors.StatusOnline
val DcChargeOrange get() = StitchColors.StatusCharging
