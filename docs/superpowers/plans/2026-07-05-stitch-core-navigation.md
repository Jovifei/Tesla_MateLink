# Stitch Core Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Replicate Stitch white-minimal design system 1:1 onto app_glm Android+iOS core navigation (4 tabs + 4 main pages).

**Architecture:** Surface-based StitchCard components, MaterialTheme color override (no shadows), shared design-tokens.json. Data layer (ViewModel/Repository) preserved — UI layer rewrite only.

**Tech Stack:** Android: Kotlin + Jetpack Compose + Material3. iOS: Swift + SwiftUI.

---
change: stitch-core-navigation
design-doc: docs/superpowers/specs/2026-07-05-stitch-core-navigation-design.md
base-ref: 69d2d8c7f718c097e21d55c1f00914a3a97cb354
---

### Task 1: Shared Design Tokens

**Files:**
- Create: `app_glm/shared/design-tokens.json`

- [x] **Step 1: Create design-tokens.json**

```json
{
  "version": "1.0",
  "colors": {
    "background": "#fdf8f8",
    "surface": "#fdf8f8",
    "surfaceDim": "#ddd9d8",
    "surfaceBright": "#fdf8f8",
    "surfaceContainerLowest": "#ffffff",
    "surfaceContainerLow": "#f7f3f2",
    "surfaceContainer": "#f1edec",
    "surfaceContainerHigh": "#ebe7e6",
    "surfaceContainerHighest": "#e5e2e1",
    "onSurface": "#1c1b1b",
    "onSurfaceVariant": "#444748",
    "inverseSurface": "#313030",
    "inverseOnSurface": "#f4f0ef",
    "outline": "#747878",
    "outlineVariant": "#c4c7c7",
    "surfaceTint": "#5f5e5e",
    "primary": "#000000",
    "onPrimary": "#ffffff",
    "primaryContainer": "#1c1b1b",
    "onPrimaryContainer": "#858383",
    "inversePrimary": "#c8c6c5",
    "secondary": "#895200",
    "onSecondary": "#ffffff",
    "secondaryContainer": "#ffb157",
    "onSecondaryContainer": "#734400",
    "tertiary": "#000000",
    "onTertiary": "#ffffff",
    "tertiaryContainer": "#1c1b1a",
    "onTertiaryContainer": "#868382",
    "error": "#ba1a1a",
    "onError": "#ffffff",
    "errorContainer": "#ffdad6",
    "onErrorContainer": "#93000a",
    "accent": "#A16207",
    "statusOnline": "#059669",
    "statusOnlineBg": "#d1fae5",
    "statusOffline": "#747878",
    "statusOfflineBg": "#f3f4f6",
    "statusCharging": "#D97706",
    "statusChargingBg": "#fef3c7",
    "statusError": "#ba1a1a",
    "statusErrorBg": "#ffdad6",
    "border": "#E5E5E5"
  },
  "typography": {
    "displayLg": { "family": "Inter", "size": 32, "weight": 700, "letterSpacing": -0.02, "lineHeight": 1.2 },
    "headlineMd": { "family": "Inter", "size": 24, "weight": 600, "letterSpacing": -0.01, "lineHeight": 1.3 },
    "bodyLg": { "family": "Inter", "size": 16, "weight": 400, "letterSpacing": 0, "lineHeight": 1.6 },
    "bodySm": { "family": "Inter", "size": 14, "weight": 400, "letterSpacing": 0, "lineHeight": 1.5 },
    "dataLg": { "family": "JetBrains Mono", "size": 24, "weight": 500, "lineHeight": 1.0 },
    "dataMd": { "family": "JetBrains Mono", "size": 16, "weight": 500, "lineHeight": 1.0 },
    "labelCaps": { "family": "Inter", "size": 12, "weight": 700, "letterSpacing": 0.05, "lineHeight": 1.0 }
  },
  "spacing": { "xs": 4, "sm": 8, "md": 16, "lg": 24, "xl": 32 },
  "shapes": { "card": 8, "chip": 4, "button": 8 },
  "components": {
    "card": { "bg": "surface", "border": "border", "radius": 8, "padding": 24 },
    "tabBar": { "bg": "surface", "borderTop": "border", "activeColor": "accent", "inactiveColor": "outline", "iconSize": 24, "iconWeight": 1.5 },
    "statusChip": { "radius": 4 }
  }
}
```

