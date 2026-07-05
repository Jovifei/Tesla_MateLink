# MateLink Stitch 1:1 — Baseline Page Mapping

> 范围：`app_mimo/`（Android · iOS · Web）vs Stitch 白色简约瑞士风项目 `11493757920836657212`（Precision Minimalist）。
> 依据：`docs/PRD/MateLink_Stitch_Swiss_PRD_2026-07-05.md`、`docs/PRD/MateLink_UI_PRD.md`、`app_mimo/README.md` 及三端真实源码文件。
> 基准日期：2026-07-05。仅记录仓库现状，不臆测不存在的文件。

## 1. 页面映射表

图例：✅ 文件存在且已连入活动导航壳；📦 文件存在但未连入活动导航壳；❌ 无文件；❓ 不确定/需核实。

| Stitch 页面 | 产品角色 | Android 现状 | iOS 现状 | Web 现状 | Gap / mismatch | 建议归属 |
|---|---|---|---|---|---|---|
| dashboard | 实时总览（电量/续航/状态/胎压/7日趋势） | ✅ `DashboardScreen.kt`（`MateLinkNavHost` 4-Tab 起始页） | ✅ `DashboardView.swift`（`ContentView` Tab） | ✅ `Dashboard.tsx`（`/dashboard`，侧栏首项） | Android `MateLinkNavHost` 调用 `DashboardScreen()` 未传任何 `onNavigateTo*` 回调，钻取（行程/充电/电池/里程…）在活动壳中失效；Web 用 14 项侧栏非 4-Tab | Android |
| trip history | 行程历史列表（月切换/筛选/高效徽章） | ✅ `DrivesScreen.kt`（Tab，label=`Drives`） | ✅ `DriveListView.swift`（Tab） | ✅ `Drives.tsx`（`/drives`） | 三端 Tab/标题仍为 `Drives`，未对齐 Stitch「行程历史」中文口径 | 跨端（文案） |
| trip detail | 单次行程复盘（地图+5曲线 Tab） | 📦 `DriveDetailScreen.kt` 仅在 `NavGraph.kt` 注册，`MateLinkNavHost` 中 `DrivesScreen()` 未传 `onNavigateToDriveDetail` → 活动壳不可达 | ✅ `DriveDetailView.swift`（`NavigationLink`） | ✅ `DriveDetail.tsx`（`/drives/:id`） | Android 详情在活动导航壳中无入口 | Android |
| charge history | 充电历史列表（AC/DC/总电量/总费用/实时卡） | ✅ `ChargesScreen.kt`（Tab，label=`Charges`） | ✅ `ChargeListView.swift`（Tab） | ✅ `Charges.tsx`（`/charges`） | Android `ChargesScreen()` 未传 `onNavigateToChargeDetail`/`onNavigateToCurrentCharge`，列表→详情/实时链路在活动壳断开；Web 无「正在充电」置顶实时卡入口 | Android / Web |
| charge detail | 单次充电复盘（4曲线+阶段划分） | 📦 `ChargeDetailScreen.kt` 仅 `NavGraph.kt`，活动壳不可达 | ✅ `ChargeDetailView.swift` | ✅ `ChargeDetail.tsx`（`/charges/:id`） | 同上，Android 链路断裂 | Android |
| current charge | 充电中实时监控（1s 功率曲线/ETA/阶段） | 📦 `CurrentChargeScreen.kt` 仅 `NavGraph.kt`，活动壳不可达 | ❌ 无 `CurrentChargeView`（README 确认 iOS 缺失） | ✅ `CurrentCharge.tsx`（`/current-charge`）但侧栏无入口 | iOS 整页缺失；Android 不可达；Web 有路由无入口 | iOS（新建）/ Android（连线）/ Web（入口） |
| heatmap | 高频时段/常去目的地/路线排行 | ❓ README 标 ✅ 但 `ui/screens/` 下无独立 `HeatmapScreen.kt`，疑似内嵌于 `StatsScreen`，需核实 | ✅ `HeatmapView.swift`（`MoreView` 入口） | ✅ `Heatmap.tsx`（`/heatmap`） | Android 是否独立页存疑 | Android（核实） |
| range analysis | 预估 vs 实际偏差/影响因素/评级 | ❓ README 标 ✅ 但无独立 `RangeScreen.kt`，疑似内嵌，需核实 | ✅ `RangeView.swift`（`MoreView`） | ❌ 无 Range 页（grep 确认无 `Range.tsx`/`Vampire.tsx`/`Timeline.tsx`/`CostAnalysis.tsx`） | Web 整页缺失；Android 独立页存疑 | Web（新建）/ Android（核实） |
| efficiency analysis | 平均能耗/评级/散点/同车型对比 | ❓ README 标 ✅ 但无独立 `EfficiencyScreen.kt`，疑似内嵌，需核实 | ✅ `EfficiencyView.swift`（`MoreView`） | ✅ `EfficiencyCurve.tsx`（`/efficiency`） | Android 独立页存疑；命名 `EfficiencyCurve` ≠ Stitch「效率分析」 | Android（核实） |
| battery health | 健康度/容量衰减/循环/温度/维护建议 | 📦 `BatteryScreen.kt` 仅 `NavGraph.kt`；`MateLinkNavHost` 的 `DashboardScreen()` 未传 `onNavigateToBattery`，且无 More 菜单托管 → 不可达 | ✅ `BatteryHealthView.swift`（`MoreView`） | ✅ `BatteryHealth.tsx`（`/battery`，侧栏） | Android 不可达 | Android |
| vampire drain | 待机耗电/耗电来源/优化建议 | ❓ README 标 ✅ 但无独立 `VampireScreen.kt`，需核实 | ✅ `VampireView.swift`（`MoreView`） | ❌ 无 Vampire 页 | Web 缺失；Android 存疑 | Web（新建）/ Android（核实） |
| mileage drill-down | 年度/月度/场景/365热力/Top5/钻取 | 📦 `MileageScreen.kt` 仅 `NavGraph.kt`，活动壳不可达 | ❌ 无 `MileageView`（README 确认 iOS 缺失） | ✅ `Mileage.tsx`（`/mileage`） | iOS 整页缺失；Android 不可达 | iOS（新建）/ Android（连线） |
| more | 分析/报告/系统入口中转页（车辆摘要+分组） | ❌ 无 `MoreScreen.kt`；`MateLinkNavHost` 将 `More` Tab 直接映射到 `SettingsScreen` | ✅ `MoreView.swift`（Tab，托管全部分析入口） | ❌ 无 More 页（采用 14 项侧栏替代） | Android 无 More 中转页（More=Settings）；Web 信息架构与 Stitch 4-Tab+More 不一致 | Android（新建 More）/ Web（IA 决策） |
| settings | 服务器/Token/连接测试/语言/主题/模拟 | ✅ `SettingsScreen.kt`（同时充当 `More` Tab；`NavGraph.kt` 中独立注册） | ✅ `SettingsView.swift`（仅经 `MoreView` 进入，非 Tab） | ✅ `Settings.tsx`（侧栏） | Android Settings 兼 More 角色；iOS Settings 不在 Tab；多实例 Android ✅ / iOS ❌（README 确认） | 跨端（IA + iOS 多实例） |
| about | 品牌/技术栈/依赖/车辆摘要/许可 | ❓ 无独立 `AboutScreen.kt`；`SettingsScreen.kt` 含 `version` 字样，可能内嵌版本号 | ✅ `AboutView` 内嵌于 `SettingsView.swift`（非独立文件） | ❌ 无 About 页 | Web 缺失；Android 是否有独立 About 存疑；iOS 非独立文件 | Web（新建）/ Android（核实） |

