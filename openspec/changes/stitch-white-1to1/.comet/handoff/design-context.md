# Comet Design Handoff

- Change: stitch-white-1to1
- Phase: design
- Mode: compact
- Context hash: 3afbddf7beb81c02c596da9435a9668997d2852133371466e46dedc0e53ee932

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/stitch-white-1to1/proposal.md

- Source: openspec/changes/stitch-white-1to1/proposal.md
- Lines: 1-39
- SHA256: 8cfed93d899cd6ffb1c2a68524c81d344beb8dca6804018a4297715f84215d7f

```md
## Why

app_glm 双端虽已通过 prior change `stitch-core-navigation` 建立 Stitch 简约白设计基准（4-Tab 导航 + L1 四页 + 设计令牌 + StitchCard/Chip/DataRow 组件），但存在两个问题：(1) L1 四页是凭 PRD/记忆做的"风格重写"，未对照 Stitch 真实 HTML 验证 1:1；(2) L2/L3 共 15 页（详情页/分析页/系统页）仍为旧风格，与简约白基准脱节。现在 Stitch MCP 已可用，能读取项目 `11493757920836657212` 的真实页面 HTML，可做精确 1:1 还原，消除 L1 偏差并补齐 L2/L3，使全 19 页视觉统一到 Precision Minimalist 瑞士简约白。

## What Changes

- **L1 校准（4页）**：仪表盘/行程历史/充电历史/更多菜单 — 对照 Stitch 真实 HTML 校准颜色/字体/间距/边框/布局/文案，修正 prior 的偏差（任务级，不改 spec）
- **L2/L3 新增 1:1 还原（15页）**：
  - 详情页：行程详情（地图+5曲线）/ 充电详情（4曲线+充电阶段）/ 当前充电（实时监控）
  - 分析页：续航分析 / 能耗分析 / 电池健康 / 待机耗电 / 热力图 / 时间线 / 里程钻取（年→月→日多级）
  - 系统页：设置 / 固件版本 / 关于 / 哨兵历史
  - 成本页：成本分析（月度充电+驾驶习惯）
- **双端**：每页同时还原 Android（Kotlin/Compose）+ iOS（SwiftUI），视觉与 Stitch HTML 1:1
- **数据层不改**：保留现有 ViewModel/Repository 注入，仅替换 UI 视觉呈现
- **复用基础**：`shared/design-tokens.json` + 已有 Stitch 组件（StitchCard/StitchStatusChip/StitchDataRow/StitchBottomBar）+ Inter/JetBrains Mono 字体

## Capabilities

### New Capabilities

- `stitch-detail-pages`: 详情类页面 1:1 还原 — 行程详情（路线地图+速度/功率/海拔/温度曲线 Tab）、充电详情（SoC/功率/电压/温度曲线+恒流/恒压/涓流阶段）、当前充电（1s 功率曲线+ETA+阶段）
- `stitch-analysis-pages`: 分析专题页 1:1 还原 — 续航分析（预估vs实际偏差+评级）、能耗分析（平均能耗+评级+同车型对比）、电池健康（健康度+衰减+循环+维护建议）、待机耗电（耗电来源+趋势+优化建议）、热力图（高频时段+目的地+路线排行）、时间线（24h活动轴）、里程钻取（年度/月度/场景/365热力/Top5/年→月→日钻取）
- `stitch-system-pages`: 系统页 1:1 还原 — 设置（服务器/Token/连接测试/语言/主题/模拟）、固件版本（更新历史时间线）、关于（技术栈+开源许可+车辆摘要）、哨兵历史（事件时间线+灵敏度）
- `stitch-cost-page`: 成本分析页 1:1 还原 — 月度充电成本+驾驶习惯总结+分时电价 TOU

### Modified Capabilities

<!-- L1 四页校准为任务级工作（spec 已要求"匹配 Stitch 设计"，本次用真实 HTML 更精确执行，不改 spec 级需求）。prior change 的 dashboard-stitch/trip-list-stitch/charge-list-stitch/more-menu-stitch delta spec 未归档，不在此修改。 -->

## Impact

- **Affected code**:
  - Android: `app_glm/android/app/src/main/java/com/teslamatelink/ui/` 下 19 个页面子模块的 Composable（dashboard/drives/charges/more/settings/drives(详情)/charges(详情+当前)/cost/range/efficiency/battery/vampire/heatmap/timeline/updates/about/statistics 等）
  - iOS: `app_glm/ios/MateLink/Features/` 下对应 SwiftUI View；可能需新建 Sentry/CurrentCharge/Mileage Feature 目录
  - 复用: `app_glm/shared/design-tokens.json`、`ui/components/Stitch*.kt`、`ios/MateLink/Core/Theme/AppTheme.swift`、`ios/MateLink/Components/Stitch*.swift`
- **Data layer**: 不改 — DashboardViewModel/DriveViewModel/ChargeViewModel 等 + CarRepository/DelegatingCarRepository 注入保持
- **Dependencies**: 依赖现有 Stitch 设计系统（prior change 产物）、Stitch MCP（读取 HTML 源）、docs/git_ref（openclaw/teslamate-mobile Flutter 参考）
- **Verification constraint**: 无 JDK/Xcode 环境，编译验证降级为文件结构审查 + 视觉对照 Stitch 截图（与 prior change 一致策略）
- **Out of scope**: Web/Watch/Widget、新功能、数据层重构、导航结构改造
```