- [x] **Step 2: Verify JSON is valid**

```bash
python -c "import json; json.load(open('app_glm/shared/design-tokens.json')); print('VALID')"
```

- [x] **Step 3: Commit**

```bash
git add app_glm/shared/design-tokens.json
git commit -m "feat(shared): add Stitch design tokens JSON"
```

### Task 2: Android — Stitch Theme Rewrite

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/theme/Color.kt`
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/theme/Theme.kt`

- [x] **Step 1: Rewrite Color.kt with Stitch palette**

Replace all existing color definitions with:

```kotlin
package com.teslamatelink.ui.theme

import androidx.compose.ui.graphics.Color

// Stitch White-Minimal Palette
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
```

- [x] **Step 2: Rewrite Theme.kt — MaterialTheme with Stitch colors, zero shadow**

```kotlin
package com.teslamatelink.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.R

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium)
)

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

val StitchTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.02).sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.01).sp, lineHeight = 31.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp, lineHeight = 12.sp)
)

@Composable
fun MateLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StitchLightColors,
        typography = StitchTypography,
        content = content
    )
}
```

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/theme/Color.kt app_glm/android/app/src/main/java/com/teslamatelink/ui/theme/Theme.kt
git commit -m "feat(android): rewrite theme with Stitch white-minimal palette"
```

### Task 3: Android — JetBrains Mono Font

**Files:**
- Create: `app_glm/android/app/src/main/res/font/jetbrains_mono_regular.ttf`
- Create: `app_glm/android/app/src/main/res/font/jetbrains_mono_medium.ttf`

- [x] **Step 1: Download JetBrains Mono fonts**

Download from Google Fonts or use existing system fonts. If unavailable, note as TODO and fall back to monospace.

```bash
# If fonts available locally:
cp <source>/JetBrainsMono-Regular.ttf app_glm/android/app/src/main/res/font/jetbrains_mono_regular.ttf
cp <source>/JetBrainsMono-Medium.ttf app_glm/android/app/src/main/res/font/jetbrains_mono_medium.ttf
```

- [x] **Step 2: Verify Theme.kt references fonts correctly**

The `JetBrainsMonoFamily` in Theme.kt references `R.font.jetbrains_mono_regular` and `R.font.jetbrains_mono_medium`. Verify these match the filenames.

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/res/font/
git commit -m "feat(android): add JetBrains Mono font assets"
```

### Task 4: Android — StitchCard, StitchStatusChip, StitchDataRow Components

**Files:**
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/ui/components/StitchCard.kt`
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/ui/components/StitchStatusChip.kt`
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/ui/components/StitchDataRow.kt`

- [x] **Step 1: Create StitchCard.kt**

```kotlin
package com.teslamatelink.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.theme.StitchColors

@Composable
fun StitchCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}
```

- [x] **Step 2: Create StitchStatusChip.kt**

```kotlin
package com.teslamatelink.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.StitchColors

enum class ChipStatus(val bg: Color, val fg: Color) {
    ONLINE(StitchColors.StatusOnlineBg, StitchColors.StatusOnline),
    OFFLINE(StitchColors.StatusOfflineBg, StitchColors.StatusOffline),
    CHARGING(StitchColors.StatusChargingBg, StitchColors.StatusCharging),
    ERROR(StitchColors.StatusErrorBg, StitchColors.StatusError)
}

