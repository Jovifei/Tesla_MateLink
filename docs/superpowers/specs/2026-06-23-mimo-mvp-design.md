---
comet_change: mimo-mvp
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-24-mimo-mvp
status: final
---

# MateLink MVP 深度设计文档

> 日期：2026-06-23
> 品牌名：MateLink
> 技术栈：原生（iOS Swift + Android Kotlin）+ Web 交互原型（React）

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 1. 总体架构

### 1.1 开发流程

```
Phase 0: Web 交互原型（React + Vite + Tailwind）
  │  模拟 App 全部页面和交互，用 Mock 数据
  │  Jovi 确认交互后进入下一阶段
  ▼
Phase 1: iOS App（Swift + SwiftUI + Swift Charts）
  │  基于确认的交互实现原生 App
  ▼
Phase 2: Android App（Kotlin + Jetpack Compose）
  │  参考 matedroid 架构，与 iOS 功能对齐
  ▼
Phase 3: 上架（App Store + Google Play）
```

### 1.2 系统数据流

```
┌─────────────┐     ┌──────────────┐     ┌──────────────────┐
│  Tesla 车辆  │────▶│  TeslaMate   │────▶│   PostgreSQL     │
│             │     │  (Elixir)    │     │   + MQTT Broker  │
└─────────────┘     └──────────────┘     └────────┬─────────┘
                                                   │
                                          ┌────────▼─────────┐
                                          │  TeslaMateApi    │
                                          │  (Go, v1.21+)    │
                                          └────────┬─────────┘
                                                   │
                                    ┌──────────────┼──────────────┐
                                    │              │              │
                              ┌─────▼─────┐  ┌────▼────┐  ┌─────▼─────┐
                              │   iOS     │  │ Android │  │ Web 原型  │
                              │  (Swift)  │  │(Kotlin) │  │ (React)   │
                              └───────────┘  └─────────┘  └───────────┘
```

### 1.3 API 端点（基于 teslamateapi v1.21 源码核实）

```
# 车辆信息
GET  /api/v1/cars                       # 车辆列表
GET  /api/v1/cars/{CarID}               # 单车信息
GET  /api/v1/cars/{CarID}/status        # 实时状态（5s 轮询）
GET  /api/v1/cars/{CarID}/battery-health # 电池健康

# 历史数据
GET  /api/v1/cars/{CarID}/charges       # 充电列表（分页）
GET  /api/v1/cars/{CarID}/charges/current # 当前充电
GET  /api/v1/cars/{CarID}/charges/{ChargeID} # 充电详情
GET  /api/v1/cars/{CarID}/drives        # 驾驶列表（分页）
GET  /api/v1/cars/{CarID}/drives/{DriveID} # 驾驶详情
GET  /api/v1/cars/{CarID}/updates       # 软件更新历史

# 系统
GET  /api/ping                          # 连通性测试
GET  /api/healthz                       # 健康检查
GET  /api/readyz                        # 就绪检查
GET  /api/v1/globalsettings             # 全局设置
```

### 1.4 数据模型（基于 teslamateapi 源码 struct）

```typescript
// 车辆信息（嵌套结构）
interface Car {
  car_id: number;
  name: string;
  car_details: {
    eid: number;
    vid: number;
    vin: string;
    model: string;
    trim_badging: string;
    efficiency: number;
  };
  car_exterior: {
    exterior_color: string;
    spoiler_type: string;
    wheel_type: string;
  };
  teslamate_stats: {
    total_charges: number;
    total_drives: number;
    total_updates: number;
  };
}

// 实时状态
interface CarStatus {
  car_id: number;
  state: 'online' | 'offline' | 'asleep' | 'charging' | 'driving';
  battery_level: number;
  usable_battery_range_km: number;
  odometer: number;
  inside_temp: number;
  outside_temp: number;
  tire_pressure_front_left: number;
  tire_pressure_front_right: number;
  tire_pressure_rear_left: number;
  tire_pressure_rear_right: number;
  latitude: number;
  longitude: number;
  charger_power: number;
  charge_energy_added: number;
}
```

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 2. Phase 0：Web 交互原型