## openspec/changes/stitch-white-1to1/design.md

- Source: openspec/changes/stitch-white-1to1/design.md
- Lines: 1-65
- SHA256: 05342fb9ee9d2b698c6244854fa956f9c2aa95697d7298194045e512af401114

```md
## Context

app_glm 是 MateLink 的 GLM 变体工程，含 Android（Kotlin/Jetpack Compose）+ iOS（SwiftUI）双端。prior change `stitch-core-navigation` 已建立 Stitch 简约白设计基准：
- `shared/design-tokens.json` — Precision Minimalist 设计令牌（28 色 + 7 级排版 + 5 级间距 + 组件规格）
- Android: `ui/theme/Color.kt`+`Theme.kt`（Inter+JetBrains Mono，无阴影）、`ui/components/StitchCard.kt`+`StitchStatusChip.kt`+`StitchDataRow.kt`+`StitchBottomBar.kt`、`res/font/jetbrains_mono_*.ttf`
- iOS: `Core/Theme/AppTheme.swift`、`Components/Stitch*.swift`、`Resources/JetBrainsMono-*.ttf`
- L1 四页（仪表盘/行程历史/充电历史/更多菜单）已重写为 Stitch 风格但未对照真实 HTML

现在 Stitch MCP 可用，能读取项目 `11493757920836657212` 的 19 页真实 HTML。本 change 用真实 HTML 做精确 1:1 还原：L1 校准 + L2/L3 新增。

数据层（CarRepository/DelegatingCarRepository/各 ViewModel）成熟稳定，不改。

## Goals / Non-Goals

**Goals:**
- 19 页 Android+iOS 视觉与 Stitch HTML 1:1（颜色/字体/间距/边框/圆角/布局/文案/状态语义）
- 复用 prior 的设计令牌 + Stitch 组件，不重复造轮子
- 数据层零改动，UI 层即插即用现有 ViewModel
- 双端视觉一致（同页 Android 与 iOS 渲染结果与 Stitch 截图肉眼不可分辨）
- 导航跳转链路完整且中文文案对齐 Stitch

**Non-Goals:**
- 不改 ViewModel/Repository/Room/Retrofit 数据层
- 不做新功能（信息架构/数据字段与现有一致）
- 不做 Web/Watch/Widget
- 不重构导航结构（4-Tab 框架已定）
- 不做编译验证（无 JDK/Xcode，降级为文件结构审查 + 视觉对照）

## Decisions

### D1: 1:1 还原的"源真理"是 Stitch HTML，不是截图
**选择**: 子代理通过 `mcp__stitch__get_screen` 读取每页 HTMLCode，解析其结构/类名/内联样式作为 1:1 还原依据；截图作为视觉参考。
**理由**: HTML 含精确间距/字号/色值/布局结构，截图只有像素。prior change 凭记忆/PRD 做的偏差正源于此。
**替代**: 仅用截图 → 无法精确还原间距字号，否决。

### D2: 双端共享同一设计令牌源，各自平台化
**选择**: `shared/design-tokens.json` 为单一源；Android `Theme.kt` 解析为 Compose `Color`/`TextStyle`/`Dp`，iOS `AppTheme.swift` 解析为 SwiftUI `Color`/`Font`/`CGFloat`。组件级（StitchCard 等）双端各自实现但规格一致。
**理由**: 双端一致性需单一令牌源；平台 API 差异需各自适配。prior 已建立此模式，沿用。

### D3: 执行方式 — /child-claude 子代理按页并行
**选择**: build 阶段用 /child-claude 派发子代理，每子代理负责 1 页的双端 1:1 还原（读 Stitch HTML → 改 Android Composable + 改 iOS SwiftUI View）。复杂页（里程钻取 103894px）单独拆分。
**理由**: 19 页 × 2 端 = 38 个屏幕重写，串行不现实；按页并行是最高效切片；每页独立可验证。
**替代**: 按平台分（先全 Android 再全 iOS）→ 跨端不一致风险高，否决。按页双端同做保证一致性。

### D4: L1 校准是任务级，不开新 spec
**选择**: L1 四页校准作为 tasks.md 中的任务，不新增/修改 capability spec。prior 的 dashboard-stitch 等 delta spec 已要求"匹配 Stitch 设计"，本次只是用真实 HTML 更精确执行。
**理由**: spec 级需求未变（仍是"匹配 Stitch 设计"），变的是实现精度。避免与未归档的 prior delta spec 冲突。

### D5: 缺失 iOS Feature 目录就地新建
**选择**: 若 iOS 缺 Sentry/CurrentCharge/Mileage 等 Feature 目录，子代理在还原时新建对应 `Features/<Name>/<Name>View.swift` 并接入 MoreView/导航。
**理由**: 这些页 Android 已有对应 screen，iOS 需补齐才能 1:1。

### D6: 验证策略 — 文件结构 + 视觉对照
**选择**: 无编译环境，验证 = (1) 文件存在且非空 (2) 关键视觉元素 checklist（无阴影/1px边框/8px圆角/JetBrains Mono数字/中文文案）(3) 对照 Stitch 截图。与 prior change 一致。
**理由**: 真实编译需 Mac/JDK，当前不具备；文件结构审查 + checklist 能捕获大部分偏差。

## Risks / Trade-offs

- **[里程钻取 HTML 103894px 超长]** → 子代理分段读取（按 `<section>` 或滚动边界），优先还原年→月→日三级钻取骨架，细节曲线降级
- **[Stitch 多变体选错]** → 已在 proposal 选定最全/最新版 screen ID；子代理开工前核对 title 与 height，若发现更全变体可切换并记录
- **[iOS Feature 缺失导致断链]** → 子代理核实目录，缺则新建；MoreView 接入新页入口
- **[无编译验证]** → 文件结构审查 + 视觉 checklist + Stitch 截图对照；真实编译留待 Mac/JDK 环境
- **[数据层字段与 Stitch 文案不匹配]** → 以现有 ViewModel 字段为准，Stitch 文案作 UI label；若字段缺失则占位或降级提示（与 PRD"降级策略"一致）
- **[prior 未归档导致 capability 重叠]** → 本 change 只对 L2/L3 开新 capability，L1 校准走任务级，不碰 prior delta spec
- **[子代理上下文爆炸]** → 每子代理只读 1 页 HTML + 相关现有文件；里程钻取单独处理
```