@Composable
fun StitchStatusChip(text: String, status: ChipStatus) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = status.bg,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            color = status.fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
```

- [x] **Step 3: Create StitchDataRow.kt**

```kotlin
package com.teslamatelink.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.StitchColors
import com.teslamatelink.ui.theme.JetBrainsMonoFamily

@Composable
fun StitchDataRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            color = StitchColors.OnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily
        )
    }
}
```

- [x] **Step 4: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/components/
git commit -m "feat(android): add StitchCard, StitchStatusChip, StitchDataRow components"
```

### Task 5: Android — Bottom Navigation Rewrite

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/navigation/NavGraph.kt`

- [x] **Step 1: Rewrite bottom bar with Stitch styling**

Replace the existing `NavigationBar` with:

```kotlin
NavigationBar(
    containerColor = StitchColors.Surface,
    tonalElevation = 0.dp,
    border = BorderStroke(1.dp, StitchColors.Border),
    modifier = Modifier.fillMaxWidth()
) {
    // Dashboard tab
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Dashboard,
                contentDescription = "仪表盘",
                modifier = Modifier.size(24.dp),
                tint = if (currentRoute == "dashboard") StitchColors.Accent else StitchColors.Outline
            )
        },
        label = {
            Text(
                "仪表盘",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentRoute == "dashboard") StitchColors.Accent else StitchColors.Outline
            )
        },
        selected = currentRoute == "dashboard",
        onClick = { navController.navigate("dashboard") { launchSingleTop = true } },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )
    // Trips tab
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Route,
                contentDescription = "行程",
                modifier = Modifier.size(24.dp),
                tint = if (currentRoute == "trips") StitchColors.Accent else StitchColors.Outline
            )
        },
        label = {
            Text(
                "行程",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentRoute == "trips") StitchColors.Accent else StitchColors.Outline
            )
        },
        selected = currentRoute == "trips",
        onClick = { navController.navigate("trips") { launchSingleTop = true } },
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
    )
    // Charging tab
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Bolt,
                contentDescription = "充电",
                modifier = Modifier.size(24.dp),
                tint = if (currentRoute == "charging") StitchColors.Accent else StitchColors.Outline
            )
        },
        label = {
            Text(
                "充电",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentRoute == "charging") StitchColors.Accent else StitchColors.Outline
            )
        },
        selected = currentRoute == "charging",
        onClick = { navController.navigate("charging") { launchSingleTop = true } },
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
    )
    // More tab
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "更多",
                modifier = Modifier.size(24.dp),
                tint = if (currentRoute == "more") StitchColors.Accent else StitchColors.Outline
            )
        },
        label = {
            Text(
                "更多",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentRoute == "more") StitchColors.Accent else StitchColors.Outline
            )
        },
        selected = currentRoute == "more",
        onClick = { navController.navigate("more") { launchSingleTop = true } },
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
    )
}
```

- [x] **Step 2: Verify imports include BorderStroke, StitchColors, etc.**

```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.NavigationBarItemDefaults
import com.teslamatelink.ui.theme.StitchColors
```

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/navigation/NavGraph.kt
git commit -m "feat(android): rewrite bottom nav with Stitch styling"
```

