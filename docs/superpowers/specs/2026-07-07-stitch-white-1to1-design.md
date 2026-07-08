---
comet_change: stitch-white-1to1
role: technical-design
canonical_spec: openspec
archived-with: 2026-07-08-stitch-white-1to1
status: final
---

# Stitch White 1:1 — Technical Design

> Date: 2026-07-07
> Change: `stitch-white-1to1`
> Source: Stitch 项目 `11493757920836657212`「MateLink - Tesla 监控 (简约白)」(Precision Minimalist)
> Target: `app_glm/` (Android Kotlin/Compose + iOS SwiftUI)

## 1. Architecture Overview

```
Stitch MCP (mcp__stitch__get_screen)
   │ 读取 19 页真实 HTML (源真理)
   ▼
/child-claude 子代理（按页切片）
   │ 每子代理 1 页：读 HTML → 解析结构/间距/色值
   ▼
app_glm 双端 UI 层改写
   ├── Android: ui/<page>/<Page>Screen.kt  (Compose, 复用 Stitch 组件)
   └── iOS:    Features/<Page>/<Page>View.swift  (SwiftUI, 复用 Stitch 组件)
   │
   ├── 共享: shared/design-tokens.json (单一令牌源)
   ├── Android theme: ui/theme/Color.kt + Theme.kt (Inter + JetBrains Mono)
   ├── iOS theme: Core/Theme/AppTheme.swift
   └── 组件: StitchCard / StitchStatusChip / StitchDataRow / StitchBottomBar (双端各自实现)
   │
   ▼
数据层（不改）
   ├── Android: DashboardViewModel / DriveViewModel / ChargeViewModel + CarRepository
   └── iOS: 对应 ViewModel + ApiClient
```

**核心约束**：数据层零改动。UI 层即插即用现有 ViewModel。1:1 精度服从数据层稳定（Stitch 文案作 UI label，数据字段以现有为准）。

## 2. 复用 prior change 产物（不重复造轮子）

| 资产 | 路径 | 用途 |
|------|------|------|
| 设计令牌 | `app_glm/shared/design-tokens.json` | 28色+7级排版+5级间距+组件规格，双端单一源 |
| Android 主题 | `ui/theme/Color.kt` `Theme.kt` | Inter+JetBrains Mono，无阴影，StitchLightColors |
| Android 组件 | `ui/components/StitchCard.kt` `StitchStatusChip.kt` `StitchDataRow.kt` `StitchBottomBar.kt` | 1px边框/8px圆角/无elevation |
| Android 字体 | `res/font/jetbrains_mono_*.ttf` | 数字 tabular-nums |
| iOS 主题 | `Core/Theme/AppTheme.swift` | SwiftUI Color/Font 适配 |
| iOS 组件 | `Components/Stitch*.swift` | 同 Android 规格 |
| iOS 字体 | `Resources/JetBrainsMono-*.ttf` + Info.plist | 同 Android |

子代理开工前核实上述资产存在（tasks.md §1 预检）。

## 3. 1:1 还原方法论（子代理执行模板）

每个子代理执行流程：

```
1. mcp__stitch__get_screen(name="projects/11493757920836657212/screens/<screen_id>")
   → 获取 htmlCode.downloadUrl
2. 读取 HTML 内容，解析：
   - 页面结构（section/card/row 层级）
   - 精确色值（内联 style 或 CSS 变量）
   - 字号/字重/行高（映射到 design-tokens.json 的 7 级排版）
   - 间距（8px 基准，映射到 space-xs/sm/md/lg/xl）
   - 边框/圆角（1px #E5E5E5, 8px radius）
   - 状态语义色（在线绿/充电橙/警告红）
   - 文案（中文 label）
3. Android 改写：ui/<page>/<Page>Screen.kt
   - 用 StitchCard 包裹卡片区
   - 用 StitchDataRow 渲染 label:value 行
   - 用 StitchStatusChip 渲染状态
   - 数字用 JetBrains Mono + tabular-nums
   - 现有 ViewModel 数据流不动
4. iOS 改写：Features/<Page>/<Page>View.swift
   - 同结构，SwiftUI 组件
   - 若目录缺失则新建 Features/<Page>/ + 接入 MoreView/导航
5. 接入导航：核实 NavGraph (Android) / NavigationLink (iOS) 跳转可达
6. 自检：无阴影/1px边框/8px圆角/正确字体/中文文案
```

## 4. 页面清单与 screen ID

