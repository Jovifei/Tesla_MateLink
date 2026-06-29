# Mimo 审议 GLM PRD 报告

> **审议人**：Mimo (Claude)
> **审议对象**：GLM PRD 全套 10 份文档
> **日期**：2026-06-22

---

## 总体评价

GLM 的 PRD 质量**很高**，10 份文档覆盖了产品、技术、风险、计划全链路，功能编号体系（F-001 ~ F-308）和用户流程设计都很专业。以下逐项说明**同意采纳**和**不同意反驳**的内容。

---

## 一、同意采纳（将更新到 Mimo PRD）

### 1.1 Mock 模式（F-014）⭐ 强烈同意

GLM 提出的 Mock 模式是一个**极好的设计**：
- 内置虚拟数据，无需 TeslaMate 实例即可预览 UI
- 开发期 UI 调试、App Store 截图、演示评估三合一
- 借鉴来源：`teslamate-modern-dashboard` 的 Mock 数据方案

**采纳理由**：这直接解决了"用户没有 TeslaMate 就没法体验 App"的痛点，大幅降低用户尝试门槛。

### 1.2 功能编号体系

GLM 的 F-001 ~ F-308 编号体系比 Mimo 的文字描述更精确：
- 每个功能有唯一 ID，便于 Issue 追踪、commit 引用
- P0/P1/P2/P3 分级 + 版本映射清晰

**采纳**：后续开发用 GLM 的编号体系。

### 1.3 用户流程图（6 个 Flow）

GLM 设计了 6 个核心用户流程：
- Flow A：首次启动 → Dashboard
- Flow B：查看行程详情
- Flow C：切换车辆
- Flow D：充电中查看进度
- Flow E：离线查看历史
- Flow F：启用 Mock 模式

**采纳理由**：Mimo PRD 缺少用户流程图，这是明显短板。

### 1.4 离线缓存策略（F-015）

GLM 的离线方案比 Mimo 更具体：
- MMKV 缓存最近 30 天数据
- TTL 24h，超时仍显示但标记 "stale"
- 离线时顶部黄色 banner 提示

**采纳**。

### 1.5 状态徽章颜色规范

| 状态 | 颜色 |
|------|------|
| online | 绿色 |
| driving | 蓝色 |
| charging | 橙色 |
| asleep | 灰色 |
| offline | 深灰 |

**采纳**：Mimo PRD 缺少这个细节。

### 1.6 法律免责条款

GLM 明确了 App Store / Google Play 描述必须包含的免责文字：
- "Not affiliated with Tesla, Inc."
- "Requires self-hosted TeslaMate instance"
- "Your data stays on your server"

**采纳**：这是上架审核的关键。

### 1.7 无障碍（Accessibility）要求

- 支持 Dynamic Type / Font Scale
- WCAG AA 对比度标准
- VoiceOver / TalkBack label
- 触摸目标 ≥ 44pt/48dp
- 支持"减少动效"开关

**采纳**：Mimo PRD 完全没提无障碍。

### 1.8 空状态 / 加载状态 / 错误状态规范

GLM 对每种状态都有明确定义：
- 空状态：插画 + 引导文案
- 加载状态：骨架屏
- 错误状态：Toast + Retry / 全屏错误页

**采纳**。

---

## 二、部分同意（需调整后采纳）

### 2.1 品牌命名策略

**GLM 提议**：独立品牌名 + "for TeslaMate" 副标题

**同意部分**：
- 不用 "Tesla" 作主名 ✅（商标风险）
- 用 "for TeslaMate" 副标题 ✅

**不同意部分**：
- GLM 的 5 个候选名（Tessera / VoltLens / ChargeQ / MateLink / TeslaPulse）都太"技术化"，缺乏记忆点
- **建议**：品牌名决策延后，先用项目代号 "Mimo" 开发，上架前再定

### 2.2 Freemium 商业模式

**GLM 提议**：$9.99 一次性 PRO，对标 Mytess

**同意部分**：
- 核心数据展示免费 ✅（隐私用户最看重的）
- 不做订阅 ✅（用户讨厌订阅）

**不同意部分**：
- Widget 锁定为 PRO 功能 ❌ — Widget 是基础体验，不应付费墙挡
- 中国本地化（高德地图）锁 PRO ❌ — 中国用户是核心目标群，锁了等于自废武功
- **建议**：PRO 锁高级分析（AI 查询、数据导出、多实例），基础体验功能全免费

### 2.3 通知推送方案

**GLM 提议**：需要用户自行配置 APNs/FCM

**问题**：这对普通用户太复杂了。

**建议调整**：
- MVP 用 MQTT over WebSocket 直连（App 前台时实时推送）
- 后台通知用轻量中继服务（可选部署），或直接用 TeslaMate 的 webhook + ntfy.sh / Gotify 等开源推送方案
- 不自建 APNs/FCM 服务

