# glm_02 · 功能需求

## 1. 功能优先级分级

- **P0（MVP 必做）**：不上线就不能发布
- **P1（v1.1 必做）**：上线后 4 周内补齐
- **P2（v1.2 规划）**：上线后 8 周内补齐
- **P3（v2.0 远期）**：根据用户反馈决定是否做

### 1.1 功能 × 版本矩阵（概览）

> ✅ = 该版本包含；⏳ = 该版本开发中；❌ = 不包含

| 功能模块 | MVP (v1.0) | v1.1 | v1.2 | v2.0+ |
|---|:---:|:---:|:---:|:---:|
| 实时 Dashboard | ✅ | ✅ | ✅ | ✅ |
| 2D 车辆图（按车色匹配） | ✅ | ✅ | ✅ | ✅ |
| 充电历史 + 详情 | ✅ | ✅ | ✅ | ✅ |
| 驾驶历史 + 详情 | ✅ | ✅ | ✅ | ✅ |
| 电池健康 | ✅ | ✅ | ✅ | ✅ |
| 软件更新历史 | ✅ | ✅ | ✅ | ✅ |
| 多车切换 | ✅ | ✅ | ✅ | ✅ |
| 浅色/深色主题 | ✅ | ✅ | ✅ | ✅ |
| Mock 模式 | ✅ | ✅ | ✅ | ✅ |
| 离线缓存 | ✅ | ✅ | ✅ | ✅ |
| 中国本地化（高德+分时电价） | ❌ | ✅ | ✅ | ✅ |
| 桌面 Widget | ❌ | ✅ | ✅ | ✅ |
| 推送通知 | ❌ | ✅ | ✅ | ✅ |
| 行程效率评分 | ❌ | ✅ | ✅ | ✅ |
| 访问过的地区 | ❌ | ✅ | ✅ | ✅ |
| 统计概览（钻取） | ❌ | ✅ | ✅ | ✅ |
| 多语言（5+ 种） | ❌ | ✅ | ✅ | ✅ |
| 活动热力图 | ❌ | ❌ | ✅ | ✅ |
| Top 目的地 | ❌ | ❌ | ✅ | ✅ |
| Vampire Drain 分析 | ❌ | ❌ | ✅ | ✅ |
| 3D 车辆展示（Three.js） | ❌ | ❌ | ✅ | ✅ |
| 年度报告 | ❌ | ❌ | ✅ | ✅ |
| 数据导出 | ❌ | ❌ | ✅ | ✅ |
| 多实例切换 | ❌ | ❌ | ✅ | ✅ |
| 车端命令（wake/lock/charge） | ❌ | ❌ | ❌ | ✅ |
| AI 自然语言查询 | ❌ | ❌ | ❌ | ✅ |
| 多车对比 | ❌ | ❌ | ❌ | ✅ |
| 驾驶行为评分 | ❌ | ❌ | ❌ | ✅ |
| 地理围栏管理 | ❌ | ❌ | ❌ | ✅ |
| Apple Watch App | ❌ | ❌ | ✅ | ✅ |

## 2. 功能矩阵

### 2.1 P0 · MVP（v1.0）