| # | 页面 | screen ID | 平台现状 |
|---|------|-----------|----------|
| L1 校准 | | | |
| 1 | 仪表盘 | `405f645538ae4a788b30aa4f64550e6f` | 双端已有，校准 |
| 2 | 行程历史 | `11444dd2914644cab88e53dd6973e46e` | 双端已有，校准 |
| 3 | 充电历史 | `2958ceb895414130bb618a34682e26f7` | 双端已有，校准 |
| 4 | 更多菜单 | `607f50c463444dbf8183d6f0e96dfabb` | 双端已有，校准 |
| L2 详情 | | | |
| 5 | 行程详情 | `7262882484106971972` | Android 有 DriveDetailScreen，iOS 有 DriveDetailView |
| 6 | 充电详情 | `12c4a93d5f484d1c89a16c3e385e59cb` | Android 有 ChargeDetailScreen，iOS 有 ChargeDetailView |
| 7 | 当前充电 | `5d52c8ca82df434e9bd4a67e74290ffc` | Android 有 CurrentChargeScreen，iOS 可能缺→新建 |
| L2 分析 | | | |
| 8 | 续航分析 | `c4bf3de8c1ee4f439751ab3bc14fb601` | 双端有 RangeScreen/RangeView |
| 9 | 能耗分析 | `3f828fd2e1bb462bb104b6aae0e19290` | 双端有 EfficiencyScreen/EfficiencyView |
| 10 | 电池健康 | `a903f4ccfaf64988b12eebd9b6b07d5f` | 双端有 BatteryHealthScreen/BatteryHealthView |
| 11 | 待机耗电 | `78dd96dc2e1d4882a30b0af4f9b83f17` | 双端有 VampireScreen/VampireView |
| 12 | 热力图 | `5ddffde05eba4fec9ba278857d5f5b24` | 双端有 HeatmapScreen/HeatmapView |
| 13 | 时间线 | `e1b336b48d1c48cca53d693131a44839` | 双端有 TimelineScreen/TimelineView |
| 14 | 里程钻取 | `9d4bc5d2a8024d0c8397b7d3cd037848` | Android statistics，iOS StatisticsView；超长HTML分段 |
| L2 系统 | | | |
| 15 | 设置 | `4c90a050b87c44b1aaf73a8ba590ad96` | 双端有 SettingsScreen/SettingsView |
| 16 | 固件版本 | `2c1bd185b2d14b5ba5647bd762c9c240` | 双端有 UpdatesScreen/UpdatesView |
| 17 | 关于 | `845c19f9afe94ddc9d1544b3a6936f1c` | 双端有 AboutScreen/AboutView |
| 18 | 哨兵历史 | `7b959ff2df234fe4ba834c7eb96dcd9c` | Android 可能缺→新建，iOS 可能缺→新建 |
| L2 成本 | | | |
| 19 | 成本分析 | `cbf4541b745f447d8de3e67eacc2df50` | 双端有 CostScreen/CostView |

## 5. 跨端一致性规则

- **同页双端同子代理**：保证 Android 与 iOS 渲染一致
- **共享令牌**：所有色值/字号/间距引用 `design-tokens.json`，禁止硬编码
- **组件复用**：卡片用 StitchCard，状态用 StitchStatusChip，数据行用 StitchDataRow，禁自定义
- **数字字体**：所有数值（电量/续航/能耗/费用/功率/效率）用 JetBrains Mono + `tabular-nums`
- **无阴影**：所有 Surface `shadowElevation = 0.dp`（Android）/ 无 `.shadow()` 修饰（iOS）
- **边框分层**：卡片 1px #E5E5E5，模态 1px #171717
- **中文文案**：Tab 标签「仪表盘/行程/充电/更多」，对齐 Stitch

## 6. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 里程钻取 HTML 103894px 超长 | 子代理按 `<section>` 分段读取，优先年→月→日骨架，细节曲线降级 |
| Stitch 多变体选错 | 已选最全版；子代理开工前核对 title/height，发现更全可切换并记录 |
| iOS Feature 缺失断链 | 子代理核实目录，缺则新建 `Features/<Name>/`，接入 MoreView |
| 无编译验证 | 文件结构审查 + 视觉 checklist + Stitch 截图对照；真实编译留待 Mac/JDK |
| 数据字段与 Stitch 文案不匹配 | 以现有 ViewModel 字段为准，Stitch 文案作 UI label；缺失字段占位或降级提示 |
| prior 未归档 capability 重叠 | L1 校准走任务级，不碰 prior delta spec |
| 子代理上下文爆炸 | 每子代理只读 1 页 HTML + 相关现有文件；里程钻取单独分段 |

## 7. 测试策略

**不能做**：Android Gradle 编译（无 JDK）、iOS Xcode 编译（无 Mac）

**能做且必做**：
1. **文件存在性**：19 页 Android Composable + 19 页 iOS SwiftUI View 均存在且非空
2. **视觉 checklist**（每页）：
   - 无阴影/elevation
   - 卡片 1px #E5E5E5 边框
   - 8px 圆角
   - 数字 JetBrains Mono + tabular-nums
   - 文字 Inter
   - 状态语义色正确（在线绿/充电橙/警告红）
3. **Stitch 截图对照**：关键页 Android+iOS 渲染肉眼比对（用 `mcp__stitch__get_screen` 返回的 screenshot.downloadUrl）
4. **数据层未改核实**：`git diff` 确认无 ViewModel/Repository/Dao/Api 改动
5. **导航链路**：所有跳转可达无断链（仪表盘→行程详情、充电历史→充电详情/当前充电、更多→各页、里程钻取年→月→日）

**未来**：Mac/JDK 环境就绪后补真实编译 + 预览截图

## 8. 实施顺序（build 阶段）

按 tasks.md 分组，建议子代理派发顺序：
1. §1 预检（主代理直接做）
2. §2 L1 校准 4 页（并行 4 子代理）— 建立校准基准
3. §3 L2 详情 3 页（并行）— 含地图/曲线，复杂
4. §4 L2 分析 7 页（并行）— 里程钻取单独慢
5. §5 L2 系统 4 页（并行）— 含 iOS 新建
6. §6 L2 成本 1 页
7. §7 导航接入（主代理汇总）
8. §8 验证（主代理 + 抽样）

## 9. Spec Patch

无。open 阶段 4 个 delta spec（stitch-detail-pages/analysis-pages/system-pages/cost-page）已含完整 1:1 Requirement + WHEN/THEN Scenario，本设计不补充。
