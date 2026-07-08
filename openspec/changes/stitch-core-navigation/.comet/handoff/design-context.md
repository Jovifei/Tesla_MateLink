# Comet Design Handoff

- Change: stitch-core-navigation
- Phase: design
- Mode: compact
- Context hash: f99a209abd0f303e93f1a958a80ef28c55d269f84379478733e1bccd01f09d84

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/stitch-core-navigation/proposal.md

- Source: openspec/changes/stitch-core-navigation/proposal.md
- Lines: 1-34
- SHA256: 3cff6882d7be252291552ace62548f5173c254ec9e52e444f5b9e18c66b0c024

```md
## Why

当前 app_glm Android 端虽有 20 个 Screen 已实现，但 UI 风格不统一（各页面视觉语言混杂），与 Stitch 项目中已验证的「MateLink 白色简约瑞士风」设计稿存在较大偏差。iOS 端同样需要对齐该设计系统。本 change 作为三阶段拆分的第一步，聚焦核心导航框架和 4 个主 Tab 页面，建立统一的设计基准，后续 change 在此基础上扩展详情页和分析页。

## What Changes

- **Android**: 重构底部 4 Tab 导航栏（仪表盘/行程/充电/更多），统一为 Stitch 白色简约风：`#FFFFFF` 背景、`#171717` 文字、`#A16207` 金色强调、Inter 字体、JetBrains Mono 数据字体、1px `#E5E5E5` 边框卡片、8px 圆角、无阴影
- **Android**: 重写 DashboardScreen UI 层，保留现有 DashboardViewModel 数据流，仅改视觉呈现
- **Android**: 重写 DriveListScreen / ChargeListScreen / MoreScreen UI 层
- **iOS**: 创建/重写底部 4 Tab 导航（TabView），统一为相同设计系统
- **iOS**: 重写 DashboardView / DriveListView / ChargeListView / MoreView UI 层
- **Shared**: 提取设计 Token 常量（颜色、字体、间距）到 shared 目录供双端引用

## Capabilities

### New Capabilities

- `stitch-design-system`: Stitch 白色简约风设计系统 — 颜色 palette、字体 scale、间距 grid、卡片/按钮/状态指示器组件规范
- `core-navigation`: 底部 4 Tab 主导航框架（仪表盘/行程/充电/更多），含 Tab 切换、图标、活跃态金色指示
- `dashboard-stitch`: 仪表盘页面 — 车辆状态总览、电池卡片（百分比+续航）、充电卡片、信息网格（胎压/温度/里程）、7 天趋势图
- `trip-list-stitch`: 行程历史页面 — 按月分组列表、起止地点、距离、时长、日期、效率标记
- `charge-list-stitch`: 充电历史页面 — AC/DC 分类、当前充电实时卡片、总电量/费用统计
- `more-menu-stitch`: 更多菜单页面 — 分析入口列表（统计/热力图/效率/续航/能耗/电池健康/时间线）、报告导出入口

### Modified Capabilities

<!-- 本次不修改已有 capability，仅新增 UI 层 -->

## Impact

- Affected code: `app_glm/android/.../ui/dashboard/`, `ui/drives/`, `ui/charges/`, `ui/navigation/`, `ui/theme/`, `app_glm/ios/.../Features/Dashboard/`, `Features/Drives/`, `Features/Charges/`, `app_glm/shared/`
- Existing DashboardViewModel / DriveViewModel / ChargeViewModel data layer preserved
- 8 个硬编码分析页（BatteryHealth/Heatmap/Efficiency/Vampire/Range/Destinations/Cost/Updates）不在本次范围，由 Change #3 处理
- Dependencies: 依赖现有 CarRepository / TeslamateRepository 数据层
```

## openspec/changes/stitch-core-navigation/design.md

- Source: openspec/changes/stitch-core-navigation/design.md
- Lines: 1-60
- SHA256: 47c6c93741b6801c302bccb5ffe5c1c782c74461e85e33cb0df9a80dac74710a

```md
## Context

当前 app_glm Android 端有 20 个 Screen 全实现，iOS 端有 17 个 Feature View。但 UI 风格未统一，各页面使用不同视觉语言（Material3 默认主题）。本 change 的目标是将 Stitch 项目「MateLink 白色简约瑞士风」的设计系统 1:1 落地到 app_glm 双端。