### 2.1 技术栈

| 技术 | 用途 |
|------|------|
| React 18+ | UI 框架 |
| Vite | 构建工具 |
| Tailwind CSS | 样式 |
| Recharts | 图表（Web 端图表库） |
| React Router | 页面路由 |
| Zustand | 状态管理 |
| Mock 数据 | 内置 JSON 文件 |

### 2.2 页面与交互清单

#### Dashboard

**展示内容**：
- 车辆名称 + 状态徽章（online/driving/charging/asleep/offline + 颜色）
- 2D 车辆图（按车色匹配）
- 电量百分比 + 续航里程（大数字）
- 4 个信息卡片：位置、里程、温度、胎压
- 充电中额外显示：充电功率、已充电量、预计剩余时间

**交互**：
- 5s 自动刷新（模拟）
- 下拉刷新
- 点击车辆名 → 弹出车辆切换 Modal
- 点击状态徽章 → 高亮显示

#### 充电列表

**展示内容**：
- 日期分组（Today / Yesterday / 更早）
- 每条：日期时间、充电时长、充入电量、费用、AC/DC 标识、起止电量

**交互**：
- 上拉加载更多（分页 20 条）
- 顶部筛选：AC / DC / 全部
- 点击某条 → 进入充电详情

#### 充电详情

**展示内容**：
- 顶部：日期、时长、地点
- 地图：充电位置标记
- 统计卡片：充入电量、费用、效率、起止电量
- 图表标签切换：功率曲线 / 电压曲线 / 温度曲线

**交互**：
- 图表可缩放（鼠标滚轮/双指）
- 图表可拖拽平移
- 悬停显示数值 Tooltip
- 标签切换图表

#### 驾驶列表

**展示内容**：
- 日期分组
- 每条：日期时间、距离、时长、效率(Wh/km)、起止地址

**交互**：
- 上拉加载更多
- 点击某条 → 进入驾驶详情

#### 驾驶详情

**展示内容**：
- 顶部：日期、时长、距离
- 地图：完整路线（Polyline）+ 起止标记
- 统计卡片：平均速度、最高速度、能耗、效率、室外温度
- 图表标签切换：速度曲线 / 功率曲线 / 海拔曲线

**交互**：
- 地图可缩放、拖拽
- 图表交互同充电详情
- 路线动画播放（可选）

#### 电池健康

**展示内容**：
- 健康度百分比（大数字 + 环形进度）
- 出厂容量 vs 当前容量
- 衰减百分比、续航损失
- 衰减趋势折线图（容量 vs 时间）

**交互**：
- 折线图缩放、悬停 Tooltip

#### 设置

**展示内容**：
- 连接：服务器 URL、API Token、测试连接按钮
- 偏好：单位（km/mi）、温度（°C/°F）、主题（亮/暗/跟随系统）
- 中国本地化：地图选择、分时电价配置
- 开发：Mock 模式开关
- 关于：版本、开源协议、GitHub 链接

**交互**：
- 表单输入、开关切换
- 测试连接按钮 → 显示成功/失败
- Mock 模式开关 → 确认对话框 → 顶部 banner

#### 首次配置

**展示内容**：
- 欢迎页：Logo + "连接 TeslaMate" 按钮
- 配置页：URL 输入、Token 输入、测试连接
- 结果页：连接成功 → 显示车辆数 → 进入 Dashboard

**交互**：
- 表单验证（URL 格式、非空）
- 测试连接动画（Loading → 成功/失败）
- 错误提示（网络不可达 / 401 / 超时）

### 2.3 Mock 数据

内置 1 辆虚拟车 + 30 天历史数据：

```json
{
  "car": {
    "car_id": 1,
    "name": "Demo Car",
    "model": "Model 3",
    "exterior_color": "SolidBlack"
  },
  "status": {
    "state": "online",
    "battery_level": 78,
    "usable_battery_range_km": 312,
    "odometer": 45230
  },
  "charges": [...],   // 30 条充电记录
  "drives": [...],    // 50 条驾驶记录
  "battery_health": {
    "original_capacity": 75.0,
    "current_capacity": 70.7,
    "degradation_percent": 5.8
  }
}
```

