# Comet Design Handoff

- Change: mimo-mvp
- Phase: design
- Mode: compact
- Context hash: 1e0e9d0d971e9c22a1c1395b99268efee9c0f299fb696ce610b04d5259cb8432

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/mimo-mvp/proposal.md

- Source: openspec/changes/mimo-mvp/proposal.md
- Lines: 1-43
- SHA256: 3a2d022d1ef6d458188489d087529550f815ece5e9e196df7ab278ba5b8d8b0a

```md
## Why

TeslaMate 是一个优秀的自托管 Tesla 数据记录器，但没有官方移动端 App。车主只能通过浏览器访问 Grafana 仪表盘，手机体验极差。中国用户还面临地图不可用（OpenStreetMap 国内数据稀疏）、无中文界面等问题。

本 change（mimo-mvp）构建 **Tesla_MateLink** App 的第一个可上架版本，让 TeslaMate 用户在手机上以原生体验查看车辆数据。

**品牌名**：Tesla_MateLink
**商业模式**：前期免费，后期可能收费多功能
**视觉风格**：Apple-Like + 基于车色的强调色

## What Changes

- **新建 Android App**（Kotlin + Jetpack Compose）：Dashboard、充电/驾驶历史、电池健康、设置
- **新建 iOS App**（Swift + SwiftUI）：同上功能，共享 API 层设计
- **中国本地化**：高德地图（GCJ-02 纠偏）、分时电价、中文 UI
- **Mock 模式**：内置虚拟数据，无需 TeslaMate 实例即可预览
- **离线缓存**：最近 30 天数据可离线查看
- **后端依赖**：用户自托管 TeslaMateApi v1.21+（不自建后端）

## Capabilities

### New Capabilities

- `dashboard`: 实时车辆状态展示（电量、续航、位置、胎压、2D 车辆图）
- `charge-history`: 充电记录列表 + 详情（功率曲线、地图、费用）
- `drive-history`: 驾驶记录列表 + 详情（轨迹地图、速度/功率曲线）
- `battery-health`: 电池衰减趋势、容量对比、健康度百分比
- `china-localization`: 高德地图集成、GCJ-02 坐标纠偏、分时电价计算、中文 UI
- `mock-mode`: 内置虚拟数据模式，支持 UI 预览和截图
- `offline-cache`: 离线数据缓存，弱网/无网可查看历史
- `settings`: 服务器配置、主题切换单位偏好、多车切换
- `app-foundation`: 项目骨架、导航、主题、API Client、错误处理

### Modified Capabilities

（无，这是全新项目）

## Impact

- **新建代码库**：`E:\project\tesla_master\app_mimo\`（Android + iOS 双端）
- **外部依赖**：TeslaMateApi v1.21+（Docker 部署）、高德地图 SDK
- **上架目标**：App Store + Google Play
- **参考仓库**：matedroid (Android)、t-buddy (iOS)、teslamateapi (API)、teslamate-chinese-dashboards (中国本地化)
```

## openspec/changes/mimo-mvp/design.md

- Source: openspec/changes/mimo-mvp/design.md
- Lines: 1-141
- SHA256: 44c8ab6d4c04ad861010063196717d74fee58c3faf30233a1e657df1e375ac7c

[TRUNCATED]

```md
## Context

Mimo 是基于 TeslaMate 的移动端 App。TeslaMate 是自托管的 Tesla 数据记录器（Elixir），数据存入 PostgreSQL，通过 MQTT 发布实时状态。TeslaMateApi（Go）提供 RESTful JSON API 供移动端调用。

**当前状态**：无移动端 App，用户只能通过浏览器访问 Grafana 仪表盘。

**约束**：
- 后端不自建，完全依赖用户自托管的 TeslaMateApi v1.21+
- 单人开发，原生双端（Kotlin + Swift）
- 主攻中国市场，需高德地图 + 中文 UI
- 品牌名：**Tesla_MateLink**
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
```

Full source: openspec/changes/mimo-mvp/design.md

## openspec/changes/mimo-mvp/tasks.md

- Source: openspec/changes/mimo-mvp/tasks.md
- Lines: 1-105
- SHA256: aaf13964d4b7f5ce38b874f2eba28d505efced366b22f655df688ddf48fa2a6e