## openspec/changes/stitch-white-1to1/tasks.md

- Source: openspec/changes/stitch-white-1to1/tasks.md
- Lines: 1-58
- SHA256: 8be0393b077e46cd2fcfe70b550c0fc88ebd0a1d131d9031c391e1a71ac867d9

```md
# Tasks — stitch-white-1to1

> 执行策略：build 阶段用 /child-claude 派发子代理，每页一子代理负责双端（Android Composable + iOS SwiftUI）1:1 还原。每子代理开工前用 `mcp__stitch__get_screen` 读取对应 screen ID 的 HTML 作为源真理。

## 1. 预检（核实 prior change 产物可用）

- [ ] 1.1 核实 `app_glm/shared/design-tokens.json` 存在且双端解析正常（Android Theme.kt / iOS AppTheme.swift）
- [ ] 1.2 核实 Stitch 组件可用：Android `StitchCard/StitchStatusChip/StitchDataRow/StitchBottomBar`、iOS `StitchCard/StitchStatusChip/StitchDataRow`
- [ ] 1.3 核实字体资源：Android `res/font/jetbrains_mono_*.ttf`、iOS `Resources/JetBrainsMono-*.ttf` + Info.plist 注册
- [ ] 1.4 核实 Stitch MCP 可用（`mcp__stitch__get_screen` 测试一页）

## 2. L1 校准（4页 — 对照真实 HTML 修正 prior 偏差）

- [ ] 2.1 仪表盘 `405f645538ae4a788b30aa4f64550e6f` — 校准 Android DashboardScreen + iOS DashboardView（状态头/电池卡/充电卡/信息网格/7日趋势，中文 Tab「仪表盘」）
- [ ] 2.2 行程历史 `11444dd2914644cab88e53dd6973e46e` — 校准 Android DriveListScreen + iOS DriveListView（月分组/卡片行/效率徽章，中文 Tab「行程」）
- [ ] 2.3 充电历史 `2958ceb895414130bb618a34682e26f7` — 校准 Android ChargeListScreen + iOS ChargeListView（实时置顶卡/月分组/AC-DC 标识，中文 Tab「充电」）
- [ ] 2.4 更多菜单 `607f50c463444dbf8183d6f0e96dfabb` — 校准 Android MoreScreen + iOS MoreView（车辆摘要+分组导航+报告入口，中文 Tab「更多」）

## 3. L2 详情页（3页）

- [ ] 3.1 行程详情 `7262882484106971972` — 还原 Android DriveDetailScreen + iOS DriveDetailView（地图+5曲线 Tab+摘要，接行程列表跳转）
- [ ] 3.2 充电详情 `12c4a93d5f484d1c89a16c3e385e59cb` — 还原 Android ChargeDetailScreen + iOS ChargeDetailView（4曲线+充电阶段+月度习惯，接充电列表跳转）
- [ ] 3.3 当前充电 `5d52c8ca82df434e9bd4a67e74290ffc` — 还原 Android CurrentChargeScreen + iOS CurrentChargeView（1s功率曲线+ETA+阶段；iOS 若缺则新建 Features/CurrentCharge/）

## 4. L2 分析页（7页）

- [ ] 4.1 续航分析 `c4bf3de8c1ee4f439751ab3bc14fb601` — 还原 Android RangeScreen + iOS RangeView（偏差+影响因素+评级+同车型对比）
- [ ] 4.2 能耗分析 `3f828fd2e1bb462bb104b6aae0e19290` — 还原 Android EfficiencyScreen + iOS EfficiencyView（平均能耗+评级+趋势+散点+同车型+建议）
- [ ] 4.3 电池健康 `a903f4ccfaf64988b12eebd9b6b07d5f` — 还原 Android BatteryHealthScreen + iOS BatteryHealthView（健康度+衰减+循环+温度+维护建议）
- [ ] 4.4 待机耗电 `78dd96dc2e1d4882a30b0af4f9b83f17` — 还原 Android VampireScreen + iOS VampireView（损耗+来源+趋势+优化建议）
- [ ] 4.5 热力图 `5ddffde05eba4fec9ba278857d5f5b24` — 还原 Android HeatmapScreen + iOS HeatmapView（24h网格+目的地+路线排行）
- [ ] 4.6 时间线 `e1b336b48d1c48cca53d693131a44839` — 还原 Android TimelineScreen + iOS TimelineView（24h活动轴+事件分段）
- [ ] 4.7 里程钻取 `9d4bc5d2a8024d0c8397b7d3cd037848` — 还原 Android Statistics/MileageScreen + iOS StatisticsView/MileageView（年度+月度+场景+365热力+Top5+年→月→日钻取；超长 HTML 分段读取）

## 5. L2 系统页（4页）

- [ ] 5.1 设置 `4c90a050b87c44b1aaf73a8ba590ad96` — 还原 Android SettingsScreen + iOS SettingsView（服务器/Token/连接测试/语言/主题/模拟/实例状态）
- [ ] 5.2 固件版本 `2c1bd185b2d14b5ba5647bd762c9c240` — 还原 Android UpdatesScreen + iOS UpdatesView（当前版本+历史时间线）
- [ ] 5.3 关于 `845c19f9afe94ddc9d1544b3a6936f1c` — 还原 Android AboutScreen + iOS AboutView（品牌+车辆摘要+版本+技术栈+开源许可）
- [ ] 5.4 哨兵历史 `7b959ff2df234fe4ba834c7eb96dcd9c` — 还原 Android SentryScreen + iOS SentryView（事件时间线+灵敏度；iOS 若缺则新建 Features/Sentry/）

## 6. L2 成本页（1页）

- [ ] 6.1 成本分析 `cbf4541b745f447d8de3e67eacc2df50` — 还原 Android CostScreen + iOS CostView（月度成本+驾驶习惯+TOU 分时电价）

## 7. 导航与入口接入

- [ ] 7.1 Android NavGraph 核实所有 19 页路由注册 + 跳转链路（仪表盘→行程详情、充电历史→充电详情/当前充电、更多→各分析/系统页、里程钻取年→月→日）
- [ ] 7.2 iOS MoreView/ContentView 核实所有 19 页入口 + NavigationLink 链路
- [ ] 7.3 中文文案对齐 Stitch（Tab 标签 仪表盘/行程/充电/更多；页面标题；状态文案）

## 8. 验证（文件结构 + 视觉 checklist）

- [ ] 8.1 文件存在性：19 页 Android Composable + 19 页 iOS SwiftUI View 均存在且非空
- [ ] 8.2 视觉 checklist：无阴影/elevation、所有卡 1px #E5E5E5 边框、8px 圆角、数字 JetBrains Mono tabular-nums、文字 Inter
- [ ] 8.3 Stitch 截图对照：每页 Android+iOS 渲染与 Stitch screenshot 肉眼比对（关键页抽样）
- [ ] 8.4 数据层未改核实：grep 确认无 ViewModel/Repository/Dao 改动
- [ ] 8.5 导航链路核实：所有跳转可达，无断链
```