---

## 三、不同意反驳

### 3.1 技术选型：React Native + Expo ❌ 强烈反对

**GLM 选型理由**：
1. `gwesseling/Tesla-app` 已验证 RN + Three.js 可行
2. Expo EAS Build 简化发布
3. 3D 生态 R3F 最强
4. 团队规模未知，跨平台保险

**反驳**：

| GLM 理由 | 反驳 |
|----------|------|
| gwesseling/Tesla-app 验证可行 | 该仓库 ★81，是 **UI 克隆**，不是生产级 App。从未上架，无真实用户验证。 |
| Expo 简化发布 | EAS Build 免费版每月 30 次构建，开发期根本不够。付费 $99/月。 |
| R3F 3D 生态最强 | 3D 车辆是**锦上添花**，不是核心功能。matedroid 用 2D 图照样 ★67 上架了。 |
| 跨平台保险 | RN 的 Widget、Live Activities、Watch App 全都要写原生模块，"跨平台"名存实亡。 |

**更严重的问题**：

1. **性能天花板**：RN 的 JS Bridge 在列表滚动、图表渲染、地图交互上有明显瓶颈。matedroid (Kotlin) 和 t-buddy (Swift) 的流畅度 RN 做不到。
2. **包体积**：RN + Expo + Three.js + 地图 SDK，轻松超 50MB。原生方案可以压到 15MB。
3. **调试噩梦**：RN 跨平台 bug + 原生模块 bug 叠加，调试成本远高于原生。
4. **生态碎片化**：RN 0.76 新架构（Fabric/TurboModules）与大量社区库不兼容，踩坑概率极高。

**Mimo 方案**：

| 平台 | 技术栈 | 理由 |
|------|--------|------|
| Android | Kotlin + Jetpack Compose | matedroid 已验证，615 个文件，已上架 Play Store + F-Droid |
| iOS | Swift + SwiftUI | t-buddy 已验证，原生体验，Widget/Live Activities 一把梭 |
| 共享 | API Client 逻辑用 Kotlin Multiplatform (KMP) 共享 | 只共享数据层，UI 各自原生 |

**工作量对比**：
- GLM 方案：1 套 RN 代码 + 大量原生桥接 ≈ 1.5x 工作量
- Mimo 方案：2 套原生代码 + 1 套共享 API ≈ 1.8x 工作量
- **但**：原生方案质量远高于 RN，长期维护成本更低

### 3.2 3D 车辆作为 P0 功能 ❌ 反对

**GLM 提议**：F-004 3D 车辆展示为 P0（MVP 必做）

**反驳**：
- 3D 车辆是**视觉锦上添花**，不是数据展示 App 的核心价值
- 需要 GLTF 模型资源（5 种车型 × 多种颜色 × 多种轮毂 = 几十个模型文件）
- 低端机降级为 2D 后，3D 开发投入就浪费了
- matedroid 用 2D 车辆图照样获得 ★67 + 上架 Play Store

**建议**：3D 车辆降为 P2（v1.2），MVP 用 2D 车辆图 + 颜色匹配。

### 3.3 项目计划 24 周太长 ❌

**GLM 计划**：7 个里程碑，24 周（6 个月）

**反驳**：
- 这是一个**数据展示 App**，不是社交/电商/IM，功能边界清晰
- matedroid 一个人开发，从 2025-12 到现在（6 个月）已经功能完整
- 24 周的计划容易导致"完美主义拖延"

**Mimo 计划**：
- Phase 1 MVP：6 周（Dashboard + 充放电历史 + 设置）
- Phase 2 完善：6 周（电池健康 + Widget + 通知）
- Phase 3 差异化：持续
- **总计 12 周上架**，比 GLM 快一倍

### 3.4 PostHog 自托管分析 ❌ 过度设计

**GLM 提议**：自托管 PostHog 做用户行为分析

**反驳**：
- MVP 阶段用户量 < 1000，PostHog 自托管是杀鸡用牛刀
- 自托管需要服务器 + 维护成本
- 用户隐私敏感型产品，埋点本身就敏感

**建议**：
- MVP 用 Sentry（崩溃监控）+ 简单的 App Store Connect / Google Play Console 内置分析
- 用户量过万后再考虑 PostHog / Mixpanel

### 3.5 Apple Watch 作为 P2 ❌ 过早

**GLM 提议**：F-201 Apple Watch App 为 P2（v1.2）

**反驳**：
- Watch App 开发成本高（独立 watchOS 项目）
- 使用场景有限（看电量？看续航？）
- t-buddy 和 mytess 都没有 Watch App
- 开发时间应该投入到核心功能打磨

**建议**：Apple Watch 降为 P3（远期），根据用户反馈决定。

### 3.6 风险评估 30+ 条过度分析 ❌