### Task 6: Android — DashboardScreen Rewrite

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/dashboard/DashboardScreen.kt`
- Read: `app_glm/android/app/src/main/java/com/teslamatelink/ui/dashboard/DashboardViewModel.kt` (existing, do not modify)

- [x] **Step 1: Rewrite DashboardScreen with Stitch components**

Key structure:
```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val carStatus = uiState.carStatus

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            // Status header: car name + Online/Offline chip
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = SpaceBetween) {
                Column {
                    Text(carStatus?.displayName ?: "My Tesla", style = headlineMd)
                    Text("Last seen: 2 min ago", style = bodySm, color = OnSurfaceVariant)
                }
                StitchStatusChip(
                    text = if (carStatus?.state == "online") "Online" else "Offline",
                    status = if (carStatus?.state == "online") ChipStatus.ONLINE else ChipStatus.OFFLINE
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), verticalArrangement = spacedBy(24.dp)) {
            // Battery Card
            StitchCard {
                Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Battery", style = labelCaps, color = OnSurfaceVariant)
                        Text("${carStatus?.batteryLevel ?: 0}%", style = dataLg, fontFamily = JetBrainsMono, color = OnSurface)
                        Text("${carStatus?.estBatteryRangeKm ?: 0} km", style = bodySm, color = OnSurfaceVariant)
                    }
                    // Circular battery indicator
                    Box(contentAlignment = Center) {
                        CircularProgressIndicator(progress = { (carStatus?.batteryLevel ?: 0) / 100f }, color = Accent, strokeWidth = 3.dp, modifier = Modifier.size(56.dp))
                        Text("${carStatus?.chargeLimitSoc ?: 80}%", style = labelCaps)
                    }
                }
            }

            // Charging Card (only when plugged in)
            if (carStatus?.pluggedIn == true) {
                StitchCard {
                    Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (carStatus.isDcCharging) "DC Fast Charging" else "Charging", style = labelCaps, color = StatusCharging)
                        StitchStatusChip("Charging", ChipStatus.CHARGING)
                    }
                    Spacer(Modifier.height(16.dp))
                    StitchDataRow("Power", "${carStatus.chargerPower ?: 0} kW")
                    StitchDataRow("Energy Added", "${carStatus.chargeEnergyAdded ?: 0} kWh")
                    StitchDataRow("Time to Full", "${carStatus.timeToFullChargeHours ?: "--"} h")
                }
            }

            // Info Grid (2 columns)
            StitchCard {
                Column(verticalArrangement = spacedBy(16.dp)) {
                    Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        StitchDataRow("Odometer", "${carStatus?.odometer?.toLong() ?: 0} km", Modifier.weight(1f))
                        Spacer(Modifier.width(16.dp))
                        StitchDataRow("Version", "2026.20.5", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        StitchDataRow("FL Tire", "${carStatus?.frontLeftPsi ?: "--"} psi", Modifier.weight(1f))
                        StitchDataRow("FR Tire", "${carStatus?.frontRightPsi ?: "--"} psi", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        StitchDataRow("RL Tire", "${carStatus?.rearLeftPsi ?: "--"} psi", Modifier.weight(1f))
                        StitchDataRow("RR Tire", "${carStatus?.rearRightPsi ?: "--"} psi", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        StitchDataRow("Inside", "${carStatus?.insideTemp ?: "--"}°C", Modifier.weight(1f))
                        StitchDataRow("Outside", "${carStatus?.outsideTemp ?: "--"}°C", Modifier.weight(1f))
                    }
                }
            }

            // 7-Day Trend (placeholder chart)
            StitchCard {
                Text("7-Day Trend", style = labelCaps, color = OnSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                // Simple bar chart using Canvas or placeholder
                Text("Chart placeholder — to be enhanced", style = bodySm, color = OnSurfaceVariant)
            }
        }
    }
}
```

- [x] **Step 2: Verify ViewModel injection unchanged**

The `hiltViewModel()` call and `uiState` collection remain identical to existing code. Only the UI layout changes.

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/dashboard/DashboardScreen.kt
git commit -m "feat(android): rewrite DashboardScreen with Stitch design"
```