## openspec/changes/stitch-white-1to1/specs/stitch-analysis-pages/spec.md

- Source: openspec/changes/stitch-white-1to1/specs/stitch-analysis-pages/spec.md
- Lines: 1-58
- SHA256: 198494585e35eb15118dc01f2cd7559d5acfc2e3ec576a4fd9e3aded7234e74a

```md
## ADDED Requirements

### Requirement: 续航分析页 1:1 Stitch 还原
续航分析页（RangeScreen / RangeView）SHALL 1:1 还原 Stitch screen `c4bf3de8c1ee4f439751ab3bc14fb601`「MateLink 续航分析 (Swiss Minimal)」，含预估 vs 实际偏差、影响因素、综合续航评级、同车型相对位置。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入续航分析
- **THEN** 偏差卡 + 影响因素列表 + 评级卡视觉与 Stitch HTML 一致（白底/1px 边框/JetBrains Mono 数字）

### Requirement: 能耗分析页 1:1 Stitch 还原
能耗分析页（EfficiencyScreen / EfficiencyView）SHALL 1:1 还原 Stitch screen `3f828fd2e1bb462bb104b6aae0e19290`「MateLink 能耗分析 (Swiss Minimal)」，含平均能耗、评级、趋势、分布、同车型对比、优化建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入能耗分析
- **THEN** 能耗评级 + 趋势曲线 + 散点分布 + 同车型对比 + 建议卡视觉与 Stitch 一致

### Requirement: 电池健康页 1:1 Stitch 还原
电池健康页（BatteryHealthScreen / BatteryHealthView）SHALL 1:1 还原 Stitch screen `a903f4ccfaf64988b12eebd9b6b07d5f`「MateLink 电池健康 (中文导航)」，含健康度、容量衰减、循环统计、温度分布、维护建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入电池健康
- **THEN** 健康度主卡 + 衰减曲线 + 循环统计 + 温度分布 + 维护建议视觉与 Stitch 一致

### Requirement: 待机耗电页 1:1 Stitch 还原
待机耗电页（VampireScreen / VampireView）SHALL 1:1 还原 Stitch screen `78dd96dc2e1d4882a30b0af4f9b83f17`「MateLink 待机耗电详情 (Swiss Minimal 优化)」，含总损耗、平均功率、耗电来源（哨兵/第三方唤醒/温度预调节）、趋势、优化建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入待机耗电
- **THEN** 损耗总览 + 耗电来源分解 + 趋势曲线 + 优化建议视觉与 Stitch 一致

### Requirement: 热力图页 1:1 Stitch 还原
热力图页（HeatmapScreen / HeatmapView）SHALL 1:1 还原 Stitch screen `5ddffde05eba4fec9ba278857d5f5b24`「MateLink 热力图 (Swiss Minimal)」，含高频时间段、常去目的地、路线排行。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入热力图
- **THEN** 24h 热力网格 + 目的地列表 + 路线排行视觉与 Stitch 一致

### Requirement: 时间线页 1:1 Stitch 还原
时间线页（TimelineScreen / TimelineView）SHALL 1:1 还原 Stitch screen `e1b336b48d1c48cca53d693131a44839`「MateLink 时间线 (Swiss Minimal)」，含 24h 活动轴、事件分段（驾驶/充电/待机/哨兵）。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入时间线
- **THEN** 24h 活动轴 + 事件分段色块 + 事件详情视觉与 Stitch 一致

### Requirement: 里程钻取页 1:1 Stitch 还原
里程钻取页（Statistics/MileageScreen / StatisticsView/MileageView）SHALL 1:1 还原 Stitch screen `9d4bc5d2a8024d0c8397b7d3cd037848`「MateLink 里程钻取 (Swiss Minimal)」，含年度总里程、月度趋势、场景分布、365 热力、Top5、年→月→日多级钻取。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入里程钻取
- **THEN** 年度总览 + 月度趋势 + 365 热力 + Top5 视觉与 Stitch 一致

#### Scenario: 年→月→日钻取
- **WHEN** 用户点击某月再点击某日
- **THEN** 钻取到月度详情再到当日行程列表，视觉与 Stitch 一致

#### Scenario: 超长 HTML 分段读取
- **WHEN** 子代理读取 103894px 超长 HTML
- **THEN** 按 section 分段读取，优先还原年→月→日三级骨架，细节曲线降级处理
```

