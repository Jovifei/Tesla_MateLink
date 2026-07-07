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