[TRUNCATED]

```md
## 1. Web 交互原型（Phase 0）

- [ ] 1.1 初始化 React + Vite + Tailwind 项目到 `app_mimo/web-prototype/`
- [ ] 1.2 配置路由（React Router）+ 状态管理（Zustand）
- [ ] 1.3 创建 Mock 数据（1 辆虚拟车 + 30 天历史）
- [ ] 1.4 实现 Dashboard 页面（电量、状态徽章、2D 车辆图、信息卡片、5s 自动刷新）
- [ ] 1.5 实现充电列表页（分页、AC/DC 筛选）
- [ ] 1.6 实现充电详情页（地图 + Recharts 曲线图 + 统计卡片）
- [ ] 1.7 实现驾驶列表页（分页）
- [ ] 1.8 实现驾驶详情页（路线地图 + 曲线图 + 统计卡片）
- [ ] 1.9 实现电池健康页（健康度 + 衰减趋势图）
- [ ] 1.10 实现设置页（表单交互、Mock 开关）
- [ ] 1.11 实现首次配置页（欢迎页、URL/Token 输入、测试连接）
- [ ] 1.12 Jovi 确认所有页面交互

## 2. iOS App 初始化（Phase 1）

- [ ] 2.1 创建 iOS 项目（Swift + SwiftUI）到 `app_mimo/ios/`
- [ ] 2.2 配置 Swift Package Manager、依赖（Swift Charts、MapKit）
- [ ] 2.3 搭建 TeslaMateApi Docker 测试环境

## 2. API 层

- [ ] 2.1 Android：实现 API Client（Retrofit + OkHttp），封装 `/cars`、`/status`、`/charges`、`/drives`、`/battery-health`、`/updates` 端点
- [ ] 2.2 iOS：实现 API Client（URLSession + async/await），同上端点
- [ ] 2.3 定义数据模型（Car、CarStatus、Charge、Drive、BatteryHealth）基于 teslamateapi 真实嵌套结构
- [ ] 2.4 实现错误处理（网络不可达、401、超时、TLS 证书）
- [ ] 2.5 实现 Token 安全存储（Android Keystore / iOS Keychain）

## 3. 导航与主题

- [ ] 3.1 Android：实现底部 Tab 导航（Dashboard、Drives、Charges、More）
- [ ] 3.2 iOS：实现 TabView 底部导航
- [ ] 3.3 实现浅色/深色主题系统（跟随系统 + 手动切换）
- [ ] 3.4 定义 Apple-Like 设计规范（圆角、间距、字体、车色强调色）

## 4. 首次配置（Onboarding）

- [ ] 4.1 实现欢迎页 + "连接 TeslaMate" 按钮
- [ ] 4.2 实现服务器配置页（URL + Token 输入）
- [ ] 4.3 实现"测试连接"功能（调用 `/api/ping` → `/api/v1/cars` 三步检测）
- [ ] 4.4 连接成功 → 显示车辆数 → 进入 Dashboard

## 5. Dashboard

- [ ] 5.1 实现 Dashboard 页面布局（车辆图 + 状态卡片 + 信息网格）
- [ ] 5.2 实现电量/续航显示（大数字 + 进度条）
- [ ] 5.3 实现车辆状态徽章（online/driving/charging/asleep/offline + 颜色）
- [ ] 5.4 实现 2D 车辆图（按 exterior_color 匹配颜色）
- [ ] 5.5 实现信息卡片（位置、里程、温度、胎压）
- [ ] 5.6 实现 5s 自动轮询刷新
- [ ] 5.7 实现下拉刷新

## 6. 充电历史

- [ ] 6.1 实现充电列表页（分页加载，每页 20 条）
- [ ] 6.2 实现充电列表项（日期、时长、电量、费用、AC/DC 标识）
- [ ] 6.3 实现充电详情页（地图 + 功率/电压/温度曲线 + 统计卡片）
- [ ] 6.4 实现 AC/DC 筛选

## 7. 驾驶历史

- [ ] 7.1 实现驾驶列表页（分页加载，每页 20 条）
- [ ] 7.2 实现驾驶列表项（日期、距离、时长、效率、起止地址）
- [ ] 7.3 实现驾驶详情页（路线地图 + 速度/功率曲线 + 统计卡片）
- [ ] 7.4 实现轨迹点抽稀算法（Douglas-Peucker）
- [ ] 7.5 实现图表标签切换（速度/功率/海拔）

## 8. 电池健康

- [ ] 8.1 实现电池健康页（健康度百分比 + 容量对比 + 衰减信息）
- [ ] 8.2 实现衰减趋势折线图

## 9. More 页面与设置

- [ ] 9.1 实现 More 页面（电池健康入口、软件更新入口、设置入口）
- [ ] 9.2 实现设置页（服务器配置、单位、主题、语言、Mock 开关）
- [ ] 9.3 实现多车切换功能（Modal 选择器）
- [ ] 9.4 实现软件更新历史列表

```