## openspec/changes/stitch-white-1to1/specs/stitch-cost-page/spec.md

- Source: openspec/changes/stitch-white-1to1/specs/stitch-cost-page/spec.md
- Lines: 1-16
- SHA256: 648f9586bb3cf89655d61f402cbb362e32f18fa126e4156ef3c8d548c0ffa6fb

```md
## ADDED Requirements

### Requirement: 成本分析页 1:1 Stitch 还原
成本分析页（CostScreen / CostView）SHALL 1:1 还原 Stitch screen `cbf4541b745f447d8de3e67eacc2df50`「MateLink 成本分析 (Swiss Minimal)」，含月度充电成本、驾驶习惯总结、分时电价 TOU 成本统计。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单或月度摘要进入成本分析
- **THEN** 月度成本卡 + 驾驶习惯总结 + TOU 时段分解视觉与 Stitch HTML 一致（白底/1px #E5E5E5 边框/8px 圆角/无阴影/JetBrains Mono 数字 tabular-nums）

#### Scenario: TOU 分时电价
- **WHEN** 用户查看成本分析
- **THEN** 成本按 TOU 时段（峰/平/谷）分解显示，色块语义与 Stitch 一致

#### Scenario: 数据层保留
- **WHEN** 页面渲染成本数据
- **THEN** 调用现有 CostViewModel/TariffConfig，UI 仅改视觉不改计算逻辑
```