### Task 7: Android — DriveListScreen Rewrite

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/drives/DriveListScreen.kt`

- [x] **Step 1: Rewrite with month-grouped StitchCard rows**

Replace the existing list with month-grouped LazyColumn, each trip as StitchCard:

```kotlin
@Composable
fun DriveListScreen(viewModel: DriveViewModel = hiltViewModel(), onDriveClick: (Long) -> Unit) {
    val drives by viewModel.drives.collectAsState()

    Scaffold(containerColor = StitchColors.Background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group by month
            val grouped = drives.groupBy { /* extract YYYY-MM */ }
            grouped.forEach { (month, monthDrives) ->
                item {
                    Text(month, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                }
                items(monthDrives) { drive ->
                    StitchCard(modifier = Modifier.clickable { onDriveClick(drive.id) }) {
                        Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${drive.startAddress} → ${drive.endAddress}", style = bodyLg, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = spacedBy(12.dp)) {
                                    Text(drive.date, style = bodySm, color = OnSurfaceVariant)
                                    Text("${drive.distance} km", style = bodySm, color = OnSurfaceVariant)
                                    Text("${drive.duration}", style = bodySm, color = OnSurfaceVariant)
                                }
                            }
                            if (drive.efficiency > 90) {
                                StitchStatusChip("Efficient", ChipStatus.ONLINE)
                            }
                        }
                    }
                }
            }
            if (drives.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Center) {
                        Text("No trips recorded", style = bodyLg, color = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}
```

- [x] **Step 2: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/drives/DriveListScreen.kt
git commit -m "feat(android): rewrite DriveListScreen with Stitch month-grouped cards"
```

### Task 8: Android — ChargeListScreen Rewrite

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/charges/ChargeListScreen.kt`

- [x] **Step 1: Rewrite with active charging card + StitchCard rows**

```kotlin
@Composable
fun ChargeListScreen(viewModel: ChargeViewModel = hiltViewModel(), onChargeClick: (Long) -> Unit) {
    val charges by viewModel.charges.collectAsState()
    val activeCharge by viewModel.activeCharge.collectAsState()

    Scaffold(containerColor = StitchColors.Background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active charging card
            if (activeCharge != null) {
                item {
                    StitchCard {
                        Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Charging Now", style = labelCaps, color = StatusCharging)
                            StitchStatusChip("Charging", ChipStatus.CHARGING)
                        }
                        Spacer(Modifier.height(16.dp))
                        StitchDataRow("Current", "${activeCharge!!.batteryLevel}%")
                        StitchDataRow("Power", "${activeCharge!!.chargerPower} kW")
                        StitchDataRow("Energy Added", "${activeCharge!!.chargeEnergyAdded} kWh")
                        StitchDataRow("Est. Complete", activeCharge!!.timeToFull ?: "--")
                    }
                }
            }
            // Summary header
            item {
                val totalKwh = charges.sumOf { it.chargeEnergyAdded ?: 0.0 }
                val totalCost = charges.sumOf { it.cost ?: 0.0 }
                Text("Total: ${String.format("%.1f", totalKwh)} kWh · ¥${String.format("%.2f", totalCost)}", style = bodySm, color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            }
            // Charge history
            val grouped = charges.groupBy { /* YYYY-MM */ }
            grouped.forEach { (month, monthCharges) ->
                item { Text(month, style = headlineMd, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                items(monthCharges) { charge ->
                    StitchCard(modifier = Modifier.clickable { onChargeClick(charge.id) }) {
                        Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(charge.location ?: "Unknown", style = bodyLg)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = spacedBy(12.dp)) {
                                    Text(charge.date, style = bodySm, color = OnSurfaceVariant)
                                    Text("${charge.duration}", style = bodySm, color = OnSurfaceVariant)
                                    Text("${charge.chargeEnergyAdded} kWh", style = bodySm, color = OnSurfaceVariant)
                                    Text("¥${charge.cost}", style = bodySm, color = OnSurfaceVariant)
                                }
                            }
                            StitchStatusChip(if (charge.isDc) "DC" else "AC", if (charge.isDc) ChipStatus.CHARGING else ChipStatus.OFFLINE)
                        }
                    }
                }
            }
        }
    }
}
```

- [x] **Step 2: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/charges/ChargeListScreen.kt
git commit -m "feat(android): rewrite ChargeListScreen with Stitch design"
```

### Task 9: Android — MoreScreen Creation

**Files:**
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/ui/more/MoreScreen.kt`

- [x] **Step 1: Create MoreScreen with analysis entries**

```kotlin
package com.teslamatelink.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.theme.StitchColors