Full source: openspec/changes/mimo-mvp/tasks.md

## openspec/changes/mimo-mvp/specs/app-foundation/spec.md

- Source: openspec/changes/mimo-mvp/specs/app-foundation/spec.md
- Lines: 1-30
- SHA256: bb6ffaca542afb926a947dabcf5a5728a187a796ccfbbecaaeed14332fad1bf2

```md
## ADDED Requirements

### Requirement: Project skeleton with native navigation
The system SHALL provide a native app skeleton for both Android (Kotlin + Jetpack Compose) and iOS (Swift + SwiftUI) with bottom tab navigation containing 4 tabs: Dashboard, Drives, Charges, More.

#### Scenario: App launches with navigation
- **WHEN** user opens the app
- **THEN** app displays bottom tab bar with 4 tabs and defaults to Dashboard tab

### Requirement: API Client for TeslaMateApi
The system SHALL provide an HTTP API client that communicates with TeslaMateApi v1.21+ endpoints, supporting authentication via Bearer token and automatic JSON parsing.

#### Scenario: Successful API call
- **WHEN** app calls `/api/v1/cars` with valid token
- **THEN** app receives and parses car list JSON response

#### Scenario: API error handling
- **WHEN** API returns non-2xx status code
- **THEN** app displays appropriate error message (network error / 401 / timeout)

### Requirement: Theme support
The system SHALL support light and dark themes, following system preference by default with manual override option.

#### Scenario: System theme change
- **WHEN** user changes system theme from light to dark
- **THEN** app automatically switches to dark theme

#### Scenario: Manual theme override
- **WHEN** user selects "Dark" in settings
- **THEN** app uses dark theme regardless of system setting
```

## openspec/changes/mimo-mvp/specs/battery-health/spec.md

- Source: openspec/changes/mimo-mvp/specs/battery-health/spec.md
- Lines: 1-15
- SHA256: 12eef1749eb3ff45fce9145b8957ec9f0a08943ff0e3fa30e8045f99975830f8

```md
## ADDED Requirements

### Requirement: Battery health overview
The system SHALL display battery health percentage, current capacity vs original capacity, range loss, and total mileage.

#### Scenario: View battery health
- **WHEN** user navigates to Battery Health from More tab
- **THEN** app shows health %, capacity comparison, and degradation info

### Requirement: Battery degradation chart
The system SHALL display a line chart showing battery capacity degradation over time.

#### Scenario: View degradation trend
- **WHEN** user views Battery Health page
- **THEN** app shows line chart with capacity (Y) vs time (X)
```

## openspec/changes/mimo-mvp/specs/charge-history/spec.md

- Source: openspec/changes/mimo-mvp/specs/charge-history/spec.md
- Lines: 1-26
- SHA256: 175b7b89c308934aaa5961c91a4c1721bf64a0d5e17238a154723ee787a8e363

```md
## ADDED Requirements

### Requirement: Charge history list
The system SHALL display a paginated list of charging sessions with date, duration, energy added, cost, charging type (AC/DC), and start/end battery level.

#### Scenario: View charge list
- **WHEN** user taps Charges tab
- **THEN** app displays recent charging sessions in reverse chronological order

#### Scenario: Load more charges
- **WHEN** user scrolls to bottom of list
- **THEN** app loads next page of 20 charging sessions

### Requirement: Charge detail view
The system SHALL display detailed charge information including location map, power/voltage/temperature curves over time, and cost breakdown.

#### Scenario: View charge detail
- **WHEN** user taps a charge session in the list
- **THEN** app shows detail page with map, charts, and statistics

### Requirement: Charge filtering
The system SHALL allow filtering charges by AC/DC type and date range.

#### Scenario: Filter by DC only
- **WHEN** user selects "DC" filter
- **THEN** list shows only DC fast charging sessions
```

