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