| ID | 功能 | 描述 | 借鉴来源 |
|---|---|---|---|
| F-001 | **首次配置** | 输入 TeslaMateApi URL + Token，校验连通性 | matedroid |
| F-002 | **车辆选择** | 多车账号下切换当前车辆 | matedroid |
| F-003 | **Dashboard 实时状态** | 电量、续航、状态（在线/睡眠/充电/行驶）、里程、位置、胎压 | matedroid / teslamate-moblie |
| F-004 | **2D 车辆图** | 在 Dashboard 显示 2D 车辆图片，按车色/wheel 匹配（3D 降级为 F-205） | matedroid |
| F-005 | **实时位置地图** | 当前位置地图 + 状态徽章 | matedroid |
| F-006 | **行程列表** | 分页列表，显示日期/距离/时长/能耗/效率 | teslamate-moblie |
| F-007 | **行程详情** | 起止地点、轨迹地图、速度/功率/海拔曲线 | matedroid |
| F-008 | **充电列表** | 分页列表，显示日期/能量/成本/类型（AC/DC） | teslamate-moblie |
| F-009 | **充电详情** | 地点地图、功率/电压/温度曲线、充电进度 | matedroid |
| F-010 | **电池健康** | 当前 vs 原始容量、衰减百分比、衰减曲线 | teslamate-moblie |
| F-011 | **软件更新历史** | 版本列表 + 安装日期 | matedroid |
| F-012 | **浅色/深色主题** | 跟随系统 + 手动切换 | matedroid |
| F-013 | **基础设置** | 服务器配置、单位（公里/英里）、时区、登出 | teslamate-moblie |
| F-014 | **Mock 模式** | 内置 Mock 数据，无需 TeslaMate 实例即可预览 UI | teslamate-modern-dashboard |
| F-015 | **离线缓存** | 最近 N 次数据缓存，弱网可看历史 | matedroid |

### 2.2 P1 · v1.1（上线后 4 周）

| ID | 功能 | 描述 | 借鉴来源 |
|---|---|---|---|
| F-101 | **中国本地化** | 高德地图 + GCJ-02 坐标纠偏 + 中文 UI | teslamate-chinese-dashboards |
| F-102 | **分时电价** | 峰平谷电价配置 + 充电成本重算 | teslamate-chinese-dashboards |
| F-103 | **桌面 Widget** | iOS Home Widget + Android Widget 显示电量/续航 | matedroid / hedgiemate |
| F-104 | **推送通知** | 充电完成/充电中断/Sentry 事件/胎压告警 | matedroid |
| F-105 | **行程效率评分** | "Golden Foot" 评分 + 安全距离分析 | mytess |
| F-106 | **访问过的地区统计** | 国家/省份旗帜 + 距离 + 能耗 | matedroid |
| F-107 | **统计概览** | 总里程/总能耗/驾驶次数 + 月度图表 | teslamate-moblie |
| F-108 | **多语言** | 中文、英语、日语、德语、法语 | matedroid |

### 2.3 P2 · v1.2（上线后 8 周）

| ID | 功能 | 描述 | 借鉴来源 |
|---|---|---|---|
| F-201 | **电池热力图** | 15 天活动热力图（GitHub 风格） | teslamate-modern-dashboard |
| F-202 | **Top 目的地** | 最常去地点排行 | teslamate-modern-dashboard |
| F-203 | **vampire drain 分析** | 停车掉电统计 | teslamate (grafana) |
| F-204 | **充电曲线分析** | DC 充电速度曲线 + 不同充电桩对比 | teslamate (grafana) |
| F-205 | **3D 车辆展示**（从 P0 降级） | Three.js + R3F 渲染 3D 车辆模型，按车色/wheel 匹配 | gwesseling/Tesla-app |
| F-206 | **年度报告** | 年度汇总报告（可分享图片） | teslamate-chinese-dashboards |
| F-207 | **数据导出** | CSV / PDF 导出 | — |
| F-208 | **多实例切换** | 连接多个 TeslaMate 实例 | — |
| F-209 | **Apple Watch App**（重新升级到 P2） | iPhone 配套 watchOS App：电量/续航/状态 glance + Complication + 充电进度 | hedgiemate |

### 2.4 P3 · v2.0+（远期）

| ID | 功能 | 描述 | 借鉴来源 |
|---|---|---|---|
| F-301 | **车端命令** | wake_up / lock / unlock / charge_start / climate_on | vehicle-command SDK |
| F-302 | **AI 自然语言查询** | "上周哪天最费电"、"对比 Q1 和 Q2 的能耗" | teslamate-mcp |
| F-303 | **多车对比** | 两辆车并排数据对比 | teslamate-chinese-dashboards |
| F-304 | **驾驶行为评分** | 急加速/急刹车/超速统计 | mytess |
| F-305 | **地理围栏管理** | 在 App 内创建/编辑 geofence | mytess |
| F-306 | **Home Assistant 集成** | 发现实体 + 联动自动化 | TeslaMateAgile |
| F-307 | **CarPlay / Android Auto** | 驾驶中简化界面 | — |
| F-308 | **Vision Pro 适配** | visionOS 沉浸式数据空间 | hedgiemate |
| F-309 | **iPad 大屏适配** | iPad 横屏分栏布局 | hedgiemate |