**Stitch 设计系统已从 MCP 完整提取**，包含：
- 完整颜色 palette（namedColors 28 色）
- 完整 typography scale（display-lg/headline-md/body-lg/body-sm/data-lg/data-md/label-caps）
- 完整 spacing scale（xs=4/sm=8/md=16/lg=24/xl=32）
- 完整组件规范（Buttons/Cards/Inputs/Status Chips/Bottom Tab Bar/Data Rows）

**现有数据层保留**：DashboardViewModel、DriveViewModel、ChargeViewModel 已接真实数据（通过 DelegatingCarRepository），本 change 仅重写 UI 层。

## Goals / Non-Goals

**Goals:**
- 双端统一为 Stitch 白色简约风设计系统
- 底部 4 Tab 导航（仪表盘/行程/充电/更多）
- 4 个主 Tab 页面的 UI 层重写，与 Stitch HTML 视觉 1:1 一致
- 提取共享设计 Token 到 `app_glm/shared/`

**Non-Goals:**
- 不修改数据层（Repository/ViewModel 保留）
- 不处理详情页（Change #2）
- 不处理分析页（Change #3）
- 不新增后端 API 端点

## Decisions

### D1: 双端设计 Token 策略

**决定**: Android 用 Compose `MaterialTheme` 扩展自定义 Color/Typography/Shape；iOS 用 SwiftUI `Color`/`Font` 扩展。共享颜色值写入 `shared/design-tokens.json` 供双端引用。

**备选**: 双端各自硬编码颜色值
**选择理由**: 单一 Token 源保证一致性，JSON 格式双端均可解析

### D2: UI 层重写 vs 渐进修改

**决定**: 4 个主 Tab 页面完全重写 Composable/View，保留 ViewModel 注入不变

**备选**: 渐进修改现有 Composable
**选择理由**: 现有 UI 与 Stitch 差异大（如卡片阴影 → 1px 边框、Material3 颜色 → Stitch palette），渐进修改不如重写干净

### D3: 导航架构

**决定**: Android 保留 Jetpack Navigation + BottomNavBar；iOS 保留 TabView。不改导航库，仅替换 UI 呈现

### D4: 数据字体

**决定**: Android 数值显示使用 `JetBrains Mono`（需打包字体文件到 assets）；iOS 使用 `JetBrains Mono`（需添加 .ttf 到 bundle）

### D5: 卡片组件

**决定**: 创建通用 `StitchCard` 组件（双端各自实现），封装 1px `#E5E5E5` 边框 + 8px 圆角 + 24px 内边距 + 白色背景 + 无阴影

## Risks / Trade-offs

- [JetBrains Mono 字体体积] → 仅打包 Regular/Medium 两个 weight（约 200KB），不打包完整字体家族
- [现有 ViewModel 数据字段可能不匹配 Stitch 设计] → 在 UI 层做字段映射/缺省值处理，不修改 ViewModel
- [iOS 端没有 Compose 预览，开发效率低于 Android] → Android 先完成作为参考，iOS 对照实现
- [Stitch HTML 是静态 mock，字段名可能与实际 API 不同] → UI 层使用 ViewModel 暴露的实际字段名，数值示例参考 Stitch
```

## openspec/changes/stitch-core-navigation/tasks.md

- Source: openspec/changes/stitch-core-navigation/tasks.md
- Lines: 1-92
- SHA256: 2a5280e39bb0be04b614ea5f282a98b09e5701bdc5b0735e9b55752c94f578a8

[TRUNCATED]

```md
## 1. Shared Design Tokens

- [ ] 1.1 Create `app_glm/shared/design-tokens.json` — color palette, typography scale, spacing grid, card spec, tab bar spec
- [ ] 1.2 Verify JSON schema: all 28 named colors, 7 typography levels, 5 spacing steps, component specs

## 2. Android — Theme & Components

