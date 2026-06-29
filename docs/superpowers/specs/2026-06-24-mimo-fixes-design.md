---
comet_change: mimo-fixes
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-24-mimo-fixes
status: final
---

# mimo-fixes 深度设计文档

> 日期：2026-06-24
> 目标：修复 mimo-mvp 交叉审核发现的遗留问题

## 1. Dashboard 增强（三端）

### 1.1 7 天电量趋势图

**数据来源**：`mock_data.json` 中的 `battery_health` 数组

**实现方案**：
- 从 battery_health 取最近 7 条记录
- X 轴：日期，Y 轴：电量百分比
- 图表库：Android MPAndroidChart / iOS Swift Charts / Web Recharts

**验收标准**：
- 图表显示 7 个数据点
- hover/点击显示具体数值
- 无数据时显示"数据不足"提示

### 1.2 门锁/充电线状态卡片

**数据来源**：`CarStatus.locked`、`CarStatus.plugged_in`

**实现方案**：
- Dashboard 信息网格新增 2 个卡片
- 门锁：🔒 Locked / 🔓 Unlocked
- 充电线：⚡ Plugged / Not Plugged
- 状态变化时高亮闪烁

### 1.3 高电量警告

**触发条件**：`charge_limit_soc > 90%`

**实现方案**：
- Dashboard 电量卡片下方显示黄色警告条
- 文案："⚠️ High charge level - consider reducing to 80-90% for daily use"
- 仅在充电上限 > 90% 时显示

### 1.4 海拔显示

**数据来源**：`CarStatus.elevation`

**实现方案**：
- 位置卡片下方显示海拔
- 格式："Elevation: 120m"

## 2. Web 状态管理统一

### 2.1 问题

`App.tsx` 用 `useState` 管理 `currentCarId`/`mockMode`/`theme`，`store.ts` 有 Zustand store 定义相同字段但未使用。

### 2.2 修复方案

1. 删除 `App.tsx` 中的 `useState` 声明
2. 从 `useStore()` 读取状态
3. 所有子组件统一通过 `useStore()` 访问

**代码变更**：
```typescript
// App.tsx - 删除
const [currentCarId, setCurrentCarId] = useState(1);
const [mockMode, setMockMode] = useState(true);
const [theme, setTheme] = useState<'light' | 'dark' | 'system'>('system');

// App.tsx - 改为
const { currentCarId, setCurrentCarId, mockMode, setMockMode, theme, setTheme } = useStore();
```

## 3. Web 页面补全

### 3.1 EfficiencyCurve

- 使用 `mock_data.json` 中的 drives 数据计算效率 vs 温度
- 添加回归线（多项式拟合）
- 高亮最优温度区间（18-25°C）

### 3.2 Trips

- 基于 drives 数据自动检测：连续行程 + DC 快充 + 总距离 > 300km
- 显示检测到的 trips 列表
- 点击查看详情（时间线 + 路线地图）

### 3.3 TopDestinations

- 集成 Leaflet 地图
- 从 drives 数据聚合目的地
- Marker 大小按访问次数缩放
- 列表↔地图联动

### 3.4 Mileage 钻取

- Year View：月度柱状图
- Month View：日度热力图
- Day View：当日行程列表
- 面包屑导航

### 3.5 Heatmap

- 使用真实 drives 数据（非随机）
- 15 天 × 24 小时网格
- hover 显示日期+时段+里程

### 3.6 Drives 骨架屏

- 首次加载显示 3 行骨架屏
- 加载完成后替换为真实数据

### 3.7 列表分页

- 所有列表页添加"Load More"按钮
- 每页 20 条
- 客户端分页（Mock 模式）

## 4. iOS 数据模型补全

### 4.1 新增模型

```swift
struct SentryEvent: Codable, Identifiable {
    let id: Int; let startDate: String; let endDate: String
    let latitude: Double; let longitude: Double; let address: String
    enum CodingKeys: String, CodingKey {
        case id; case startDate = "start_date"; case endDate = "end_date"
        case latitude, longitude, address
    }
}

struct Trip: Codable, Identifiable {
    let id: Int; let name: String; let startDate: String; let endDate: String
    let distanceKm: Double; let durationMin: Int; let energyUsed: Double
    let startAddress: String; let endAddress: String
    enum CodingKeys: String, CodingKey {
        case id, name; case startDate = "start_date"; case endDate = "end_date"
        case distanceKm = "distance_km"; case durationMin = "duration_min"
        case energyUsed = "energy_used"; case startAddress = "start_address"
        case endAddress = "end_address"
    }
}

struct VisitedRegion: Codable, Identifiable {
    let id = UUID(); let country: String; let countryCode: String
    let region: String; let distanceKm: Double; let energyUsed: Double
    let driveCount: Int; let chargeCount: Int; let lastVisit: String
    enum CodingKeys: String, CodingKey {
        case country; case countryCode = "country_code"; case region
        case distanceKm = "distance_km"; case energyUsed = "energy_used"
        case driveCount = "drive_count"; case chargeCount = "charge_count"
        case lastVisit = "last_visit"
    }
}
```

## 5. 交互增强

### 5.1 下拉刷新

- Android：SwipeRefresh 组件
- iOS：`.refreshable` modifier
- Web：自定义 RefreshControl

### 5.2 车辆切换 Modal

- Android：BottomSheet
- iOS：`.sheet` modifier
- Web：Modal 组件

### 5.3 卡片点击跳转

- 位置卡片 → 地图页
- 电量卡片 → 电池健康页
- 温度卡片 → 无跳转（仅显示）

## 6. 验收标准

| 功能 | 验收标准 |
|------|----------|
| 7 天趋势图 | 三端显示 7 个数据点，hover 显示数值 |
| 门锁/充电线卡片 | 状态正确显示，变化时高亮 |
| 高电量警告 | charge_limit_soc > 90% 时显示黄色警告 |
| Web 状态统一 | 删除 App.tsx useState，统一用 Zustand |
| Web 页面补全 | 8 个页面有实际交互 |
| iOS 模型 | 编译通过，数据正确解析 |
| 下拉刷新 | 三端支持下拉刷新 |
| 车辆切换 | Modal 正确显示和切换 |