## 3. P0 功能详细需求

### F-001 首次配置

**用户故事**：作为新用户，我希望通过简单的引导连接到我的 TeslaMate 实例，以便开始使用 App。

**验收标准**：
- [ ] 首次启动显示欢迎页 + "Connect to TeslaMate" 按钮
- [ ] 输入页含字段：
  - Server URL（必填，例：`https://teslamate.example.com/api/v1`）
  - API Token（可选，取决于用户 TeslaMateApi 配置）
  - Display Name（可选，用于多实例区分）
- [ ] 点击 "Test Connection" 按钮，执行**三步连通性检测**（吸收 mimo 的健康检查端点发现）：
  1. `GET /api/ping` → 验证 URL 正确性（轻量）
  2. `GET /api/readyz` → 验证 DB + MQTT 都已就绪
  3. `GET /api/v1/cars` → 验证 Token 权限 + 拿到车辆数
- [ ] 三步全过 → 显示找到的车辆数，进入 Dashboard
- [ ] 任意一步失败 → 显示具体错误（指出失败的是哪一步）
- [ ] 凭据用 `expo-secure-store` 加密存储

**错误处理**：
| 错误 | 提示 |
|---|---|
| 网络不可达 | "Cannot reach server. Check URL and network." |
| 401 Unauthorized | "Invalid token. Verify API_TOKEN in TeslaMateApi config." |
| TLS 证书问题 | "Certificate error. Self-signed certs not supported." |
| 超时（>10s） | "Connection timeout. TeslaMate server may be slow or offline." |

### F-003 Dashboard 实时状态

**用户故事**：作为车主，我打开 App 就能看到车辆当前状态，无需点进任何菜单。

**数据来源**：`GET /api/v1/cars/:id/status`（轮询 5s 间隔）

**UI 元素**：
- 顶部：车辆名称 + 状态徽章（在线/睡眠/充电/行驶）
- 中部：3D 车辆模型 + 电量百分比 + 续航里程
- 下部：4 张卡片
  - 位置（地图缩略图 + 地址）
  - 里程（当前 odometer）
  - 气候（车内/外温度）
  - 胎压（4 个胎压值）
- 充电中：额外显示充电进度 + 剩余时间 + 当前功率

**状态徽章颜色**：
| 状态 | 颜色 |
|---|---|
| online | 绿色 |
| driving | 蓝色 |
| charging | 橙色 |
| asleep | 灰色 |
| offline | 深灰 |

**下拉刷新**：手动触发立即拉取最新 status。

### F-004 2D 车辆图（MVP）

**用户故事**：作为车主，我希望在 Dashboard 看到我车辆的图片，按车色和轮毂匹配，快速识别当前车辆。

**说明**：3D 车辆展示原为 P0，经评估降级为 P2（F-205）。MVP 用 2D 图片方案，借鉴 matedroid 的成熟实现。

**技术方案**：
- 按 `car_type`（model3 / modelY / modelS / modelX / cybertruck）选择车型图片
- 按 `exterior_color` 动态着色（使用 `react-native-fast-image` + 色调滤镜）
- 按 `wheel_type` 切换轮毂图
- 图片资源放在 `assets/images/cars/` 下，按 `<model>_<color>_<wheel>.png` 命名

**资源准备**：
- 5 个车型 × 6 种车色 × 2-3 种轮毂 = 60-90 张图片
- 每张 PNG 约 50-100KB，总计 5-10MB
- 可从 matedroid 的 `app/src/main/assets/car_images/` 直接借鉴（许可证待复核）

**交互**：
- 长按图片切换视角（正面/侧面/后面）
- 点击图片进入"Stats for Nerds"（F-107 统计）

