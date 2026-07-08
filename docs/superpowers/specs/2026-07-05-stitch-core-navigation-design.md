---
comet_change: stitch-core-navigation
role: technical-design
canonical_spec: openspec
---

# Stitch Core Navigation — Technical Design

> Date: 2026-07-05
> Change: `stitch-core-navigation` (Split 1/3)

## 1. Architecture Overview

```
shared/design-tokens.json          ← 单一设计 Token 源
       │
       ├── Android: Theme.kt 读取 → StitchCard/StitchStatusChip/StitchDataRow
       │         DashboardScreen / DriveListScreen / ChargeListScreen / MoreScreen
       │         NavGraph (BottomBar)
       │
       └── iOS: AppTheme.swift 读取 → 同组件 SwiftUI 版
                 DashboardView / DriveListView / ChargeListView / MoreView
                 ContentView (TabView)
```

**数据层不修改**：DashboardViewModel、DriveViewModel、ChargeViewModel 保持现有 CarRepository/DelegatingCarRepository 注入。

## 2. Component Design

### 2.1 StitchCard (Surface-based)

```kotlin
@Composable
fun StitchCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.OutlineVariant),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}
```

No Material3 `Card` — pure `Surface` with border. No elevation.

### 2.2 StitchStatusChip

```kotlin
@Composable
fun StitchStatusChip(text: String, status: ChipStatus) {
    Surface(shape = RoundedCornerShape(4.dp), color = status.bg, ...) {
        Text(text, color = status.fg, style = labelCaps)
    }
}
// Online=green(#059669), Offline=gray(#747878), Charging=orange(#D97706), Error=red(#ba1a1a)
```

### 2.3 StitchDataRow

```kotlin
@Composable
fun StitchDataRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = SpaceBetween) {
        Text(label, style = labelCaps)
        Text(value, style = dataMd, fontFamily = JetBrainsMono)
    }
}
```

## 3. Theme Strategy

**Keep MaterialTheme, override colors only:**

```kotlin
private val StitchLightColors = lightColorScheme(
    primary = Stitch.Primary,           // #000000
    onPrimary = Stitch.White,           // #ffffff
    secondary = Stitch.Secondary,       // #895200
    background = Stitch.Background,     // #fdf8f8
    surface = Stitch.Surface,           // #fdf8f8
    onSurface = Stitch.OnSurface,       // #1c1b1b
    outline = Stitch.Outline,           // #747878
    outlineVariant = Stitch.OutlineVariant, // #c4c7c7
    // unused container colors → map to surface
    primaryContainer = Stitch.Surface,
    secondaryContainer = Stitch.Surface,
    // ...
)
```

All `Surface` calls: `shadowElevation = 0.dp`.

## 4. Navigation

**Android**: Keep `NavGraph.kt` with `NavigationBar` — replace visual only:
- `containerColor = StitchColors.Surface`
- Top border: `BorderStroke(1.dp, StitchColors.OutlineVariant)`
- Selected icon: `StitchColors.Accent` (#A16207)
- Unselected icon: `StitchColors.Outline` (#747878)

**iOS**: Keep `TabView` in `ContentView.swift` — replace visual only.

## 5. Design Tokens

Single JSON at `app_glm/shared/design-tokens.json`. Both platforms read from it.
Android parses in `Theme.kt`; iOS parses in `AppTheme.swift`.

## 6. Testing

- `StitchThemeTest.kt`: verify color values match design-tokens.json
- `StitchCardScreenshotTest`: compare rendered card vs Stitch HTML screenshot
- Existing ViewModel tests preserved (no data layer changes)

## 7. File Change List

```
NEW:
  app_glm/shared/design-tokens.json
  app_glm/android/.../ui/components/StitchCard.kt
  app_glm/android/.../ui/components/StitchStatusChip.kt
  app_glm/android/.../ui/components/StitchDataRow.kt
  app_glm/android/.../res/font/jetbrains_mono_regular.ttf
  app_glm/android/.../res/font/jetbrains_mono_medium.ttf

MODIFIED:
  app_glm/android/.../ui/theme/Color.kt
  app_glm/android/.../ui/theme/Theme.kt
  app_glm/android/.../ui/dashboard/DashboardScreen.kt
  app_glm/android/.../ui/drives/DriveListScreen.kt
  app_glm/android/.../ui/charges/ChargeListScreen.kt
  app_glm/android/.../ui/navigation/NavGraph.kt
  app_glm/ios/.../Core/Theme/AppTheme.swift
  app_glm/ios/.../App/ContentView.swift
  app_glm/ios/.../Features/Dashboard/DashboardView.swift
  app_glm/ios/.../Features/Drives/DriveListView.swift
  app_glm/ios/.../Features/Charges/ChargeListView.swift

NEW (iOS):
  app_glm/ios/.../Features/More/MoreView.swift
  app_glm/ios/.../Components/StitchCard.swift
  app_glm/ios/.../Components/StitchStatusChip.swift
  app_glm/ios/.../Components/StitchDataRow.swift
  app_glm/ios/.../Resources/JetBrainsMono-Regular.ttf
  app_glm/ios/.../Resources/JetBrainsMono-Medium.ttf
```