**GLM** 列了 30+ 风险点，分 6 大类。

**反驳**：
- 大部分风险是"可能发生但概率极低"的理论风险
- "TeslaMate 上游停止维护"、"Apple Developer 账号被封" — 这些是所有 App 的通用风险，不需要特别列出
- 30 条风险反而分散注意力，抓不住重点

**Mimo 方案**：聚焦 5 个**真正可能发生的**风险即可。

---

## 四、采纳清单汇总

| 来源 | 内容 | 动作 |
|------|------|------|
| GLM F-014 | Mock 模式 | ✅ 采纳，加入 Mimo PRD |
| GLM | 功能编号体系 (F-xxx) | ✅ 采纳 |
| GLM | 6 个用户流程图 | ✅ 采纳 |
| GLM F-015 | 离线缓存策略 | ✅ 采纳 |
| GLM | 状态徽章颜色 | ✅ 采纳 |
| GLM | 法律免责条款 | ✅ 采纳 |
| GLM | 无障碍要求 | ✅ 采纳 |
| GLM | 空/加载/错误状态规范 | ✅ 采纳 |
| GLM | 品牌命名策略 | ⚠️ 部分采纳（延后决策） |
| GLM | Freemium 模式 | ⚠️ 部分采纳（调整 PRO 范围） |
| GLM | 通知推送方案 | ⚠️ 部分采纳（简化实现） |
| GLM glm_04 | React Native + Expo | ❌ 反对，用原生双端 |
| GLM F-004 | 3D 车辆 P0 | ❌ 反对，降为 P2 |
| GLM glm_07 | 24 周计划 | ❌ 反对，12 周上架 |
| GLM | PostHog 自托管 | ❌ 反对，用 Sentry + 平台内置 |
| GLM F-201 | Apple Watch P2 | ❌ 反对，降为 P3 |
| GLM glm_08 | 30+ 风险 | ❌ 反对，精简为 5 个核心风险 |

---

## 五、GLM 反驳 Mimo 的回应（第二轮）

| # | GLM 反驳 | Mimo 回应 | 状态 |
|---|----------|-----------|------|
| 1 | 单人团队原生双端不可行 | 承认取决于团队规模。单人→RN更现实，2+人→原生质量更高。技术方案改为双方案待Jovi决策 | ✅ 已修正 |
| 2 | MQTT 不该是 P0 | 同意。P0 用 HTTP 轮询，MQTT 降为 P2 | ✅ 已修正 |
| 3 | 包体积 15MB 不现实 | 同意。改为 ≤50MB (iOS) / ≤40MB (Android) | ✅ 已修正 |
| 4 | iOS 最低版本 16.0 偏低 | 部分同意。改为 16.0 最低 / 17+ 推荐 | ✅ 已修正 |
| 5 | TeslaMateApi v2.0 不存在 | **承认错误**。改为 v1.21+，已核实源码 | ✅ 已修正 |
| 6 | 5 个虚构 API 端点 | **承认错误**。已用源码核实的 13 个真实端点替换 | ✅ 已修正 |
| 7 | Car JSON 的 eid/vid 不暴露 | **承认错误后又被反驳**。GLM 指出 eid/vid 确实在 API 中暴露。已修正数据模型 | ✅ 已修正 |
| 8 | 5 个风险遗漏合规风险 | 同意。已补充 Tesla 商标 / AGPL / GDPR / PIPL / App Store 审核 / 高德合规 | ✅ 已修正 |

---

## 六、第三轮审议结果（v1.2 → v1.3）

### GLM 反驳的 6 项全部接受

| # | GLM 反驳 | 修正动作 |
|---|----------|----------|
| 1 | MQTT 自相矛盾（§2.2 轮询 vs §3.2.1 MQTT） | Dashboard 数据来源全部改为 HTTP 轮询 |
| 2 | 电池健康应为 P0 | 从 P1 提升到 P0（MVP 必做） |
| 3 | Live Activity 复杂度高 | 不在当前 PRD 中，留给 v2.0 |
| 4 | Widget 1 周不现实 | 从 Phase 2 移除单独 Widget 周，合并到统计概览 |
| 5 | Mock 模式缺用户流程 | 新增 Flow E：启用 Mock 模式 |
| 6 | 非功能需求仅 5 项 | 新增资源消耗预算（内存/电量/流量/磁盘） |

### Jovi 决策已落实
- 技术方案：**原生**（Kotlin + Swift + watchOS）
- 商业模式：全免费
- Apple Watch：P2（v1.2）
- 中国本地化：P0（MVP）

---

## 七、下一步

1. ✅ 已将采纳内容更新到 `mimo_PRD.md`（v1.3）
2. ✅ 已修正 GLM 指出的所有错误
3. ✅ 已落实 Jovi 的所有决策
4. ⏭ 开始 Phase 1 开发（原生方案）