**性能预算**：图片加载 < 200ms，内存占用 < 5MB。

**降级到 3D 的路径**（v1.2，F-205）：
- v1.2 可选启用 3D 模式，用 Three.js + R3F 渲染
- 用户可在 Settings 切换 2D/3D
- 低端机默认 2D，高端机可选 3D

### F-006 行程列表

**用户故事**：作为车主，我想查看历史行程列表，快速找到某次出行。

**数据来源**：`GET /api/v1/cars/:id/drives?page=1&limit=20`

**UI 元素**：
- 顶部：日期范围筛选（今天/本周/本月/自定义）
- 列表项：
  - 左：日期 + 起止时间
  - 中：起止地点（geocode 地址）
  - 右：距离 + 能耗 + 效率（Wh/km）
- 上拉加载更多（每页 20 条）
- 点击进入 F-007 行程详情

**空状态**：无行程时显示 "No drives yet. Go for a drive!"

### F-007 行程详情

**用户故事**：作为车主，我想查看某次行程的详细信息，包括路线和速度曲线。

**数据来源**：`GET /api/v1/cars/:id/drives/:driveId`

**UI 元素**：
- 顶部：日期 + 起止时间 + 总时长
- 地图：完整轨迹（Polyline）+ 起止标记
- 卡片：距离 / 平均速度 / 最高速度 / 能耗 / 效率 / 室外温度
- 图表（标签切换）：
  - 速度曲线（mph/km/h）
  - 功率曲线（kW）
  - 海拔曲线（m）
  - 温度曲线（内/外）
  - 胎压曲线（4 线）

**轨迹抽稀**：参考 `matedroid/domain/RouteSimplifier.kt`，避免手机渲染上千点卡顿。

### F-010 电池健康

**用户故事**：作为车主，我想了解电池衰减情况，判断是否需要维修。

**数据来源**：`GET /api/v1/cars/:id/battery-health`

**UI 元素**：
- 顶部：当前健康度百分比（大数字 + 进度环）
- 卡片：
  - 原始容量（kWh）vs 当前容量
  - 原始续航 vs 当前续航
  - 衰减百分比
  - 续航损失百分比
- 图表：衰减曲线（X 时间，Y 容量）
- 历史里程数（衰减对应里程）

**计算逻辑**：参考 `teslamate/grafana/dashboards/battery-health.json` 的 SQL。

### F-014 Mock 模式

**用户故事**：作为开发者/评估者，我想在没有 TeslaMate 实例的情况下预览 App UI。

**实现**：
- 设置页提供 "Enable Mock Mode" 开关
- 内置 1 个虚拟车辆 + 30 天历史数据
- Mock 数据放在 `src/mock/data.json`
- 切换 Mock/Real 模式不需要重启 App

**用途**：
- 开发期 UI 调试
- App Store 截图拍摄
- 给评估者演示

### F-015 离线缓存

**用户故事**：作为车主，我在地铁/飞机上也想查看历史行程。

**实现**：
- 用 MMKV 或 AsyncStorage 缓存最近 30 天的 drives/charges 列表
- 缓存 key：`{carId}_{endpoint}_{page}`
- TTL：24h（超时后仍可显示，但标记 "stale"）
- 离线时：
  - Dashboard 显示 "Offline - showing last known status"
  - 列表显示缓存数据 + 顶部 "Offline" banner
  - 详情页若未缓存，显示 "This drive is not cached for offline viewing"

## 4. P1 功能详细需求

### F-101 中国本地化

**用户故事**：作为中国车主，我希望地图用高德（OpenStreetMap 国内数据稀疏），且 UI 是中文。

**实现**：
- 地图组件根据 locale 切换：
  - `zh-CN` → 高德地图 SDK
  - 其他 → Google Maps / Apple Maps（`react-native-maps` 默认）
- 坐标纠偏：WGS-84（TeslaMate 原始） → GCJ-02（国内地图）
  - 参考实现：`teslamate-chinese-dashboards/sql/` 中的纠偏函数
