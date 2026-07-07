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