## openspec/changes/stitch-white-1to1/specs/stitch-detail-pages/spec.md

- Source: openspec/changes/stitch-white-1to1/specs/stitch-detail-pages/spec.md
- Lines: 1-34
- SHA256: 02efbca0da2fa43b6cc0d7ca591cb424e76d0c327885e1e536df4c176672d2fc

```md
## ADDED Requirements

### Requirement: 行程详情页 1:1 Stitch 还原
行程详情页（DriveDetailScreen / DriveDetailView）SHALL 1:1 还原 Stitch screen `7262882484106971972`「MateLink 行程详情 (Swiss Minimal)」的视觉，含路线地图、距离/时长/最高速/均速/能耗/效率摘要卡、速度/功率/海拔/车内温/车外温 5 曲线 Tab 切换。数据源为现有 DriveViewModel。

#### Scenario: 视觉 1:1
- **WHEN** 用户从行程历史点击某条行程进入详情
- **THEN** 页面视觉（纯白底/1px #E5E5E5 边框卡/8px 圆角/无阴影/Inter 文字/JetBrains Mono 数字 tabular-nums）与 Stitch HTML 一致

#### Scenario: 5 曲线 Tab 切换
- **WHEN** 用户在行程详情切换速度/功率/海拔/车内温/车外温 Tab
- **THEN** 曲线图区域切换，金色 #A16207 主线，坐标轴 label-caps 风格

#### Scenario: 数据降级标识
- **WHEN** 某曲线只有摘要数据无完整时间序列
- **THEN** 显示降级图表并标注"模拟数据 — 基于摘要"（与 PRD 一致）

### Requirement: 充电详情页 1:1 Stitch 还原
充电详情页（ChargeDetailScreen / ChargeDetailView）SHALL 1:1 还原 Stitch screen `12c4a93d5f484d1c89a16c3e385e59cb`「MateLink 充电详情 (Swiss Minimal)」，含 SoC/功率/电压/温度 4 曲线 + 恒流/恒压/涓流阶段划分 + 月度充电习惯。数据源为现有 ChargeViewModel。

#### Scenario: 4 曲线 + 阶段
- **WHEN** 用户进入某次充电详情
- **THEN** 显示 4 曲线 Tab + 充电阶段分段标识（恒流/恒压/涓流），视觉与 Stitch 一致

### Requirement: 当前充电实时监控页 1:1 Stitch 还原
当前充电页（CurrentChargeScreen / CurrentChargeView）SHALL 1:1 还原 Stitch screen `5d52c8ca82df434e9bd4a67e74290ffc`「MateLink 当前充电 (Current Charge)」，含 1s 功率曲线 + ETA + 当前阶段 + 实时 SoC。数据源为现有充电实时数据流。

#### Scenario: 实时监控
- **WHEN** 车辆正在充电且用户从充电历史置顶卡或仪表盘进入
- **THEN** 显示实时 SoC/功率/ETA/阶段，1s 曲线滚动，视觉与 Stitch 一致

#### Scenario: iOS Feature 缺失补建
- **WHEN** iOS 无 CurrentCharge Feature 目录
- **THEN** 子代理新建 `Features/CurrentCharge/CurrentChargeView.swift` 并接入充电列表置顶入口
```

