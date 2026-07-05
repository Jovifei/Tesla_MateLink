# app_mimo UI 规范 1:1 复刻设计

> **日期**: 2026-07-05
> **分支**: `codex/app-mimo-stitch-1to1`
> **目标**: 修复 27 项 UI 规范偏差，达到 Stitch "Precision Minimalist" 1:1 还原
> **基准**: `docs/PRD/MateLink_UI_PRD.md` 主题 B 规范

---

## 设计规范速查

| 要素 | 规范值 |
|------|--------|
| 背景 | 纯白 `#FFFFFF` |
| 主色 | 黑 `#171717` |
| 强调色 | 金 `#A16207` (仅关键 CTA) |
| 状态色 | 在线 `#059669` / 充电 `#F59E0B` / 警告 `#DC2626` |
| 边框 | 1px `#E5E5E5` |
| 圆角 | 卡片 8dp / 按钮 4dp |
| 阴影 | **无** |
| 字体 | Inter (正文) + JetBrains Mono (数字) |
| 数字特性 | `fontFeatureSettings = "tnum"` / `.monospacedDigit()` |
| 间距 | 8dp 基准 |

---

## 修复清单 (27 项，分 4 组)

### 组 A: 主题色修正 (6 项)

**文件**: Android `Color.kt`, iOS `AppTheme.swift`

| # | 平台 | 当前 | 目标 |
|---|------|------|------|
| A1 | Android | `StatusSuccess = #4CAF50` | `#059669` |
| A2 | Android | `StatusWarning = #FF9800` | `#F59E0B` |
| A3 | Android | `StatusError = #F44336` | `#DC2626` |
| A4 | Android | `BoundaryColor = #4CAF50` | `#059669` |
| A5 | Android | `OutlineLight = #D9DDE3` | `#E5E5E5` |
| A6 | iOS | `.green` / `.orange` 系统色 | `#059669` / `#F59E0B` / `#DC2626` |

**范围**: 只改主题定义文件，不改各 Screen 内的硬编码（组 C 处理）。

---

### 组 B: 卡片组件统一 (14 项)

**原则**: 所有卡片统一为 `Card` + `BorderStroke(1dp, #E5E5E5)` + `RoundedCornerShape(8dp)` + 无阴影。

| # | 平台 | 文件 | 修改 |
|---|------|------|------|
| B1 | Android | `EfficiencyScreen.kt` | `ElevatedCard` → `Card` + `BorderStroke(1dp, SwissOutline)` + `RoundedCornerShape(8dp)` |
| B2 | Android | `VampireScreen.kt` | 同上 |
| B3 | Android | `TimelineScreen.kt` | 同上 |
| B4 | Android | `CostScreen.kt` | `Card` 加 `BorderStroke(1dp, SwissOutline)` + `RoundedCornerShape(8dp)`，背景改纯白 |
| B5 | Android | `RangeScreen.kt` | 同上 |
| B6 | Android | `RangeScreen.kt` | TopAppBar `containerColor = Color.White` (当前 primaryContainer) |
| B7 | Android | `TimelineScreen.kt` | TopAppBar `containerColor = Color.White` |
| B8 | Android | `RangeScreen.kt` | 硬编码状态色 `#4CAF50`/`#FFC107`/`#EF5350` → 用主题 `StatusSuccess`/`StatusWarning`/`StatusError` |
| B9 | iOS | `DashboardView.swift` | StatCard 圆角 `cornerRadius(16)` → `cornerRadius(8)` |
| B10 | iOS | `DashboardView.swift` | MiniCard 圆角 `cornerRadius(12)` → `cornerRadius(8)` |
| B11 | iOS | `DashboardView.swift` | 地图卡片圆角 `cornerRadius(16)` → `cornerRadius(8)` |
| B12 | iOS | `DashboardView.swift` | 所有卡片加 `.overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(#E5E5E5), lineWidth: 1))` |
| B13 | iOS | `DashboardView.swift` | BatteryTrendCard 圆角 `cornerRadius(16)` → `cornerRadius(8)` |
| B14 | iOS | `DashboardView.swift` | 加金色强调色 `#A16207` 定义 + 用于关键 CTA |

---

### 组 C: 字体集成 (5 项)

**方案**: 在两端打包 Inter + JetBrains Mono 字体文件，全局应用。

| # | 平台 | 文件 | 修改 |
|---|------|------|------|
| C1 | Android | `assets/fonts/` | 添加 Inter-*.ttf + JetBrainsMono-*.ttf |
| C2 | Android | `Type.kt` | `SwissSans` 改为 `FontFamily(Font(R.font.inter_*))`, `MetricMono` 改为 `FontFamily(Font(R.font.jetbrainsmono_*))` |
| C3 | Android | `Type.kt` | 数字 TextStyle 加 `fontFeatureSettings = "tnum"` |
| C4 | iOS | `MateLink/Resources/Fonts/` | 添加 Inter + JetBrains Mono .ttf |
| C5 | iOS | `AppTheme.swift` + `project.yml` | 定义字体常量 + Info.plist 注册字体 |

**字体来源**: Google Fonts 官方下载 (Inter Variable + JetBrains Mono)。

---

### 组 D: 硬编码色修正 (2 项)

| # | 平台 | 文件 | 修改 |
|---|------|------|------|
| D1 | Android | `DashboardScreen.kt` StateBadge | 硬编码 `#43A047`/`#1E88E5`/`#FB8C00` → 用主题色 |
| D2 | Android | `DashboardScreen.kt` BatteryTrendChart | 硬编码 `#1E88E5` → 用主题 primary |

---

## 不在范围

- 数据层修复 (已完成)
- Medium Bug (RangePage accuracy / Timer capture / ApiClient race)
- 页面功能补全 (已完成)