- [ ] 2.1 Rewrite `ui/theme/Color.kt` — replace Material3 colors with Stitch palette (28 colors)
- [ ] 2.2 Rewrite `ui/theme/Theme.kt` — Inter + JetBrains Mono typography, 8px grid, no shadows
- [ ] 2.3 Create `ui/components/StitchCard.kt` — 1px #E5E5E5 border, 8px radius, 24px padding, white bg, no elevation
- [ ] 2.4 Create `ui/components/StitchStatusChip.kt` — 4px radius, tinted bg, dark text (Online green / Offline gray / Charging orange)
- [ ] 2.5 Create `ui/components/StitchDataRow.kt` — label-caps label + data-md (JetBrains Mono) value, right-aligned
- [ ] 2.6 Bundle JetBrains Mono Regular + Medium (.ttf) into `res/font/`, declare in Typeface

## 3. Android — Bottom Navigation

- [ ] 3.1 Rewrite `ui/navigation/NavGraph.kt` bottom bar: white bg, 1px #E5E5E5 top border, 4 tabs (Dashboard/Trips/Charging/More)
- [ ] 3.2 Tab icons: 24dp stroke-based (1.5px weight), active=#A16207, inactive=#747878
- [ ] 3.3 Tab labels: label-caps style (Inter 12px 700, 0.05em letter-spacing)

## 4. Android — Dashboard Screen

- [ ] 4.1 Rewrite `DashboardScreen.kt` Composable — white bg, status header, battery card, charging card, info grid, 7-day chart
- [ ] 4.2 Vehicle status header: car name (headline-md), Online/Offline chip, last-seen timestamp
- [ ] 4.3 Battery card (StitchCard): battery% (data-lg JetBrains Mono), range km, charge limit indicator
- [ ] 4.4 Charging card (StitchCard): only visible when pluggedIn=true; shows kW, kWh added, time-to-full
- [ ] 4.5 Info grid (2-col): odometer, version, FL/FR/RL/RR tire psi, inside/outside temp — using StitchDataRow
- [ ] 4.6 7-day trend chart: simple line/bar chart, gold #A16207 line, from battery history data
- [ ] 4.7 Wire up existing DashboardViewModel (CarRepository injection preserved, no data layer changes)

## 5. Android — Trip List Screen