> Stitch PRD 另列「统计-年份/月度」「时间线」「固件版本」「哨兵历史」「目的地」「成本分析」等页。仓库现状：Android `StatsScreen`/`SoftwareVersionsScreen`/`SentryHistoryScreen`/`CountriesVisitedScreen`/`RegionsVisitedScreen`/`WhereWasIScreen`/`TripsScreen` 在 `NavGraph.kt` 注册但活动壳不可达；iOS `StatisticsView`/`UpdatesView`/`TimelineView`/`DestinationsView`/`CostView` 经 `MoreView` 可达；Web 有 `Statistics`/`SoftwareVersions`/`SentryEvents`/`TopDestinations`/`CountriesVisited`/`Trips`，缺 `Timeline`/`Cost`/`Range`/`Vampire`。这些不在本表最小 16 页范围内，仅作上下文。

## 2. 关键不确定性（需后续核实，不要猜）

1. **Android 双 NavHost 矛盾**：`MainActivity.kt` → `MateLinkNavHost()`（4-Tab，`More`→`SettingsScreen`，仅 4 个 `composable`）。`NavGraph.kt` 含全部 ~25 条路由但**未被 `MainActivity` 引用**。结论：活动壳中分析类页面全部不可达，README「✅ 已完成」与导航现实不符。需确认 `NavGraph.kt` 是历史迁移残留还是待接入。
2. **Android 内嵌页存疑**：`Heatmap`/`Range`/`Efficiency`/`Vampire` 在 README 标 ✅ 但 `ui/screens/` 下无独立 Screen 文件，可能内嵌于 `StatsScreen`/`BatteryScreen`。实现侧需逐一核实是否单页可导航。
3. **Android About 存疑**：无独立 `AboutScreen.kt`，`SettingsScreen.kt` 出现 `version` 字样，是否满足 Stitch「关于」信息矩阵未核实。