## openspec/changes/stitch-white-1to1/specs/stitch-system-pages/spec.md

- Source: openspec/changes/stitch-white-1to1/specs/stitch-system-pages/spec.md
- Lines: 1-37
- SHA256: 900a271c876fb953ee21fd7228118b345b0a00df36ef31744d53520a4e477fec

```md
## ADDED Requirements

### Requirement: 设置页 1:1 Stitch 还原
设置页（SettingsScreen / SettingsView）SHALL 1:1 还原 Stitch screen `4c90a050b87c44b1aaf73a8ba590ad96`「MateLink 设置 (中文版)」，含服务器地址/API Token/连接测试/保存/语言切换/主题切换/模拟模式/实例连接状态。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入设置
- **THEN** 表单输入（1px 边框 focus 转黑 #171717）+ 开关 + 连接状态 chip 视觉与 Stitch 一致

#### Scenario: 数据层保留
- **WHEN** 用户测试连接或保存
- **THEN** 调用现有 SettingsViewModel/SecureSettingsDataStore，UI 仅改视觉不改逻辑

### Requirement: 固件版本页 1:1 Stitch 还原
固件版本页（UpdatesScreen / UpdatesView）SHALL 1:1 还原 Stitch screen `2c1bd185b2d14b5ba5647bd762c9c240`「MateLink 固件版本 (Swiss Minimal)」，含当前版本 + 更新历史时间线。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入固件版本
- **THEN** 当前版本卡 + 历史时间线视觉与 Stitch 一致

### Requirement: 关于页 1:1 Stitch 还原
关于页（AboutScreen / AboutView）SHALL 1:1 还原 Stitch screen `845c19f9afe94ddc9d1544b3a6936f1c`「MateLink 关于 (Swiss Minimal)」（5330 高最全版），含品牌/车辆基础信息/应用版本/平台技术栈/开源组件许可。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入关于页
- **THEN** 品牌 + 车辆摘要 + 版本 + 技术栈 + 开源许可视觉与 Stitch 一致

### Requirement: 哨兵历史页 1:1 Stitch 还原
哨兵历史页（SentryScreen / SentryView）SHALL 1:1 还原 Stitch screen `7b959ff2df234fe4ba834c7eb96dcd9c`「MateLink 哨兵历史 (Swiss Minimal)」，含事件时间线 + 灵敏度设置。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入哨兵历史
- **THEN** 事件时间线 + 灵敏度卡视觉与 Stitch 一致

#### Scenario: iOS Feature 缺失补建
- **WHEN** iOS 无 Sentry Feature 目录
- **THEN** 子代理新建 `Features/Sentry/SentryView.swift` 并接入 MoreView 入口
```

