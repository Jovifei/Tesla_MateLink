# TeslaMate 移动端 App 产品需求文档 (PRD)

> **项目代号**：Mimo
> **版本**：v1.1 Draft（整合 GLM PRD 审议结果）
> **日期**：2026-06-22
> **作者**：Jovi
> **审议记录**：[mimo_review_glm.md](./mimo_review_glm.md)

---

## 1. 产品概述

### 1.1 产品定位

Mimo 是一款基于 [TeslaMate](https://github.com/teslamate-org/teslamate) 开源项目的移动端 App，为 Tesla 车主提供**自托管**的车辆数据监控与分析服务。

**一句话描述**：把 TeslaMate 的数据装进口袋。

### 1.2 核心价值

| 价值点 | 说明 |
|--------|------|
| 数据主权 | 数据存在用户自己的服务器，不经过第三方云 |
| 深度分析 | 充放电效率、电池衰减、驾驶行为等 Tesla 官方 App 不提供的数据 |
| 移动便捷 | 随时随地查看车辆状态，不再依赖电脑打开 Grafana |
| 实时通知 | 充电完成、异常状态等推送提醒 |

### 1.3 目标用户

- **主要用户**：已部署 TeslaMate 的 Tesla 车主（技术爱好者、数据驱动型用户）
- **次要用户**：有 NAS/服务器、愿意部署 Docker 的 Tesla 车主
- **潜在用户**：对车辆数据分析感兴趣的普通 Tesla 车主

---

## 2. 系统架构

### 2.1 整体数据流

```
┌─────────────┐     ┌──────────────┐     ┌──────────────────┐
│  Tesla 车辆  │────▶│  TeslaMate   │────▶│   PostgreSQL     │
│             │     │  (Elixir)    │     │   + MQTT Broker  │
└─────────────┘     └──────────────┘     └────────┬─────────┘
                                                   │
                                          ┌────────▼─────────┐
                                          │  TeslaMateApi    │
                                          │  (Go REST API)   │
                                          └────────┬─────────┘
                                                   │
                                    ┌──────────────┼──────────────┐
                                    │              │              │
                              ┌─────▼─────┐  ┌────▼────┐  ┌─────▼─────┐
                              │  Android  │  │   iOS   │  │  Web PWA  │
                              │  (Kotlin) │  │ (Swift) │  │ (未来)    │
                              └───────────┘  └─────────┘  └───────────┘
```

### 2.2 技术选型

> **Jovi 决策：原生双端**（Kotlin + Swift）

| 层级 | 技术方案 | 理由 |
|------|----------|------|
| **后端 API** | Go (teslamateapi v1.21+) | 成熟开源方案，★231，Docker 一键部署 |
| **Android** | Kotlin + Jetpack Compose | 原生性能最佳，参考 matedroid 架构 |
| **iOS** | Swift + SwiftUI | 原生体验，Widget/Watch/Live Activities 直接写 |
| **Apple Watch** | watchOS 10+ + SwiftUI | WatchConnectivity 从 iPhone 推数据 |
| **数据缓存** | Room (Android) / SwiftData (iOS) | 离线可用，减少 API 调用 |
| **图表库** | MPAndroidChart (Android) / Swift Charts (iOS) | 原生图表渲染 |
| **实时数据** | P0 用 HTTP 轮询（5s 间隔） | 简单可靠，MQTT 作为 P2 优化项 |
| **后端依赖** | TeslaMateApi v1.21+ | **注意：v2.0 不存在，最新为 v1.21.x** |

### 2.3 API 依赖

> 以下端点基于 [teslamateapi 源码](https://github.com/tobiasehlert/teslamateapi/blob/main/src/webserver.go) 核实，版本 v1.21.x。

TeslaMateApi 真实端点（共 13 个）：

```
# 车辆信息
GET  /api/v1/cars                       # 车辆列表（含详情、外观、设置、统计）
GET  /api/v1/cars/{CarID}               # 单车信息
GET  /api/v1/cars/{CarID}/status        # 实时状态（MQTT 缓存）
GET  /api/v1/cars/{CarID}/battery-health # 电池健康

# 历史数据
GET  /api/v1/cars/{CarID}/charges       # 充电记录列表
GET  /api/v1/cars/{CarID}/charges/current # 当前充电
GET  /api/v1/cars/{CarID}/charges/{ChargeID} # 充电详情
GET  /api/v1/cars/{CarID}/drives        # 驾驶记录列表
GET  /api/v1/cars/{CarID}/drives/{DriveID} # 驾驶详情
GET  /api/v1/cars/{CarID}/updates       # 软件更新历史

# 命令（v2.0 规划）
POST /api/v1/cars/{CarID}/command/{Command} # 车端命令
POST /api/v1/cars/{CarID}/wake_up       # 唤醒

# 系统
GET  /api/v1/cars/{CarID}/logging       # 日志开关状态
PUT  /api/v1/cars/{CarID}/logging/{Command} # 切换日志
GET  /api/v1/globalsettings             # 全局设置

# 健康检查
GET  /api/ping                          # 连通性测试
GET  /api/healthz                       # 健康检查
GET  /api/readyz                        # 就绪检查
```

MQTT 实时主题：
```
teslamate/cars/{id}/state               # 车辆状态
teslamate/cars/{id}/charge_state        # 充电状态
teslamate/cars/{id}/battery_level       # 电池电量
teslamate/cars/{id}/latitude            # 纬度
teslamate/cars/{id}/longitude           # 经度
teslamate/cars/{id}/speed               # 速度
```

---

## 3. 功能需求

### 3.1 功能优先级矩阵

> **Jovi 决策**：全免费、主攻中国市场、Apple Watch 纳入开发。

| 优先级 | 功能模块 | MVP (v1.0) | v1.1 | v1.2 | v2.0+ |
|--------|----------|:---:|:---:|:---:|:---:|
| **P0** | 实时 Dashboard | ✅ | ✅ | ✅ | ✅ |
| **P0** | 2D 车辆图（按车色匹配） | ✅ | ✅ | ✅ | ✅ |
| **P0** | 充电历史 + 详情 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 驾驶历史 + 详情 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 电池健康 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 软件更新历史 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 多车切换 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 电池健康 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 浅色/深色主题 | ✅ | ✅ | ✅ | ✅ |
| **P0** | Mock 模式 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 离线缓存 | ✅ | ✅ | ✅ | ✅ |
| **P0** | 中国本地化（高德+分时电价+中文UI） | ✅ | ✅ | ✅ | ✅ |
| **P1** | 桌面 Widget | - | ✅ | ✅ | ✅ |
| **P1** | 推送通知 | - | ✅ | ✅ | ✅ |
| **P1** | 统计概览（钻取） | - | ✅ | ✅ | ✅ |
| **P1** | 多语言（中/英） | - | ✅ | ✅ | ✅ |
| **P2** | Apple Watch App | - | - | ✅ | ✅ |
| **P2** | 3D 车辆展示（Three.js） | - | - | ✅ | ✅ |
| **P2** | 活动热力图 | - | - | ✅ | ✅ |
| **P2** | Vampire Drain 分析 | - | - | ✅ | ✅ |
| **P2** | 年度报告（可分享） | - | - | ✅ | ✅ |
| **P2** | 数据导出（CSV/PDF） | - | - | ✅ | ✅ |
| **P3** | 车端命令（wake/lock/charge） | - | - | - | ✅ |
| **P3** | AI 自然语言查询 | - | - | - | ✅ |
| **P3** | 驾驶行为评分 | - | - | - | ✅ |
| **P3** | CarPlay / Android Auto | - | - | - | ✅ |

---

### 3.2 模块详细设计

#### 3.2.1 实时 Dashboard（P0）

**功能描述**：展示车辆当前状态的全局视图。

**数据项**：

| 数据 | 来源 | 刷新方式 |
|------|------|----------|
| 电池电量 (%) | API `/status` | HTTP 轮询 5s |
| 续航里程 (km) | API `/status` | HTTP 轮询 5s |
| 车辆状态 (在线/休眠/充电/驾驶) | API `/status` | HTTP 轮询 5s |
| 当前位置 | API `/status` | HTTP 轮询 5s |
| 车内温度 | API `/status` | HTTP 轮询 5s |
| 胎压 (4轮) | API `/status` | HTTP 轮询 5s |
| 最后更新时间 | API `/status` | HTTP 轮询 5s |

**UI 布局**：
```
┌─────────────────────────────────┐
│  Mimo            ⚙️  🔄        │  ← 顶栏：Logo + 设置 + 刷新
├─────────────────────────────────┤
│                                 │
│       ┌───────────────┐         │
│       │   🚗 3D车辆图  │         │  ← 车辆可视化（颜色匹配实车）
│       │               │         │
│       └───────────────┘         │
│                                 │
│  ┌─────────┐  ┌─────────┐      │
│  │ 🔋 78%  │  │ 📍 家   │      │  ← 电量卡片 + 位置卡片
│  │ 312 km  │  │ 在线中  │      │
│  └─────────┘  └─────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 🌡️ 车内 24°C          │      │  ← 环境信息
│  │ 胎压: 2.4 2.5 2.4 2.5 │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 📍 最近行程            │      │  ← 最近活动摘要
│  │ 今日: 45km · 效率142Wh/km│   │
│  └───────────────────────┘      │
│                                 │
├─────────────────────────────────┤
│  🏠    📊    🔋    🚗    ⚙️    │  ← 底部导航栏
│ 首页  统计  充电  行程  设置   │
└─────────────────────────────────┘
```

**参考实现**：
- matedroid：`app/src/main/java/.../dashboard/` — 3D 车辆图 + 实时数据卡片
- t-buddy：SwiftUI Dashboard 视图

---

#### 3.2.2 充电历史（P0）

**功能描述**：展示所有充电记录，支持筛选和详情查看。

**数据项**：
- 充电开始/结束时间
- 充电时长
- 充入电量 (kWh)
- 充电费用（如有）
- 充电类型（AC/DC）
- 起始/结束电量 (%)
- 充电位置
- 充电功率曲线（电压/电流/功率随时间变化）

**交互设计**：
```
┌─────────────────────────────────┐
│  ← 充电历史         筛选 ▼     │
├─────────────────────────────────┤
│  ┌───────────────────────┐      │
│  │ 📊 月度充电统计图表     │      │  ← 柱状图：每月充电次数/电量
│  │ [===|====|===|==]      │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ ⚡ DC · 超级充电站     │      │  ← 充电记录列表
│  │ 2026-06-22 14:30      │      │
│  │ 45% → 92% · 32.5 kWh  │      │
│  │ ¥58.50 · 45分钟       │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 🔌 AC · 家充桩         │      │
│  │ 2026-06-21 22:00      │      │
│  │ 30% → 90% · 42.1 kWh  │      │
│  │ ¥25.26 · 6小时        │      │
│  └───────────────────────┘      │
└─────────────────────────────────┘
```

**详情页**：
- 充电功率曲线图（时间轴可缩放）
- 电压/电流/温度曲线
- 充电位置地图
- 费用明细

**参考实现**：
- matedroid：`ChargeDetailScreen` — 功率曲线 + 地图
- t-buddy：Charge History + Charge Detail 视图

---

#### 3.2.X Mock 模式（P0）

**功能描述**：内置虚拟数据，无需 TeslaMate 实例即可预览 App UI。

**实现方案**：
- 设置页提供 "Enable Mock Mode" 开关
- 内置 1 个虚拟车辆 + 30 天历史数据
- Mock 数据放在 `src/mock/data.json`
- 切换 Mock/Real 模式不需要重启 App

**用途**：
- 开发期 UI 调试
- App Store 截图拍摄
- 给评估者演示

**参考实现**：`teslamate-modern-dashboard` 的 Mock 数据方案

---

#### 3.2.X+1 离线缓存（P0）

**功能描述**：弱网/无网环境下仍可查看历史数据。

**实现方案**：
- 缓存最近 30 天的 drives/charges 列表
- TTL 24h（超时后仍可显示，但标记 "stale"）
- 离线时：
  - Dashboard 显示 "Offline - showing last known status"
  - 列表显示缓存数据 + 顶部 "Offline" banner
  - 详情页若未缓存，显示 "This drive is not cached for offline viewing"

---

#### 3.2.3 驾驶历史（P0）

**功能描述**：展示所有行程记录，包含路线地图和效率数据。

**数据项**：
- 行程开始/结束时间
- 行程距离 (km)
- 行程时长
- 平均能耗 (Wh/km)
- 起始/结束电量 (%)
- 起点/终点地址
- 行驶路线（地图轨迹）
- 平均/最高速度

**交互设计**：
```
┌─────────────────────────────────┐
│  ← 行程记录          筛选 ▼    │
├─────────────────────────────────┤
│  ┌───────────────────────┐      │
│  │ 📊 效率趋势图          │      │  ← 折线图：近期效率变化
│  │ ─────/\───────/\──     │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 🚗 家 → 公司           │      │  ← 行程卡片
│  │ 06-22 08:30 → 09:15   │      │
│  │ 23.5 km · 45min       │      │
│  │ 142 Wh/km · 消耗8%    │      │
│  │ [路线地图缩略图]        │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 🚗 公司 → 超充站       │      │
│  │ 06-21 18:00 → 18:25   │      │
│  │ 12.3 km · 25min       │      │
│  │ 158 Wh/km · 消耗5%    │      │
│  └───────────────────────┘      │
└─────────────────────────────────┘
```

**详情页**：
- 全屏路线地图（高德/Apple Maps）
- 速度-时间曲线
- 能耗-时间曲线
- 行程对比（同路线不同时期）

---

#### 3.2.4 电池健康（P1）

**功能描述**：追踪电池衰减趋势，评估电池健康状况。

**数据项**：
- 当前可用容量 vs 出厂容量
- 衰减百分比
- 续航衰减量
- 充电循环次数（估算）
- 容量-时间趋势图
- 与同车型/同里程的对比（匿名统计，v2.0）

**UI 布局**：
```
┌─────────────────────────────────┐
│  ← 电池健康                     │
├─────────────────────────────────┤
│  ┌───────────────────────┐      │
│  │      🔋 94.2%         │      │  ← 健康度大数字
│  │    健康状况：优秀       │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 出厂容量    75.0 kWh   │      │
│  │ 当前容量    70.7 kWh   │      │
│  │ 衰减量       4.3 kWh   │      │
│  │ 总里程     45,230 km   │      │
│  └───────────────────────┘      │
│                                 │
│  ┌───────────────────────┐      │
│  │ 📈 容量衰减趋势图       │      │  ← 折线图：容量随时间/里程变化
│  │ ─────\──────\────      │      │
│  └───────────────────────┘      │
└─────────────────────────────────┘
```

---

#### 3.2.5 里程统计（P1）

**功能描述**：按年/月/日统计行驶里程和能耗，支持逐级钻取。

**钻取层级**：
```
Year View (年度)
  └─ 月度统计卡片 × 12
       └─ Month View (月度)
            └─ 日度统计卡片 × 28-31
                 └─ Day View (日度)
                      └─ 当日行程列表 × N
                           └─ Drive Detail
```

**每层展示**：

| 层级 | 关键数据 | 图表 | 交互 |
|------|----------|------|------|
| Year | 总里程 / 总能耗 / 平均效率 / 充电次数 | 月度柱状图 | 点击某月 → Month |
| Month | 月里程 / 月能耗 / 月充电成本 | 日度热力图 + 折线 | 点击某日 → Day |
| Day | 日里程 / 日能耗 / 行程数 | 小时分布柱状图 | 点击某行程 → Drive Detail |

**借鉴**：matedroid 的 `MileageScreen.kt` 钻取模式。

---

#### 3.2.6 桌面 Widget（P1）

**Android Widget**：
- 4×2：电量 + 续航 + 状态 + 位置
- 2×2：电量环形进度
- 锁屏 Widget（Android 16+）

**iOS Widget**：
- Small：电量百分比
- Medium：电量 + 续航 + 状态
- Lock Screen：电量环
- Live Activity：充电进度实时显示

---

#### 3.2.7 推送通知（P1）

| 通知类型 | 触发条件 | iOS 优先级 |
|----------|----------|------------|
| 充电完成 | 充电状态变为 complete | 普通 |
| 电量过低 | 电量 < 20% | Time Sensitive |
| 充电中断 | 充电异常停止 | Time Sensitive |
| 哨兵模式触发 | sentry_mode = true | 普通 |
| 胎压异常 | 胎压超出安全范围 | Time Sensitive |
| 软件更新可用 | 新版本检测 | 普通 |
| 车辆离线 | 超过 N 小时未上线 | 普通 |

**实现方案**：
- P0/P1 阶段：App 前台时 HTTP 轮询 + 本地通知
- P2 阶段：MQTT 直连优化实时性

---

#### 3.2.8 Apple Watch App（P2）

**功能描述**：手腕上快速查看车辆状态。

**Glance 视图**：
- 电量百分比 + 环形进度
- 续航里程
- 车辆状态（在线/充电/休眠）
- 最后更新时间

**Complication**：
- 表盘小组件：电量百分比
- 支持 Corner / Circular / Graphic 等多种样式

**技术方案**：watchOS 10+，SwiftUI，独立 App（不依赖 iPhone 常驻连接）

---

#### 3.2.9 设置页（P2）

**功能项**：
- TeslaMate 服务器地址配置
- API Token 管理
- 主题切换（亮色/暗色/跟随系统）
- 语言切换（中文/英文）
- 通知偏好设置
- 单位设置（km/mi、°C/°F）
- 关于/版本信息

---

## 4. 核心用户流程

### 4.1 Flow A：首次启动 → Dashboard

```
[Welcome 页] → [输入 TeslaMate URL + Token] → [Test Connection]
    ↓ 失败                                    ↓ 成功
[显示错误，停留]                          [显示找到 N 辆车] → [Dashboard]
```

时长目标：从启动到 Dashboard < 30s（含用户输入）

### 4.2 Flow B：查看行程详情

```
[Dashboard] → [Tab: Drives] → [行程列表] → [点击某条] → [行程详情：轨迹+曲线]
```

时长目标：从点击列表项到详情可交互 < 1s

### 4.3 Flow C：充电中查看进度

```
[打开 App / 收到推送] → [Dashboard 状态徽章: Charging] → [充电进度卡片]
```

### 4.4 Flow D：离线查看历史

```
[无网络打开 App] → [Dashboard 顶部黄色 banner: "Offline"] → [缓存数据展示]
```

### 4.5 Flow E：启用 Mock 模式

```
[Settings] → [开启 "Mock Mode"] → [提示 "Mock mode enabled. All data is fake."]
    → [Dashboard 显示虚拟车辆 "Demo Car" + 30 天虚拟数据]
```

用途：无 TeslaMate 实例时预览 UI、App Store 截图、演示评估。

---

## 5. 状态规范

### 5.1 状态徽章颜色

| 状态 | 颜色 | 含义 |
|------|------|------|
| online | 绿色 | 车辆在线 |
| driving | 蓝色 | 行驶中 |
| charging | 橙色 | 充电中 |
| asleep | 灰色 | 休眠 |
| offline | 深灰 | 离线 |

### 5.2 空状态

- 无行程：插画 + "No drives yet. Go for a drive!"
- 无充电：插画 + "No charges yet. Time to plug in!"
- 电池数据不足：插画 + "Need more data. Drive for a few weeks."

### 5.3 加载状态

- 首次加载：骨架屏（Skeleton）
- 刷新：顶部 Loading 指示器
- 详情加载：卡片骨架屏

### 5.4 错误状态

- 网络错误：Toast + "Retry" 按钮
- 服务器错误：全屏错误页 + 错误码 + "Retry"
- 认证失败：弹窗提示 + 跳转 Settings

---

## 6. 无障碍要求

| 要素 | 要求 |
|------|------|
| 字体大小 | 支持系统字体缩放（Dynamic Type / Font Scale） |
| 颜色对比度 | WCAG AA 标准（4.5:1） |
| VoiceOver / TalkBack | 所有可交互元素有 label |
| 触摸目标 | ≥ 44pt × 44pt（iOS）/ 48dp × 48dp（Android） |
| 减少动效 | 支持系统"减少动效"开关 |

---

## 7. 法律免责

App Store / Google Play 描述必须包含：
- "Not affiliated with Tesla, Inc."
- "Tesla is a registered trademark of Tesla, Inc."
- "Requires self-hosted TeslaMate instance"
- "Your data stays on your server — this app does not collect or transmit your data to third parties"

---

## 8. 非功能需求

### 4.1 性能要求

| 指标 | 目标 |
|------|------|
| 冷启动时间 | ≤ 2s（iPhone 12 / Pixel 6 级别） |
| 热启动时间 | ≤ 0.5s |
| Dashboard 数据加载 | ≤ 1s（有缓存） |
| 列表滚动帧率 | ≥ 55fps (iOS) / ≥ 50fps (Android) |
| 详情页打开 | ≤ 1s（含地图 + 图表） |
| 离线可用性 | 历史数据可离线查看 |

**资源消耗预算**：

| 资源 | 限制 |
|------|------|
| App 安装包 | ≤ 40MB (Android) / ≤ 50MB (iOS) |
| 内存占用 | ≤ 200MB（iOS 内存警告阈值） |
| 磁盘缓存 | ≤ 50MB（自动 LRU 清理） |
| 网络流量（5s 轮询 1h） | ≤ 1MB（开启 gzip） |
| 电量消耗（1h 前台运行） | ≤ 2% (iOS) / ≤ 3% (Android) |

### 4.2 安全要求

| 要求 | 说明 |
|------|------|
| Token 存储 | Android Keystore / iOS Keychain |
| 通信加密 | HTTPS only，证书固定 |
| 无 Tesla 账号 | App 不存储 Tesla 账号密码，只用 TeslaMate API Token |
| 数据隔离 | 每个用户只能访问自己的数据 |

### 4.3 兼容性

| 平台 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| Android | API 26 (Android 8.0) | API 31+ | Jetpack Compose 需 API 26 |
| iOS | iOS 16.0 | iOS 17+ | Swift Charts 需 16+，WidgetKit 交互式需 17+ |
| TeslaMateApi | v1.21+ | v1.21+ | **注意：v2.0 不存在** |

---

## 5. UI/UX 设计规范

### 5.1 设计原则

1. **数据优先**：核心数据一目了然，减少层级
2. **原生体验**：遵循 Material 3 (Android) / Human Interface Guidelines (iOS)
3. **暗色友好**：默认支持暗色主题，减少夜间刺眼
4. **信息密度**：合理利用屏幕空间，避免过度留白

### 5.2 色彩方案

| 用途 | 亮色 | 暗色 |
|------|------|------|
| 主色 | #1E88E5 | #42A5F5 |
| 背景 | #FAFAFA | #121212 |
| 卡片 | #FFFFFF | #1E1E1E |
| 文字主色 | #212121 | #E0E0E0 |
| 文字次色 | #757575 | #9E9E9E |
| 成功 | #43A047 | #66BB6A |
| 警告 | #FB8C00 | #FFA726 |
| 错误 | #E53935 | #EF5350 |

### 5.3 字体

- Android：系统默认 (Roboto / 厂商字体)
- iOS：SF Pro
- 中文：系统默认中文字体

### 5.4 图表规范（参考 matedroid CLAUDE.md）

- 柱状图：每根柱子可点击，显示数值 tooltip
- 折线图：Y 轴 4 个标签（1/4、1/2、3/4、末端），X 轴 5 个标签
- 点击图表任意点显示对应数值

---

## 6. 开发里程碑

### Phase 1：MVP v1.0（第 1-8 周）

**目标**：可上架的最小可用版本，含中国本地化

| 周次 | 任务 |
|------|------|
| W1 | 环境搭建：TeslaMateApi Docker 部署，项目初始化 |
| W2 | Dashboard 页面：电量、状态、位置、2D 车辆图 |
| W3 | 充电历史列表 + 详情页 |
| W4 | 驾驶历史列表 + 详情页（含地图） |
| W5 | 电池健康 + 软件更新 + 多车切换 |
| W6 | 中国本地化：高德地图 + GCJ-02 纠偏 + 分时电价 + 中文 UI |
| W7 | Mock 模式 + 离线缓存 + 暗色主题 |
| W8 | 测试 + Bug 修复 + 上架 App Store & Google Play |

**MVP 交付物**：Android + iOS，含中国本地化

### Phase 2：v1.1（第 9-14 周）

| 周次 | 任务 |
|------|------|
| W9-10 | 统计概览（钻取）+ 桌面 Widget |
| W11 | 推送通知系统 |
| W12-13 | 多语言（中/英）+ 测试 |
| W14 | v1.1 发布 |

### Phase 3：v1.2（第 15+ 周）

- 3D 车辆展示（Three.js）
- Apple Watch App（watchOS 10+，5 种 Complication）
- 活动热力图
- Vampire Drain 分析
- 年度报告
- 数据导出

---

## 7. 竞品对比

| 特性 | Mimo (本项目) | TeslaMate Web | Tesla 官方 App | MyTess |
|------|:---:|:---:|:---:|:---:|
| 数据深度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| 移动体验 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 数据主权 | ✅ 自托管 | ✅ 自托管 | ❌ Tesla 云 | ✅ 自托管 |
| 实时推送 | ✅ | ❌ | ✅ | ✅ |
| Widget | ✅ | ❌ | ✅ | ✅ |
| 离线查看 | ✅ | ❌ | ❌ | ✅ |
| 免费开源 | ✅ | ✅ | N/A | 部分付费 |
| 双平台 | Android + iOS | 浏览器 | Android + iOS | iOS only |

---

## 8. 风险与应对

### 8.1 技术风险

| 风险 | 严重度 | 应对策略 |
|------|--------|----------|
| TeslaMateApi 接口变更 | 🟡 中 | 版本检测 + 适配层抽象 + 锁定 v1.21.x |
| 3D 车辆渲染中低端机卡顿 | 🟡 中 | 分级降级：3D → 2D 图片 |
| 用户服务器宕机 | 🟡 中 | 离线缓存 + 友好错误提示 |

### 8.2 合规与法律风险

| 风险 | 严重度 | 应对策略 |
|------|--------|----------|
| Tesla 商标侵权 | 🔴 高 | 不用 Tesla 作主名/图标；App 描述含免责声明 |
| TeslaMate 名称冒用 | 🟡 中 | 不用 TeslaMate 作主名；用 "for TeslaMate" 副标题 |
| AGPL 传染 | 🔴 高 | 不修改 TeslaMate 源码，仅消费 API |
| 数据隐私违规（GDPR/PIPL） | 🔴 高 | App 不收集用户数据；隐私政策明确说明 |
| App Store 审核被拒（车辆类严审） | 🟡 中 | 明确免责 + 强调 "requires self-hosted" |
| 高德地图合规（中国） | 🟡 中 | 用官方 SDK + 注册 bundle id |

### 8.3 产品风险

| 风险 | 严重度 | 应对策略 |
|------|--------|----------|
| 与竞品差异化不足 | 🟡 中 | 强化中国本地化 + 开源跨平台定位 |
| 功能蔓延（Scope Creep） | 🔴 高 | 严格按 PRD 路线图，GitHub Issues 评估再决定 |
| 用户不知如何部署 TeslaMate | 🟡 中 | 写中文部署教程 + 引导到 TeslaMate 官方文档 |

---

## 9. 参考资料

### 已克隆参考仓库

| 仓库 | 路径 | 参考价值 |
|------|------|----------|
| matedroid | `docs/git_ref/mimo/matedroid/` | Android 架构、功能模块、UI 设计 |
| t-buddy | `docs/git_ref/mimo/t-buddy/` | iOS 架构、SwiftUI 实现、Widget |
| mytess | `docs/git_ref/mimo/mytess-teslamate-app/` | 商业化模式、部署方案 |
| teslamateapi | `docs/git_ref/mimo/teslamateapi/` | API 端点、数据格式 |
| teslamate-mcp | `docs/git_ref/mimo/teslamate-mcp/` | 数据分析查询逻辑 |
| Grafana Dashboards | `docs/git_ref/mimo/Teslamate-CustomGrafanaDashboards/` | 可视化指标选取 |
| Chinese Dashboards | `docs/git_ref/mimo/teslamate-chinese-dashboards/` | 中文本地化参考 |
| Flutter Tesla UI | `docs/git_ref/mimo/Animated-Tesla-Car-App-using-Flutter/` | 车辆动画效果 |
| RN Tesla App | `docs/git_ref/mimo/Tesla-app/` | 3D 车辆渲染参考 |

### 外部资料

- [TeslaMate 官方文档](https://docs.teslamate.org/)
- [TeslaMateApi GitHub](https://github.com/tobiasehlert/teslamateapi)
- [Tesla Fleet API 文档](https://developer.tesla.com/docs/fleet-api)
- [Material 3 设计规范](https://m3.material.io/)
- [Apple HIG](https://developer.apple.com/design/human-interface-guidelines/)

---

## 附录 A：页面清单

| 页面 | 优先级 | 平台 | 说明 |
|------|--------|------|------|
| Splash/引导页 | P0 | 双端 | 首次启动配置服务器 |
| Dashboard | P0 | 双端 | 实时状态主页 |
| 充电历史列表 | P0 | 双端 | 充电记录列表 |
| 充电详情 | P0 | 双端 | 单次充电详情+曲线 |
| 驾驶历史列表 | P0 | 双端 | 行程记录列表 |
| 驾驶详情 | P0 | 双端 | 单次行程详情+地图 |
| 电池健康 | P0 | 双端 | 衰减趋势 |
| 软件更新 | P0 | 双端 | OTA 更新历史 |
| 多车切换 | P0 | 双端 | 车辆选择器 |
| 中国本地化 | P0 | 双端 | 高德地图+分时电价+中文 |
| 设置 | P0 | 双端 | 服务器配置+偏好 |
| 里程统计（钻取） | P1 | 双端 | 年→月→日→行程 |
| 桌面 Widget | P1 | 双端 | 电量+续航+状态 |
| 推送通知 | P1 | 双端 | 充电/胎压/离线 |
| Apple Watch | P2 | watchOS | 电量+续航+状态 Glance |
| 3D 车辆 | P2 | 双端 | Three.js 渲染 |
| 热力图 | P2 | 双端 | 活动热力图 |
| 年度报告 | P2 | 双端 | 可分享图片 |

---

## 附录 B：数据模型

> 以下结构基于 teslamateapi v1.21.x 源码核实。

### Car（车辆） — `GET /api/v1/cars`
```json
{
  "data": {
    "cars": [
      {
        "car_id": 1,
        "name": "我的特斯拉",
        "car_details": {
          "eid": 123456789,
          "vid": 987654321,
          "vin": "5YJ3E1EA1PF000001",
          "model": "Model 3",
          "trim_badging": "LR",
          "efficiency": 142.5
        },
        "car_exterior": {
          "exterior_color": "SolidBlack",
          "spoiler_type": "None",
          "wheel_type": "AeroTurbine19"
        },
        "car_settings": {
          "suspend_min": 15,
          "suspend_after_idle_min": 15,
          "req_not_unlocked": false,
          "free_supercharging": false,
          "use_streaming_api": false
        },
        "teslamate_details": {
          "inserted_at": "2024-01-01T00:00:00Z",
          "updated_at": "2026-06-22T00:00:00Z"
        },
        "teslamate_stats": {
          "total_charges": 234,
          "total_drives": 567,
          "total_updates": 12
        }
      }
    ]
  }
}
```

**注意**：`eid`/`vid` 是 Tesla 内部 ID，通过 API 暴露，可直接使用。