## 3. 已存在的可复用基础

- **跨端共享**（`app_mimo/shared/`）：GCJ-02 坐标转换（含港澳豁免）、`TariffConfig` 分时电价、ISO 8601 解析。三端行为一致，是地图与成本页的统一基础。
- **数据层降级模式**：`DelegatingCarRepository`（Network-First + 缓存 + Mock 可切换）三端均有类比实现，是所有页面的数据契约基础。
- **Android 成熟基建**：Room v12（14 实体/11 迁移）、Hilt（3 模块）、WorkManager 同步、完整通知系统（充电/哨兵/胎压/OTA/里程/电池 6 类 + BootReceiver）、Glance Widget、多实例 CRUD、年度报告 PDF + CSV/JSON 导出、`UrlSecurity` 运行时 HTTPS/LAN 守卫、`EncryptedSharedPreferences`。可直接复用到所有分析/报告页。
- **iOS**：SwiftUI + Swift Charts、`MoreView` 已实现 Stitch「更多」IA 中转页（最接近 Stitch 模型）、Keychain `WhenUnlockedThisDeviceOnly`、Onboarding 持久化、SceneKit 3D。
- **Web**：React 19 + Vite + Zustand store、Recharts、Leaflet、`mock_data.json` 离线数据、online/offline 横幅、Mock 模式横幅、`store` 中的 `theme`/`mockMode`/`currentCarId` 全局状态。
- **多语言**：三端均支持 EN/中/日/德/法 5 语；iOS `Resources/` 5 个 `.lproj`。

## 4. 对白色简约瑞士风（Precision Minimalist）的 Top 5 不一致

1. **Android 活动导航壳与分析页脱节**：`MateLinkNavHost` 仅 4 个 `composable`，`More` Tab 直跳 `SettingsScreen`，所有分析页（电池/里程/统计/哨兵/行程详情/充电详情/当前充电…）在活动壳中无入口；`NavGraph.kt` 全量路由未被 `MainActivity` 使用。Stitch「更多 → 分析专题」钻取链在 Android 完全断裂。
2. **「更多」中转页仅 iOS 落地**：Stitch 规范 4-Tab + More（车辆摘要 + 分组导航 + 关键数值预览）。Android 无 `MoreScreen`（More=Settings）；Web 用 14 项侧栏替代 4-Tab+More。三端信息架构无一致性。
3. **Web 缺 5+ 个 Stitch 页面**：`Range`/`Vampire`/`Timeline`/`Cost Analysis`/`About`/`More` 均无文件；`CurrentCharge` 有路由但侧栏无入口。Web 远未达到 Stitch 页面集合。
4. **iOS 缺 3 个 Stitch 页面**：`CurrentCharge`/`Mileage`/`Sentry` 整页缺失（README 确认）；`About` 内嵌于 Settings 非独立页；`Mileage` 在 README 标 iOS ❌。
5. **视觉系统未对齐 Precision Minimalist**：Stitch 白色简约风要求纯白底 / 黑主色 / 金点缀 / 8px 圆角 / 无阴影靠边框分层 / JetBrains Mono `tabular-nums`。三端 README 仅提及「主题切换」但未确认落地白色简约设计令牌；Android Tab 标签仍为英文 `Dashboard/Drives/Charges/More`，未对齐 Stitch 中文 `仪表盘/行程/充电/更多` 口径。

## 5. 落地建议优先级（实现导向）

1. **P0 Android 导航壳修复**：决策 `NavGraph.kt` 去留——要么 `MainActivity` 切到 `NavGraph` 并补 4-Tab Scaffold，要么把分析页路由并入 `MateLinkNavHost` 并补 `MoreScreen`。否则所有 Android 分析页无法验收。
2. **P0 三端 More 中转页统一**：以 iOS `MoreView` 为蓝本，Android 新建 `MoreScreen`，Web 决策是否保留侧栏或加 More 入口。
3. **P1 补齐 Web 缺页**：`Range`/`Vampire`/`Timeline`/`Cost`/`About`，复用 `store` + Recharts + 现有 `mock_data.json`。
4. **P1 补齐 iOS 缺页**：`CurrentCharge`（复用 `ChargeDetailView` 曲线组件）、`Mileage`（复用 Web `Mileage.tsx` 数据契约）。
5. **P2 视觉令牌统一**：在三端 theme 中落地 Precision Minimalist 设计令牌（白底/黑/金/8px/无边框阴影/`tabular-nums`），并统一 Tab 中文文案。