## openspec/changes/mimo-mvp/specs/china-localization/spec.md

- Source: openspec/changes/mimo-mvp/specs/china-localization/spec.md
- Lines: 1-37
- SHA256: ccba71925b740df94e754e34845423de1b0307b221fca958b529f1eb56388171

```md
## ADDED Requirements

### Requirement: Amap integration for China
The system SHALL use Amap (高德地图) SDK for map display when locale is zh-CN, and system default maps otherwise.

#### Scenario: Chinese locale uses Amap
- **WHEN** device locale is zh-CN
- **THEN** all maps render using Amap SDK

#### Scenario: Non-Chinese locale uses system maps
- **WHEN** device locale is en-US
- **THEN** maps render using Apple Maps (iOS) or Google Maps (Android)

### Requirement: GCJ-02 coordinate correction
The system SHALL convert WGS-84 coordinates (from TeslaMate) to GCJ-02 for display on Amap, using standard offset algorithm.

#### Scenario: Coordinates align on Amap
- **WHEN** TeslaMate reports position at WGS-84 coordinates
- **THEN** marker appears at correct location on Amap (not offset)

### Requirement: Time-of-use tariff calculation
The system SHALL calculate charging cost based on configurable peak/flat/valley electricity rates and time periods.

#### Scenario: Default tariff rates
- **WHEN** user has not configured custom rates
- **THEN** app uses defaults: peak ¥1.0/kWh (10-15, 18-21), flat ¥0.7/kWh (7-10, 15-18, 21-23), valley ¥0.3/kWh (23-7)

#### Scenario: Custom tariff rates
- **WHEN** user configures custom peak rate to ¥1.2/kWh
- **THEN** all subsequent cost calculations use ¥1.2 for peak period

### Requirement: Chinese UI
The system SHALL display all UI text in Simplified Chinese when locale is zh-CN.

#### Scenario: Chinese text display
- **WHEN** device locale is zh-CN
- **THEN** all labels, buttons, and messages display in Chinese
```

## openspec/changes/mimo-mvp/specs/dashboard/spec.md

- Source: openspec/changes/mimo-mvp/specs/dashboard/spec.md
- Lines: 1-26
- SHA256: f67daf07169a15c47835e98927cf7f575bf237aa8c16a240e5404dfd9c31627d

```md
## ADDED Requirements

### Requirement: Real-time vehicle status display
The system SHALL display vehicle real-time status on the Dashboard, polling TeslaMateApi `/status` endpoint every 5 seconds. Data includes battery level, range, vehicle state, location, tire pressure, and temperature.

#### Scenario: Dashboard shows live data
- **WHEN** user opens Dashboard tab
- **THEN** app displays current battery %, range (km), vehicle state badge, location, tire pressure (4 wheels), and cabin temperature

#### Scenario: Data auto-refreshes
- **WHEN** Dashboard is visible for 5 seconds
- **THEN** app automatically fetches latest status from API

### Requirement: Vehicle state badge
The system SHALL display a colored badge indicating vehicle state: online (green), driving (blue), charging (orange), asleep (gray), offline (dark gray).

#### Scenario: Vehicle is charging
- **WHEN** vehicle state is "charging"
- **THEN** badge shows orange "Charging" text with current power (kW) and ETA

### Requirement: 2D vehicle image with color matching
The system SHALL display a 2D vehicle image matching the car's exterior color. Image changes based on `exterior_color` field from API.

#### Scenario: Car color matches
- **WHEN** car exterior_color is "SolidBlack"
- **THEN** dashboard shows black vehicle image
```

## openspec/changes/mimo-mvp/specs/drive-history/spec.md

- Source: openspec/changes/mimo-mvp/specs/drive-history/spec.md
- Lines: 1-26
- SHA256: 5cef982209d33fb1a8d9fcfb1dc28f04dfcbc0cec0545a605a1782127efa180a