- 中文 UI：i18n 文案库 `src/i18n/zh-CN.json`
- 默认时区：Asia/Shanghai
- 默认单位：公里

**合规注意**：
- 高德地图 SDK 需申请 Key（个人开发者免费）
- iOS bundle id + Android package name 需注册到高德开放平台

### F-102 分时电价

**用户故事**：作为中国车主，我希望按峰平谷电价计算充电成本，看哪个充电桩最划算。

**实现**：
- 设置页新增 "Time-of-Use Tariff" 入口
- 默认电价（可编辑）：
  - 峰时段（10:00-15:00, 18:00-21:00）：1.0 元/度
  - 平时段（07:00-10:00, 15:00-18:00, 21:00-23:00）：0.7 元/度
  - 谷时段（23:00-07:00）：0.3 元/度
- 充电详情显示：原成本 vs 重算成本
- 新增"充电桩性价比榜"：按 ¥/度 排序所有充电点

**借鉴**：`teslamate-chinese-dashboards/sql/` 的分时电价 SQL 逻辑。

### F-103 桌面 Widget

**iOS Widget**（WidgetKit）：
- Small：电量 + 续航
- Medium：电量 + 续航 + 状态 + 位置缩略图
- 数据刷新：App 端定时写入 App Group 共享存储，Widget 每 15min 读取

**Android Widget**（AppWidgetProvider）：
- 2x2：电量 + 续航
- 4x2：电量 + 续航 + 状态 + 位置
- 数据刷新：WorkManager 每 15min 拉取 status

**借鉴**：matedroid 的 `widget/` 完整实现。

### F-104 推送通知

**通知类型**（含优先级，用于 iOS 通知分组：Critical / Time Sensitive / 普通）：
| 事件 | 触发 | 内容 | 优先级 |
|---|---|---|---|
| 充电完成 | status.state 从 charging → online | "充电完成，电量 85%，新增 32kWh" | 普通 |
| 充电中断 | charging_process.end_date 非预期 | "充电中断，当前电量 45%" | Time Sensitive |
| 电量过低 | battery_level < 20% | "电量 18%，请尽快充电" | Time Sensitive |
| Sentry 事件 | sentry 通知 | "Sentry 检测到事件，点击查看" | 普通 |
| 胎压告警 | tire_pressure 阈值 | "右前胎压 2.0 bar，低于阈值" | Time Sensitive |
| 软件更新可用 | 新版本检测 | "新版本 2024.38.8 可用" | 普通 |
| 车辆离线超时 | 超过 N 小时未上线 | "车辆已离线 6 小时" | 普通 |

**实现**：
- iOS：需要自建 APNs 服务（因为我们不经用户数据，需要用户自行配置）
- Android：FCM（同样需要用户配置）
- **替代方案**：让 TeslaMate 端通过 webhook 推送到 App 端（用户配置）

**借鉴**：tete-manager-notifier 的 MQTT 监听 + 通知推送模式。

### F-107 统计概览（钻取交互）

**用户故事**：作为车主，我希望从"年总里程"逐级钻取到"某次行程详情"，快速定位特定出行。

**钻取层级**：

```
Year View (年度)
  └─ 月度统计卡片 × 12
       └─ Month View (月度)
            └─ 日度统计卡片 × 28-31
                 └─ Day View (日度)
                      └─ 当日行程列表 × N
                           └─ Drive Detail (F-007)
```

**每层展示**：

| 层级 | 关键数据 | 图表 | 交互 |
|---|---|---|---|
| Year | 总里程 / 总能耗 / 平均效率 / 充电次数 | 月度柱状图 | 点击某月 → Month |
| Month | 月里程 / 月能耗 / 月充电成本 | 日度热力图 + 折线 | 点击某日 → Day |
| Day | 日里程 / 日能耗 / 行程数 | 小时分布柱状图 | 点击某行程 → Drive Detail |

**数据来源**：
- 聚合数据可调用 `/drives` 列表后本地聚合（数据量小时）
- 或参考 `teslamate/grafana/dashboards/drive-stats.json` 的 SQL，由后端 API 端聚合（数据量大时）