### 2.4 目录结构

```
app_mimo/
├── web-prototype/          ← Phase 0: Web 交互原型
│   ├── src/
│   │   ├── components/     ← 通用组件（Card、Badge、Chart、Map）
│   │   ├── pages/          ← 页面（Dashboard、Charges、Drives...）
│   │   ├── hooks/          ← 自定义 hooks
│   │   ├── store/          ← Zustand 状态
│   │   ├── mock/           ← Mock 数据 JSON
│   │   ├── utils/          ← 工具函数
│   │   └── App.tsx
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
│
├── ios/                    ← Phase 1: iOS App
│   └── ...
│
└── android/                ← Phase 2: Android App
    └── ...
```

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 3. Phase 1：iOS App

### 3.1 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Swift | 5.9+ | 语言 |
| SwiftUI | iOS 16+ | UI 框架 |
| Swift Charts | iOS 16+ | 图表 |
| SwiftData | iOS 17+ | 本地缓存（备选：Core Data for iOS 16） |
| URLSession | - | 网络请求 |
| MapKit | - | 地图（海外） |
| 高德地图 SDK | - | 地图（中国） |

### 3.2 项目架构

```
ios/TeslaMateLink/
├── App/
│   ├── TeslaMateLinkApp.swift    ← 入口
│   └── ContentView.swift         ← Tab 导航
│
├── Features/
│   ├── Dashboard/
│   │   ├── DashboardView.swift
│   │   ├── DashboardViewModel.swift
│   │   └── Components/
│   │       ├── VehicleImageView.swift
│   │       ├── StatusBadgeView.swift
│   │       ├── BatteryCardView.swift
│   │       └── InfoGridView.swift
│   │
│   ├── Charges/
│   │   ├── ChargeListView.swift
│   │   ├── ChargeDetailView.swift
│   │   └── ChargeViewModel.swift
│   │
│   ├── Drives/
│   │   ├── DriveListView.swift
│   │   ├── DriveDetailView.swift
│   │   └── DriveViewModel.swift
│   │
│   ├── BatteryHealth/
│   │   ├── BatteryHealthView.swift
│   │   └── BatteryHealthViewModel.swift
│   │
│   └── Settings/
│       ├── SettingsView.swift
│       └── SettingsViewModel.swift
│
├── Core/
│   ├── API/
│   │   ├── APIClient.swift       ← URLSession + async/await
│   │   ├── APIEndpoints.swift
│   │   └── APIError.swift
│   │
│   ├── Models/
│   │   ├── Car.swift
│   │   ├── CarStatus.swift
│   │   ├── Charge.swift
│   │   ├── Drive.swift
│   │   └── BatteryHealth.swift
│   │
│   ├── Storage/
│   │   ├── CacheManager.swift    ← SwiftData / Core Data
│   │   └── KeychainManager.swift ← Token 安全存储
│   │
│   └── Utils/
│       ├── GCJ02Converter.swift  ← 坐标纠偏
│       ├── RouteSimplifier.swift ← 轨迹抽稀
│       └── UnitFormatter.swift   ← 单位格式化
│
├── Resources/
│   ├── Assets.xcassets/          ← 车辆图片、颜色
│   └── Localizable.strings       ← 中英文
│
└── Tests/
```

### 3.3 关键实现

#### API Client

```swift
class APIClient {
    private let baseURL: URL
    private let token: String?

    func request<T: Decodable>(_ endpoint: String) async throws -> T {
        var request = URLRequest(url: baseURL.appendingPathComponent(endpoint))
        request.timeoutInterval = 10
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw APIError.serverError((response as? HTTPURLResponse)?.statusCode ?? 0)
        }
        return try JSONDecoder().decode(T.self, from: data)
    }
}
```

#### Dashboard 轮询