- [ ] 5.1 Rewrite `DriveListScreen.kt` — month-grouped list, Stitch card rows
- [ ] 5.2 Month header: "2026年6月" in headline-md
- [ ] 5.3 Trip row: start→end address (body-lg), date/distance/duration (body-sm right-aligned), efficiency badge
- [ ] 5.4 Wire up existing DriveViewModel (data layer preserved)
- [ ] 5.5 Tap row navigates to DriveDetailScreen (existing, restyle in Change #2)

## 6. Android — Charge List Screen

- [ ] 6.1 Rewrite `ChargeListScreen.kt` — active charging card + month-grouped history
- [ ] 6.2 Active charging card: only when isCharging=true; SoC%, kW, kWh, time-to-full
- [ ] 6.3 Charge row: location (body-lg), date/duration/energy/cost (body-sm), AC/DC label
- [ ] 6.4 Header summary: "Total: X kWh · ¥Y" for current month
- [ ] 6.5 Wire up existing ChargeViewModel (data layer preserved)
- [ ] 6.6 Tap row navigates to ChargeDetailScreen (existing, restyle in Change #2)

## 7. Android — More Menu Screen

- [ ] 7.1 Create `MoreScreen.kt` — scrollable list of analysis entries + report/export section
- [ ] 7.2 Analysis entries: 7 items (Statistics/Heatmap/Efficiency/Range/Energy/Battery/Timeline), icon + title + description + chevron
- [ ] 7.3 Report section: 年度报告 PDF, CSV/JSON 导出, 固件版本
- [ ] 7.4 Tap entries navigate to existing screens or stub placeholders (for pages not yet restyled)

## 8. iOS — Theme & Components

- [ ] 8.1 Rewrite `Core/Theme/AppTheme.swift` — Stitch colors, Inter+JetBrains Mono fonts, no shadows
- [ ] 8.2 Add JetBrains Mono .ttf to iOS bundle, declare in Info.plist
- [ ] 8.3 Create reusable StitchCard, StitchStatusChip, StitchDataRow SwiftUI components

## 9. iOS — Bottom Navigation

- [ ] 9.1 Rewrite `App/ContentView.swift` TabView — white bg, 1px top border, 4 tabs, gold active / gray inactive
- [ ] 9.2 Tab icons: SF Symbols stroke-based equivalents, 24pt, active=#A16207, inactive=#747878

## 10. iOS — Dashboard View

- [ ] 10.1 Rewrite `Features/Dashboard/DashboardView.swift` — white bg, status header, battery card, charging card, info grid, trend
- [ ] 10.2 Same card layout as Android (4.2–4.6), adapted for SwiftUI

## 11. iOS — Trip List View

- [ ] 11.1 Rewrite `Features/Drives/DriveListView.swift` — month-grouped, Stitch card rows
- [ ] 11.2 Match Android trip row spec (5.2–5.5)

## 12. iOS — Charge List View

- [ ] 12.1 Rewrite `Features/Charges/ChargeListView.swift` — active charging card + history
- [ ] 12.2 Match Android charge row spec (6.2–6.6)

```

Full source: openspec/changes/stitch-core-navigation/tasks.md

## openspec/changes/stitch-core-navigation/specs/charge-list-stitch/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/charge-list-stitch/spec.md
- Lines: 1-40
- SHA256: 0fcd9b9c57ed0293f5ca23fc668944fdf3154a5ccaa9bc1613deee23323fc110

```md
## ADDED Requirements

### Requirement: Active charging card
The system SHALL display a prominent card at the top of the charge list when the vehicle is currently charging. Card shows: "Charging" status, current SoC%, charger power (kW), energy added (kWh), estimated completion time.

#### Scenario: Vehicle is charging
- **WHEN** vehicle is actively charging
- **THEN** active charging card displays at top with live data

#### Scenario: Vehicle not charging
- **WHEN** vehicle is not plugged in
- **THEN** active charging card is hidden

### Requirement: Charge history list
The system SHALL display past charging sessions grouped by month. Each row shows: location/address, date, duration, energy added (kWh), cost (¥). AC and DC sessions SHALL be visually distinguished.

#### Scenario: Charge list with mixed types
- **WHEN** charge history contains both AC and DC sessions
- **THEN** DC sessions show "DC" label, AC sessions show "AC" label

### Requirement: Charge summary stats
The system SHALL display total energy charged (kWh) and total cost (¥) for the visible period at the top of the list.

#### Scenario: Summary display
- **WHEN** charge list loads
- **THEN** header shows "Total: X kWh · ¥Y" for current month

### Requirement: Charge row style
Each charge row SHALL use Stitch card style: white background, 1px border, 8px radius. Location in body-lg, date/duration/energy/cost in body-sm.

#### Scenario: Charge row rendering
- **WHEN** charge list displays
- **THEN** each row matches Stitch card component spec

### Requirement: Tap to charge detail
The system SHALL allow tapping a charge row to navigate to charge detail (detail page in Change #2).

#### Scenario: User taps charge session
- **WHEN** user taps a charge row
- **THEN** navigation to charge detail screen triggers
```

## openspec/changes/stitch-core-navigation/specs/core-navigation/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/core-navigation/spec.md
- Lines: 1-26
- SHA256: 85fd534934506105d6f0e33c3ec7da6a590de64153e52665be49b92b31a5046c

```md
## ADDED Requirements

### Requirement: Bottom tab bar with 4 tabs
The system SHALL display a fixed bottom navigation bar with 4 tabs: 仪表盘 (Dashboard), 行程 (Trips), 充电 (Charging), 更多 (More). Active tab icon SHALL render in `#A16207` (gold), inactive tabs in `#747878` (outline).

#### Scenario: App launches
- **WHEN** app starts
- **THEN** Dashboard tab is selected by default with gold icon color

#### Scenario: Tab switch
- **WHEN** user taps "行程" tab
- **THEN** Trip list page displays, "行程" icon turns gold, previous tab icon turns gray

### Requirement: Tab bar visual style
The tab bar SHALL have white `#fdf8f8` background, 1px `#E5E5E5` top border, 24px stroke-based icons (1.5px weight), label-caps text below icons.

#### Scenario: Tab bar rendering
- **WHEN** any screen with bottom nav displays
- **THEN** tab bar has white background, 1px top border #E5E5E5, no shadow

### Requirement: Tab bar persistence
The tab bar SHALL remain visible and fixed at the bottom across all 4 main tab pages.

#### Scenario: Scrolling within a tab
- **WHEN** user scrolls trip list
- **THEN** bottom tab bar stays fixed, does not scroll away
```

## openspec/changes/stitch-core-navigation/specs/dashboard-stitch/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/dashboard-stitch/spec.md
- Lines: 1-40
- SHA256: cff6a0250f5d1d6ca98483a7d2511340d2425c57cca16a54c8751a3d91007985

```md
## ADDED Requirements

### Requirement: Vehicle status header
The system SHALL display vehicle name, online/offline status chip, and last-seen timestamp at the top of the dashboard.

#### Scenario: Vehicle online
- **WHEN** vehicle is online
- **THEN** status chip shows "Online" with green tint background

#### Scenario: Vehicle offline
- **WHEN** vehicle is offline
- **THEN** status chip shows "Offline" with gray tint background

### Requirement: Battery status card
The system SHALL display battery percentage (large JetBrains Mono number), estimated range in km, and charge limit percentage. Card uses Stitch card style (white, 1px border, 8px radius, 24px padding).

#### Scenario: Battery display
- **WHEN** dashboard loads with car data
- **THEN** battery card shows "78%" in data-lg (JetBrains Mono 24px), "312 km" range in data-md, charge limit "80%" indicator

### Requirement: Charging status card
The system SHALL display charging state: plugged in / charging / DC charging, charger power in kW, energy added in kWh, time to full charge. Only visible when vehicle is plugged in.

#### Scenario: DC fast charging
- **WHEN** vehicle is DC charging at 120kW
- **THEN** charging card shows "DC Fast Charging", "120 kW", energy added, estimated completion time

### Requirement: Info grid
The system SHALL display a 2-column grid showing: odometer, software version, tire pressures (4 wheels), inside/outside temperatures.

#### Scenario: Info grid rendering
- **WHEN** dashboard loads
- **THEN** grid shows odometer (km), version, FL/FR/RL/RR tire pressures in psi, inside temp, outside temp, each with label-caps label and data-md value

### Requirement: 7-day trend chart
The system SHALL display a 7-day battery level trend as a simple line or bar chart at the bottom of the dashboard.

#### Scenario: Trend data available
- **WHEN** 7 days of battery data exists
- **THEN** trend chart renders with 7 data points, gold `#A16207` line color
```

## openspec/changes/stitch-core-navigation/specs/more-menu-stitch/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/more-menu-stitch/spec.md
- Lines: 1-29
- SHA256: 12e4ea166920b7f92f687f702cd57991dd9abac71014351fba9c41d8b5ff2a2d

```md
## ADDED Requirements

### Requirement: Analysis entry list
The system SHALL display a scrollable list of analysis page entries. Each entry shows: icon, title (body-lg), brief description (body-sm). Entries: 统计 (Statistics), 热力图 (Heatmap), 效率分析 (Efficiency), 续航分析 (Range), 能耗分析 (Energy), 电池健康 (Battery Health), 时间线 (Timeline).

#### Scenario: More menu displays
- **WHEN** user navigates to More tab
- **THEN** all 7 analysis entries display with icons and descriptions

### Requirement: Report & export section
The system SHALL display entries for: 年度报告 PDF, CSV/JSON 导出, 固件版本.

#### Scenario: Export entries
- **WHEN** More menu displays
- **THEN** report and export entries appear below analysis entries in a separated section

### Requirement: Entry tap navigation
The system SHALL navigate to the corresponding detail/analysis page when an entry is tapped. Pages not yet implemented SHALL show placeholder or navigate to stub.

#### Scenario: Tap existing page
- **WHEN** user taps "Statistics" entry
- **THEN** navigation to Statistics screen triggers

### Requirement: Entry row style
Each entry row SHALL use Stitch card style within the list: white background, 1px `#E5E5E5` bottom border (not full card border, since entries are in a grouped list).

#### Scenario: Entry rendering
- **WHEN** More menu displays
- **THEN** entries render with body-lg title, body-sm description, right chevron icon
```

## openspec/changes/stitch-core-navigation/specs/stitch-design-system/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/stitch-design-system/spec.md
- Lines: 1-44
- SHA256: 15e0f9ea0b6f0479e14951c94327f8eabbbab517fc5b0cc3b1b72ac288245a31

```md
## ADDED Requirements

### Requirement: Design token source of truth
The system SHALL define all design tokens (colors, typography, spacing, shapes) in a single shared JSON file at `app_glm/shared/design-tokens.json` that both Android and iOS consume.

#### Scenario: Android reads design tokens
- **WHEN** Android app compiles
- **THEN** Theme.kt uses color values matching design-tokens.json exactly

#### Scenario: iOS reads design tokens
- **WHEN** iOS app launches
- **THEN** AppTheme.swift uses color values matching design-tokens.json exactly

### Requirement: Color palette
The system SHALL use the Stitch white-minimal palette: background `#fdf8f8`, surface `#fdf8f8`, on-surface `#1c1b1b`, primary `#000000`, secondary `#895200`, outline `#747878`, outline-variant `#c4c7c7`.

#### Scenario: Card background
- **WHEN** any card component renders
- **THEN** its background is `#fdf8f8` (surface) with 1px `#c4c7c7` border

### Requirement: Typography scale
The system SHALL use Inter font for all UI text and JetBrains Mono for all numerical data displays. Font sizes: display-lg=32px(700), headline-md=24px(600), body-lg=16px(400), body-sm=14px(400), data-lg=24px(500), data-md=16px(500), label-caps=12px(700/0.05em).

#### Scenario: Numerical value display
- **WHEN** battery percentage "78%" renders
- **THEN** it uses JetBrains Mono, 24px, weight 500, tabular-nums

#### Scenario: Section headline
- **WHEN** a section title like "Trip History" renders
- **THEN** it uses Inter, 24px, weight 600, letter-spacing -0.01em

### Requirement: Card component
The system SHALL render cards with: white background, 1px solid `#E5E5E5` border, 8px corner radius, 24px internal padding, no shadow.

#### Scenario: Dashboard card
- **WHEN** battery status card renders
- **THEN** it has 1px #E5E5E5 border, 8px radius, 24px padding, no elevation shadow

### Requirement: No shadows anywhere
The system SHALL NOT use any elevation shadows or blur effects. Depth is conveyed through border weight only.

#### Scenario: Any UI element
- **WHEN** any component renders
- **THEN** elevation/shadow is zero; borders are the only depth indicator
```

## openspec/changes/stitch-core-navigation/specs/trip-list-stitch/spec.md

- Source: openspec/changes/stitch-core-navigation/specs/trip-list-stitch/spec.md
- Lines: 1-33
- SHA256: 67d3d555ea8401bd268ef1b38e01dec8bf29ba0d487e7f5bba6a9472dc6cbf3b

```md
## ADDED Requirements

### Requirement: Trip list grouped by month
The system SHALL display trips grouped by month with month headers (e.g., "2026年6月"). Each trip row shows: start address → end address, date, distance (km), duration (h:m), battery level change (start% → end%).

#### Scenario: Trip list loads
- **WHEN** user navigates to Trips tab
- **THEN** trips display grouped by month, most recent month first

#### Scenario: Empty state
- **WHEN** no trip data exists
- **THEN** show "No trips recorded" in body-lg, centered

### Requirement: Trip row style
Each trip row SHALL use Stitch card style: white background, 1px `#E5E5E5` border, 8px radius, with address on left (body-lg), date/distance/duration on right (body-sm, right-aligned).

#### Scenario: Trip row rendering
- **WHEN** trip list displays
- **THEN** each row has white background, 1px border, 8px radius, addresses in Inter body-lg, metrics in body-sm

### Requirement: Efficiency badge
The system SHALL display an efficiency badge (green) on trips where efficiency exceeds a threshold (e.g., >90% rated).

#### Scenario: High efficiency trip
- **WHEN** a trip has efficiency > 90%
- **THEN** row shows green "Efficient" badge

### Requirement: Tap to navigate
The system SHALL allow tapping a trip row to navigate to trip detail (detail page in Change #2).

#### Scenario: User taps trip
- **WHEN** user taps a trip row
- **THEN** navigation to trip detail screen triggers
```

