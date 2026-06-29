## Context

Mimo 是基于 TeslaMate 的移动端 App。TeslaMate 是自托管的 Tesla 数据记录器（Elixir），数据存入 PostgreSQL，通过 MQTT 发布实时状态。TeslaMateApi（Go）提供 RESTful JSON API 供移动端调用。

**当前状态**：无移动端 App，用户只能通过浏览器访问 Grafana 仪表盘。

**约束**：
- 后端不自建，完全依赖用户自托管的 TeslaMateApi v1.21+
- 单人开发，原生双端（Kotlin + Swift）
- 主攻中国市场，需高德地图 + 中文 UI
- 品牌名：**MateLink**
- 商业模式：前期免费，后期可能收费多功能
- 视觉风格：Apple-Like + 基于车色的强调色

**参考仓库**（已克隆到 `docs/git_ref/mimo/`）：
- matedroid (★67, Kotlin Android) — 完整功能参考
- t-buddy (★57, Swift iOS) — SwiftUI 架构参考
- teslamateapi (★231, Go) — API 端点参考
- teslamate-chinese-dashboards (★109) — 中国本地化参考

## Goals / Non-Goals

**Goals:**
- Android + iOS 双端原生 App，可上架 App Store & Google Play
- 实时 Dashboard（电量/状态/位置/胎压/2D 车辆图）
- 充电/驾驶历史列表 + 详情（含地图、曲线图）
- 电池健康趋势
- 中国本地化（高德地图、分时电价、中文 UI）
- Mock 模式 + 离线缓存
- 暗色/亮色主题

**Non-Goals:**
- 不做车端命令（wake/lock/charge）— v2.0
- 不做 3D 车辆展示 — v1.2
- 不做 Apple Watch — v1.2
- 不做推送通知 — v1.1
- 不做桌面 Widget — v1.1
- 不自建后端，不修改 TeslaMate 源码
- 不存储 Tesla 账号密码

## Decisions

### D1: 技术栈 — 原生双端（Kotlin + Swift）

**选择**：Android 用 Kotlin + Jetpack Compose，iOS 用 Swift + SwiftUI

**理由**：
- matedroid (Kotlin) 和 t-buddy (Swift) 已验证可行，有完整参考代码
- 原生性能最佳（列表滚动、图表渲染、地图交互）
- Widget/Watch/Live Activities 直接写，无需桥接
- 包体积小（~15-20MB vs RN 的 40-50MB）

**备选**：React Native + Expo（跨平台一套代码，但性能差、Widget/Watch 仍需原生）

### D2: API 层 — HTTP 轮询（非 MQTT）

**选择**：P0 用 HTTP 轮询 TeslaMateApi `/status` 端点，5s 间隔

**理由**：
- TeslaMateApi 已提供 `/status` 端点（MQTT 缓存），无需直连 MQTT Broker
- 移动端 MQTT 实战坑多（公网暴露、WebSocket 桥接、断线重连）
- 5s 轮询对数据展示类 App 足够

**备选**：MQTT 直连（实时性更好，复杂度高，留作 P2 优化项）

### D3: 地图方案 — 高德地图（中国）+ 系统地图（海外）

**选择**：
- `zh-CN` locale → 高德地图 SDK + GCJ-02 纠偏
- 其他 locale → Apple Maps (iOS) / Google Maps (Android)

**理由**：
- OpenStreetMap 国内数据稀疏，基本不可用
- 高德地图个人开发者免费
- GCJ-02 纠偏算法参考 teslamate-chinese-dashboards

### D4: 数据缓存 — 离线缓存 30 天

**选择**：
- Android: Room 数据库
- iOS: SwiftData
- 缓存最近 30 天的 drives/charges 列表
- TTL 24h，超时仍显示但标记 "stale"

**理由**：
- 地铁/飞机上可查看历史
- 减少 API 调用
- 原生数据库方案性能最佳

### D5: 2D 车辆图（非 3D）

**选择**：MVP 用 2D 车辆图 + 颜色匹配

**理由**：
- 3D 需要 GLTF 模型资源（5 种车型 × 多种颜色），开发成本高
- matedroid 用 2D 图照样上架成功
- 3D 留作 v1.2 的差异化功能

### D6: 视觉风格 — Apple-Like + 车色强调色

**选择**：大圆角卡片（16-20pt）、毛玻璃背景、系统字体、浅色/深色主题跟随系统、配色克制（黑/白/灰 + 1 个基于车色的强调色）

**理由**：
- 大众用户接受度高，不像赛博朋克那么小众
- 参考 Tesla_Clone_Swiftui 的 SwiftUI 风格
- 车色强调色增加个性化（如蓝色 Model 3 用蓝色主题）

### D7: 品牌名 — MateLink

**选择**：MateLink

**理由**：
- 不直接用 "Tesla" 作主名（商标风险），但保留辨识度
- "MateLink" 表达"连接 TeslaMate 数据"的含义
- App Store 描述必须包含 "Not affiliated with Tesla, Inc."

### D8: 分时电价计算

**选择**：客户端计算，用户可配置峰平谷时段和电价

**理由**：
- 不需要后端支持
- 参考 teslamate-chinese-dashboards 的 SQL 逻辑，移植到客户端
- 默认值覆盖大部分中国城市

## Risks / Trade-offs

| 风险 | 严重度 | 缓解方案 |
|------|--------|----------|
| TeslaMateApi 接口变更 | 🟡 中 | 锁定 v1.21+，字段可选化，版本检测 |
| 高德地图 SDK 集成复杂 | 🟡 中 | 用官方 SDK，备选 WebView 嵌入 |
| 单人双端开发周期长 | 🟡 中 | 先做 Android（有 matedroid 完整参考），iOS 后做 |
| App Store 审核被拒（车辆类严审） | 🔴 高 | 明确免责 + 强调 "requires self-hosted" |
| Tesla 商标侵权 | 🔴 高 | 不用 Tesla 作主名/图标，描述含免责声明 |
| 用户不知如何部署 TeslaMate | 🟡 中 | 写中文部署教程 |

## Open Questions

- 3D 车辆模型来源（v1.2 需要）— 建议 Sketchfab 采购或社区贡献
- Apple Developer 账号是否已注册
- Google Play 开发者账号是否已注册