**借鉴**：matedroid 的"year → month → day → drive" 钻取模式（`MileageScreen.kt`）。

## 4A. P2 功能详细需求

### F-209 Apple Watch App（v1.2 重点功能）

**用户故事**：作为车主，我希望抬腕就能看到车辆电量、续航和状态，无需掏出 iPhone。

**目标平台**：watchOS 10+（iOS 17+ 配套）

**功能模块**：

| 模块 | 界面 | 数据来源 |
|---|---|---|
| **主表盘** | 电量大数字 + 续航 + 状态徽章 | WatchConnectivity 从 iPhone 推 |
| **Complication** | 表盘并合并位（Corner / Circular / Rectangular / Inline） | 复用主表盘数据 |
| **充电进度** | 充电中显示进度环 + 剩余时间 | WatchConnectivity 推送 |
| **位置 glance** | 当前位置文字描述（不含完整地图） | 反向地理编码地址 |
| **下拉刷新** | 主动触发数据更新 | 通过 iPhone 转发请求 |

**Complication 设计**：

| 表盘位 | 显示内容 |
|---|---|
| **Corner** | 电量百分比环 |
| **Circular** | 电量百分比环 + 数字 |
| **Rectangular** | 电量 + 续航 + 状态徽章 |
| **Inline** | "🔋 78% · 312km" |
| **Modular** | 电量大数字 + 状态文字 |

**界面布局**：

```
┌─────────────┐
│   🔋 78%    │  ← 主表盘 (45mm 屏)
│   312 km    │
│ 🟢 在线    │
│             │
│ [▼ 刷新]    │
└─────────────┘

充电中：
┌─────────────┐
│   ⚡ 65%    │
│   ●●●●○○    │  ← 进度环
│  35 min     │
│  剩余       │
└─────────────┘
```

**技术方案**：

| 项 | 选型 |
|---|---|
| 开发语言 | Swift + SwiftUI（watchOS App 必须原生） |
| 通信 | WatchConnectivity Framework |
| 数据流 | iPhone 主 App → WCSession → Watch App |
| 数据存储 | UserDefaults（轻量缓存最近一次数据） |
| 推送 | 跟随 iPhone 通知镜像 + 独立 Watch 通知（充电完成） |

**与 RN 主 App 的桥接**：

```typescript
// iPhone RN 端：通过 Native Module 发送数据到 Watch
NativeModules.WatchBridge.sendCarStatus({
  battery: 78,
  range: 312,
  state: 'online',
  isCharging: false,
});
```

```swift
// iPhone Native Module (Swift)
@objc(WatchBridge)
class WatchBridge: NSObject {
  @objc func sendCarStatus(_ status: NSDictionary) {
    if WCSession.default.isReachable {
      WCSession.default.sendMessage(status as! [String: Any], replyHandler: nil)
    } else {
      try? WCSession.default.updateApplicationContext(status as! [String: Any])
    }
  }
}
```

```swift
// Watch App (SwiftUI)
class WatchSessionManager: NSObject, WCSessionDelegate {
  @Published var carStatus: CarStatus?

  func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
    DispatchQueue.main.async {
      self.carStatus = CarStatus(from: message)
    }
  }
}
```

**数据刷新策略**：

| 场景 | 策略 |
|---|---|
| Watch App 打开 | 立即向 iPhone 请求最新数据 |
| iPhone 收到 status 更新 | 立即推送到 Watch（若可达） |
| iPhone 离线 / Watch 不可达 | 用 `updateApplicationContext` 后台同步，下次连接时送达 |
| Complication 刷新 | 每 15 min 自动刷新（系统调度），充电中更频繁 |
| 用户在 Watch 下拉刷新 | 触发 iPhone 拉取最新数据后回传 |

**Apple Watch 不做的事**（明确边界）：