```md
## ADDED Requirements

### Requirement: Drive history list
The system SHALL display a paginated list of drives with date, distance, duration, start/end addresses, and efficiency (Wh/km).

#### Scenario: View drive list
- **WHEN** user taps Drives tab
- **THEN** app displays recent drives in reverse chronological order

#### Scenario: Load more drives
- **WHEN** user scrolls to bottom of list
- **THEN** app loads next page of 20 drives

### Requirement: Drive detail with route map
The system SHALL display drive detail with full route on map (polyline), start/end markers, and speed/power/altitude curves.

#### Scenario: View drive route
- **WHEN** user taps a drive in the list
- **THEN** app shows detail page with route map, statistics, and switchable charts

### Requirement: Route simplification
The system SHALL apply route point simplification to avoid rendering thousands of points on mobile map, using Douglas-Peucker or similar algorithm.

#### Scenario: Long drive renders smoothly
- **WHEN** drive has 5000+ position points
- **THEN** map renders route without frame drops (≥ 30fps)
```

## openspec/changes/mimo-mvp/specs/mock-mode/spec.md

- Source: openspec/changes/mimo-mvp/specs/mock-mode/spec.md
- Lines: 1-19
- SHA256: fe7c04be7121f6cba031856554a1d1e89c6b615ab3ee224b14a0b63272ff1649

```md
## ADDED Requirements

### Requirement: Mock mode toggle
The system SHALL provide a "Mock Mode" toggle in Settings that switches the app to use built-in mock data instead of live API calls.

#### Scenario: Enable mock mode
- **WHEN** user enables Mock Mode in Settings
- **THEN** app displays confirmation dialog, then switches to mock data

#### Scenario: Mock mode indicator
- **WHEN** Mock Mode is active
- **THEN** app shows "Mock Mode" banner at top of Dashboard

### Requirement: Mock data content
The system SHALL include built-in mock data with 1 virtual vehicle and 30 days of history (drives, charges, battery health).

#### Scenario: Mock dashboard displays
- **WHEN** Mock Mode is enabled
- **THEN** Dashboard shows virtual car "Demo Car" with realistic status data
```

## openspec/changes/mimo-mvp/specs/offline-cache/spec.md

- Source: openspec/changes/mimo-mvp/specs/offline-cache/spec.md
- Lines: 1-22
- SHA256: f508ead34544a1e68b4501e910e20ab41052c3977d149e7cba4b8780fc388219

```md
## ADDED Requirements

### Requirement: Offline data caching
The system SHALL cache the last 30 days of drives and charges data locally for offline viewing.

#### Scenario: View cached data offline
- **WHEN** device has no network connection
- **THEN** app displays cached drives/charges list with "Offline" banner

### Requirement: Stale data indicator
The system SHALL mark cached data as "stale" after 24 hours TTL, while still displaying it.

#### Scenario: Stale data display
- **WHEN** cached data is older than 24 hours
- **THEN** app shows data with "Last updated: X hours ago" indicator

### Requirement: Offline dashboard
The system SHALL display last known vehicle status from cache when offline.

#### Scenario: Dashboard offline
- **WHEN** device is offline and user opens Dashboard
- **THEN** app shows "Offline - showing last known status" banner with cached data
```

## openspec/changes/mimo-mvp/specs/settings/spec.md

- Source: openspec/changes/mimo-mvp/specs/settings/spec.md
- Lines: 1-22
- SHA256: 70c9dac06fb854a2b42fb9f865039b9bc9bb31f78417625c5a0a040766ed2bc2

```md
## ADDED Requirements

### Requirement: Server configuration
The system SHALL allow users to configure TeslaMateApi server URL and API token, with a "Test Connection" button that validates connectivity.

#### Scenario: Configure server
- **WHEN** user enters server URL and token, taps "Test Connection"
- **THEN** app calls `/api/v1/cars` and shows success/failure result

### Requirement: Unit preferences
The system SHALL support km/mile and Celsius/Fahrenheit unit preferences.

#### Scenario: Switch to miles
- **WHEN** user selects "Miles" in settings
- **THEN** all distances display in miles, all speeds in mph

### Requirement: Multi-car switching
The system SHALL allow switching between multiple vehicles when the TeslaMate account has more than one car.

#### Scenario: Switch vehicle
- **WHEN** user taps vehicle name on Dashboard and selects another car
- **THEN** Dashboard refreshes with selected car's data
```