data class MoreEntry(val title: String, val description: String, val route: String)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    val analysisEntries = listOf(
        MoreEntry("Statistics", "Monthly & yearly summaries", "statistics"),
        MoreEntry("Heatmap", "Driving frequency & patterns", "heatmap"),
        MoreEntry("Efficiency", "Energy efficiency analysis", "efficiency"),
        MoreEntry("Range", "Estimated vs actual range", "range"),
        MoreEntry("Energy", "Energy consumption breakdown", "energy"),
        MoreEntry("Battery Health", "Capacity & degradation", "battery"),
        MoreEntry("Timeline", "Activity timeline view", "timeline")
    )
    val reportEntries = listOf(
        MoreEntry("Annual Report PDF", "Generate yearly report", "report_pdf"),
        MoreEntry("CSV/JSON Export", "Export raw data", "export"),
        MoreEntry("Firmware Versions", "Update history", "updates")
    )

    Scaffold(containerColor = StitchColors.Background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { Text("Analysis", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)) }
            items(analysisEntries) { entry ->
                Surface(color = StitchColors.Surface, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth().clickable { onNavigate(entry.route) }) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.bodyLarge)
                            Text(entry.description, style = MaterialTheme.typography.bodySmall, color = StitchColors.OnSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = StitchColors.Outline)
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = StitchColors.Border)
            }
            item { Text("Reports & Data", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)) }
            items(reportEntries) { entry ->
                Surface(color = StitchColors.Surface, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth().clickable { onNavigate(entry.route) }) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.bodyLarge)
                            Text(entry.description, style = MaterialTheme.typography.bodySmall, color = StitchColors.OnSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = StitchColors.Outline)
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = StitchColors.Border)
            }
        }
    }
}
```

- [x] **Step 2: Register MoreScreen route in NavGraph.kt**

Add to NavGraph:
```kotlin
composable("more") { MoreScreen(onNavigate = { route -> navController.navigate(route) }) }
```

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/more/MoreScreen.kt app_glm/android/app/src/main/java/com/teslamatelink/ui/navigation/NavGraph.kt
git commit -m "feat(android): add MoreScreen with analysis entries"
```

### Task 10: iOS — Theme & Font Setup

**Files:**
- Modify: `app_glm/ios/MateLink/Core/Theme/AppTheme.swift`
- Create: `app_glm/ios/MateLink/Resources/JetBrainsMono-Regular.ttf` (if available)
- Create: `app_glm/ios/MateLink/Resources/JetBrainsMono-Medium.ttf` (if available)

- [x] **Step 1: Rewrite AppTheme.swift with Stitch colors**

```swift
import SwiftUI

struct StitchColors {
    static let background = Color(hex: "fdf8f8")
    static let surface = Color(hex: "fdf8f8")
    static let onSurface = Color(hex: "1c1b1b")
    static let onSurfaceVariant = Color(hex: "444748")
    static let outline = Color(hex: "747878")
    static let outlineVariant = Color(hex: "c4c7c7")
    static let primary = Color(hex: "000000")
    static let accent = Color(hex: "A16207")
    static let secondary = Color(hex: "895200")
    static let error = Color(hex: "ba1a1a")
    static let border = Color(hex: "E5E5E5")
    static let white = Color(hex: "ffffff")
    static let statusOnline = Color(hex: "059669")
    static let statusOnlineBg = Color(hex: "d1fae5")
    static let statusOffline = Color(hex: "747878")
    static let statusOfflineBg = Color(hex: "f3f4f6")
    static let statusCharging = Color(hex: "D97706")
    static let statusChargingBg = Color(hex: "fef3c7")
    static let statusError = Color(hex: "ba1a1a")
    static let statusErrorBg = Color(hex: "ffdad6")
}

extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        var rgb: UInt64 = 0
        scanner.scanHexInt64(&rgb)
        self.init(
            red: Double((rgb >> 16) & 0xFF) / 255.0,
            green: Double((rgb >> 8) & 0xFF) / 255.0,
            blue: Double(rgb & 0xFF) / 255.0
        )
    }
}

struct StitchFont {
    static func dataLg() -> Font { .custom("JetBrainsMono-Medium", size: 24) }
    static func dataMd() -> Font { .custom("JetBrainsMono-Medium", size: 16) }
}
```

