# Brainstorm Summary

- Change: stitch-white-1to1
- Date: 2026-07-07

## Brainstorming 方案分析（7 个关键设计问题）

### Q1: 1:1 还原的"源真理"？
- **A: Stitch HTML**（含精确间距/字号/色值/布局结构）✅ 选用
- B: Stitch 截图（仅像素，无法精确还原）
- C: PRD 文字描述（prior 路径，已证有偏差）
- **理由**: HTML 是唯一能精确还原的源；截图作辅助验证

### Q2: 执行切片策略？
- **A: 按页（每子代理 1 页双端）** ✅ 选用
- B: 按平台（先全 Android 再全 iOS）— 跨端不一致风险高
- C: 按层（组件→页面→校准）— 跨端协调难
- **理由**: 同页双端同子代理保证一致性；38 屏幕重写需并行

### Q3: L1 四页处理方式？
- **A: 校准为任务级（不开新 spec）** ✅ 选用
- B: 修改 prior 的 delta spec — prior 未归档，冲突
- C: 重新创建 L1 capability — 重复
- **理由**: spec 级需求未变（"匹配 Stitch 设计"），变的是实现精度

### Q4: iOS 缺失 Feature（Sentry/CurrentCharge/Mileage）处理？
- **A: 就地新建 Feature 目录** ✅ 选用
- B: 跳过该页 — 双端不对等
- C: 占位 stub — 不达标
- **理由**: Android 已有对应页，iOS 需补齐才能 1:1

### Q5: 验证策略（无 JDK/Xcode 环境）？
- **A: 文件结构 + 视觉 checklist + Stitch 截图对照** ✅ 选用
- B: 仅文件存在性 — 太弱
- C: 等 Mac/JDK 再验证 — 阻塞交付
- **理由**: 与 prior change 一致策略，能捕获大部分偏差

### Q6: 里程钻取 103894px 超长 HTML 处理？
- **A: 分段读取 + 优先年→月→日骨架** ✅ 选用
- B: 跳过细节 — 损失 1:1
- C: 单次全量读取 — 子代理上下文爆炸
- **理由**: 骨架是核心，细节曲线可降级

### Q7: 数据层契约？
- **A: 不改 ViewModel/Repository，UI 即插即用** ✅ 选用
- B: 为 1:1 新增 ViewModel 字段 — 越界，破坏数据层稳定
- **理由**: 数据层成熟，UI 1:1 不应触发数据层改动

## 确认的技术方案

19 页 1:1 还原到 app_glm 双端。源真理 = Stitch HTML（via `mcp__stitch__get_screen`）。复用 prior 的 `shared/design-tokens.json` + Stitch 组件。执行 = /child-claude 按页派发子代理，每子代理读 1 页 HTML + 改 Android Composable + 改 iOS SwiftUI + 接导航。L1 校准走任务级。iOS 缺失 Feature 就地建。验证 = 文件结构 + 视觉 checklist + 截图对照。

## 关键取舍与风险

- **取舍**: 1:1 精度 vs 数据层稳定 → 数据层优先，UI 文案以现有字段为准，Stitch 文案作 label
- **取舍**: 并行速度 vs 子代理上下文 → 按页切片平衡两者；里程钻取单独分段
- **风险**: 里程钻取超长 HTML → 分段读取 + 骨架优先
- **风险**: Stitch 多变体选错 → 已选最全版，子代理开工前核对 title/height
- **风险**: 无编译验证 → 文件结构 + checklist + 截图对照；真实编译留待 Mac/JDK
- **风险**: prior 未归档 capability 重叠 → L1 走任务级不碰 prior delta spec

## 测试策略

- **不能做**: Android Gradle 编译（无 JDK）、iOS Xcode 编译（无 Mac）
- **能做且必做**:
  1. 文件存在性：19 页 Android Composable + 19 页 iOS SwiftUI View 均非空
  2. 视觉 checklist：无阴影/elevation、卡 1px #E5E5E5 边框、8px 圆角、JetBrains Mono 数字 tabular-nums、Inter 文字
  3. Stitch 截图对照：关键页 Android+iOS 渲染肉眼比对
  4. 数据层未改核实：grep 确认无 ViewModel/Repository/Dao 改动
  5. 导航链路：所有跳转可达无断链
- **未来**: Mac/JDK 环境就绪后补真实编译 + 预览截图

## Spec Patch

无。open 阶段 4 个 delta spec 已含完整 1:1 Requirement + WHEN/THEN Scenario，无需补充。
