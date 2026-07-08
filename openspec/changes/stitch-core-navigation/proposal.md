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