- [x] **Step 2: Add fonts to Info.plist if needed**

Add "Fonts provided by application" entries for the .ttf files.

- [x] **Step 3: Commit**

```bash
git add app_glm/ios/
git commit -m "feat(ios): add Stitch theme colors and fonts"
```

### Task 11: iOS — TabView + Dashboard + Lists

**Files:**
- Modify: `app_glm/ios/MateLink/App/ContentView.swift`
- Modify: `app_glm/ios/MateLink/Features/Dashboard/DashboardView.swift`
- Modify: `app_glm/ios/MateLink/Features/Drives/DriveListView.swift`
- Modify: `app_glm/ios/MateLink/Features/Charges/ChargeListView.swift`
- Create: `app_glm/ios/MateLink/Features/More/MoreView.swift`

- [x] **Step 1: Rewrite ContentView.swift TabView**

```swift
TabView {
    DashboardView().tabItem {
        Image(systemName: "gauge.with.dots.needle.33percent")
        Text("仪表盘")
    }
    DriveListView().tabItem {
        Image(systemName: "point.topleft.down.to.point.bottomright.curvepath")
        Text("行程")
    }
    ChargeListView().tabItem {
        Image(systemName: "bolt.fill")
        Text("充电")
    }
    MoreView().tabItem {
        Image(systemName: "ellipsis")
        Text("更多")
    }
}
.tint(Color(hex: "A16207"))
```

- [x] **Step 2: Rewrite DashboardView.swift with Stitch cards**

Match Android layout: status header + battery card + charging card (conditional) + info grid + trend placeholder. Use `StitchColors` and `StitchFont`.

- [x] **Step 3: Rewrite DriveListView.swift with month-grouped cards**

Match Android: month headers + StitchCard rows with address/date/distance/duration/efficiency badge.

- [x] **Step 4: Rewrite ChargeListView.swift with active card + history**

Match Android: active charging card (conditional) + summary header + month-grouped charge cards with AC/DC labels.

- [x] **Step 5: Create MoreView.swift with analysis entries**

Match Android: grouped list with icons, titles, descriptions, chevrons. Section headers for "Analysis" and "Reports & Data".

- [x] **Step 6: Commit**

```bash
git add app_glm/ios/
git commit -m "feat(ios): rewrite TabView, Dashboard, DriveList, ChargeList, More with Stitch design"
```

### Task 12: Verification

- [x] **Step 1: Android build check**

```bash
cd app_glm/android && ./gradlew assembleDebug
```

- [x] **Step 2: Verify no shadows remain**

```bash
grep -r "elevation" app_glm/android/app/src/main/java/com/teslamatelink/ui/ | grep -v "shadowElevation = 0.dp" | grep -v "tonalElevation = 0.dp"
```
Expected: no output (all elevations are zero).

- [x] **Step 3: Verify StitchColors used consistently**

```bash
grep -r "MaterialTheme.colorScheme" app_glm/android/app/src/main/java/com/teslamatelink/ui/dashboard/ app_glm/android/app/src/main/java/com/teslamatelink/ui/drives/ app_glm/android/app/src/main/java/com/teslamatelink/ui/charges/ app_glm/android/app/src/main/java/com/teslamatelink/ui/more/
```
Expected: minimal or no matches (StitchColors should be used instead).

- [x] **Step 4: Commit final verification**

```bash
git commit -m "chore: verification — no shadows, Stitch colors consistent" --allow-empty
```