```swift
class DashboardViewModel: ObservableObject {
    @Published var status: CarStatus?
    private var timer: Timer?

    func startPolling() {
        timer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { _ in
            Task { await self.fetchStatus() }
        }
    }

    func fetchStatus() async {
        status = try? await apiClient.request("/cars/\(carId)/status")
    }
}
```

#### GCJ-02 坐标纠偏

```swift
struct GCJ02Converter {
    static func wgs84ToGcj02(lat: Double, lng: Double) -> (lat: Double, lng: Double) {
        // 标准 GCJ-02 偏移算法
        // 参考 teslamate-chinese-dashboards 的实现
    }
}
```

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 4. Phase 2：Android App

### 4.1 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 语言 |
| Jetpack Compose | - | UI 框架 |
| Material 3 | - | 设计系统 |
| Room | - | 本地缓存 |
| Retrofit + OkHttp | - | 网络请求 |
| MPAndroidChart | - | 图表 |
| 高德地图 SDK | - | 地图（中国） |

### 4.2 项目架构（参考 matedroid）

```
android/app/src/main/java/com/teslamatelink/
├── ui/
│   ├── dashboard/
│   ├── charges/
│   ├── drives/
│   ├── battery/
│   ├── settings/
│   └── components/
│
├── data/
│   ├── api/          ← Retrofit API
│   ├── model/        ← 数据模型
│   ├── repository/   ← 数据仓库
│   └── cache/        ← Room 数据库
│
├── domain/
│   ├── usecase/
│   └── mapper/
│
└── di/               ← Hilt 依赖注入
```

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 5. 设计规范

### 5.1 Apple-Like 视觉风格

| 元素 | 规范 |
|------|------|
| 卡片圆角 | 16-20pt |
| 卡片间距 | 12-16pt |
| 背景 | 毛玻璃效果（iOS）/ 浅灰（Android） |
| 字体 | SF Pro（iOS）/ 系统默认（Android） |
| 强调色 | 基于车色动态生成 |
| 暗色主题 | 跟随系统 + 手动切换 |

### 5.2 状态徽章颜色

| 状态 | 颜色 | 含义 |
|------|------|------|
| online | 绿色 #43A047 | 车辆在线 |
| driving | 蓝色 #1E88E5 | 行驶中 |
| charging | 橙色 #FB8C00 | 充电中 |
| asleep | 灰色 #9E9E9E | 休眠 |
| offline | 深灰 #616161 | 离线 |

### 5.3 图表规范

- 柱状图：每根柱子可点击，显示数值 Tooltip
- 折线图：Y 轴 4 个标签（1/4、1/2、3/4、末端），X 轴 5 个标签
- 交互：缩放、拖拽、悬停显示数值

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 6. 测试策略

| 类型 | 工具 | 覆盖范围 |
|------|------|----------|
| 单元测试 | XCTest (iOS) / JUnit (Android) | API Client、数据模型、工具函数 |
| UI 测试 | XCUITest (iOS) / Espresso (Android) | 核心流程（Dashboard → 列表 → 详情） |
| Web 原型测试 | 手动 | 所有页面交互确认 |
| 真机测试 | 手动 | iOS + Android 各 1 台 |
| 性能测试 | Instruments (iOS) / Profiler (Android) | 启动时间、帧率、内存 |

archived-with: 2026-06-24-mimo-mvp
status: final
---

## 7. 风险与缓解

| 风险 | 严重度 | 缓解方案 |
|------|--------|----------|
| TeslaMateApi 接口变更 | 🟡 中 | 锁定 v1.21+，字段可选化 |
| 高德地图 SDK 集成复杂 | 🟡 中 | 先做海外版（系统地图），中国本地化后做 |
| App Store 审核被拒 | 🔴 高 | 明确免责 + 强调 "requires self-hosted" |
| Tesla 商标侵权 | 🔴 高 | 不用 Tesla 作主名，描述含免责声明 |
| 单人双端周期长 | 🟡 中 | 先 iOS，有参考代码可加速 |