| 不做 | 原因 |
|---|---|
| 完整行程列表 | 屏幕太小，体验差 |
| 充电曲线图表 | Watch 不适合复杂图表 |
| 地图轨迹 | Watch 地图体验有限 |
| 设置 / 配置 | 在 iPhone 上配置 |
| 多车切换 | 仅显示当前 iPhone 选中的车 |

**电量优化**：
- 不在 Watch 上做轮询（依赖 iPhone 推送）
- Complication 刷新次数 < 50 次/天（Apple 推荐）
- 后台任务用 `WKApplicationRefreshBackgroundTask` 而不是定时器

**App Store 审核要点**：
- 必须证明 Watch App 独立价值（不只是镜像 iPhone）
- 充电进度 Live Activity 是 Apple Watch 独有亮点（区别于 Android）

**工作量估算**：

| 任务 | 工时 |
|---|---|
| Watch 主表盘 + 状态展示 | 2d |
| 5 种 Complication 实现 | 2d |
| 充电进度 + 进度环动画 | 1d |
| WatchConnectivity 桥接（RN ↔ Native ↔ Watch） | 2d |
| 后台数据同步 + Complication 刷新调度 | 1d |
| 测试（多种表盘 / 多种 Watch 型号） | 1d |
| **小计** | **9d** |

**借鉴**：
- `hedgiemate` 的多平台 watchOS 11+ 实现
- Apple 官方 watchOS 文档（WatchConnectivity / ClockKit）

**优先级说明**：Apple Watch 在 v1.0 GPT 审议时一度降级到 P3，但根据 Jovi 明确要求**重新升级到 P2**，作为 v1.2 重点功能与 3D 车辆并列。

## 5. 功能不做的明确说明

### 5.1 第一版不做

| 功能 | 原因 |
|---|---|
| 车端命令（wake/lock/charge_start） | 需要 Fleet API 申请周期 + Vehicle Command SDK 集成，v2.0 再做 |
| 直接连 Tesla API（绕过 TeslaMate） | 破坏 TeslaMate 数据一致性 + 重复造轮子 |
| 内置 Grafana 仪表盘查看 | 手机体验差，应引导用户用 Web 端 |
| 用户账号体系 | 不需要——App 直连用户自己的 TeslaMate |
| 云端数据备份 | 隐私敏感——TeslaMate 已有备份方案 |
| 内置 TeslaMate 部署 | 超出 App 范围，应引导到 TeslaMate 官方文档 |

### 5.2 永远不做

| 功能 | 原因 |
|---|---|
| 存储 Tesla 账号密码 | 隐私红线——TeslaMate 已经处理认证 |
| 上传用户数据到我们服务器 | 隐私红线 |
| 替代官方 Tesla App 的车控功能 | 不是产品定位 |
| 修改 TeslaMate 数据库 | 破坏数据一致性 |

## 6. 功能与版本映射

```
v1.0 (MVP) ──────► F-001 ~ F-015
                  │
                  └─► 目标: 上架 App Store + Google Play

v1.1 ────────────► F-101 ~ F-108
                  │
                  └─► 目标: 中国本地化 + Widget + 通知

v1.2 ────────────► F-201 ~ F-208
                  │
                  └─► 目标: Apple Watch + 高级分析

v2.0 ────────────► F-301 ~ F-308
                  │
                  └─► 目标: 车端命令 + AI 查询
```

## 7. 功能依赖图

```
F-001 (首次配置) ──► F-002 (车辆选择) ──► F-003 (Dashboard)
                                              │
                                              ├─► F-004 (3D 车辆)
                                              ├─► F-005 (位置地图)
                                              │
                                              └─► 列表入口
                                                    │
                                                    ├─► F-006 (行程列表) ──► F-007 (行程详情)
                                                    ├─► F-008 (充电列表) ──► F-009 (充电详情)
                                                    ├─► F-010 (电池健康)
                                                    └─► F-011 (更新历史)

F-014 (Mock 模式) ──► 独立开关，可替代 F-001 用于开发
F-015 (离线缓存) ──► 横向依赖所有数据加载功能
```

## 8. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_03_信息架构与页面流程.md`
