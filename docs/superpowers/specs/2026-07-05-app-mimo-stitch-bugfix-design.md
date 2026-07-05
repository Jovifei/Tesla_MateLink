# app_mimo Stitch "简约白" Bug 修复计划设计

> **日期**: 2026-07-05
> **基准分支**: `codex/app-mimo-stitch-1to1`
> **目标**: 修复审查报告 `tasks/review_report_2026-07-05.md` 中的 P0+P1 级问题，使 app_mimo 达到 Stitch "简约白" 功能完整性
> **参考**: `docs/git_ref/mimo/` Android 嵌套 API 模型 + 完整分析页面

---

## 1. 整体架构

三阶段串行，平台内并行：

```
Phase 1: 数据层对齐 (5 项)
  ├─ iOS 模型嵌套化 (CarStatus/Drive/Charge/BatteryHealth)
  ├─ iOS ApiClient 补齐 7 端点 + URL 安全校验
  ├─ Android DashboardViewModel 空安全
  └─ Android NetworkModule Auth 冲突修复

Phase 2: 页面补全 (9 项)
  ├─ Android: 5 个分析页面 (效率/成本/续航/待机/时间线)
  ├─ iOS: 3 个壳页面接真实数据
  └─ 两端: 中文化

Phase 3: Bug 收尾 (13 项)
  ├─ iOS: maxSpeed/accuracy/timer/BatteryTrend/widget
  └─ Android: 线程安全/异常处理/MockMode/日期解析/外推
```

---

## 2. Phase 1 — 数据层对齐

### 2.1 iOS 模型嵌套化

**问题**: iOS 的 `CarStatus`/`Drive`/`Charge`/`BatteryHealth` 使用扁平 JSON CodingKeys，TeslaMate API v1.24+ 返回嵌套结构。连接真实服务器时所有响应解析失败。

**方案**: 重写 4 个模型的 CodingKeys 和嵌套类型，参考 `docs/git_ref/mimo/` Android 的 `api/models/CarModels.kt`。

**CarStatus 嵌套映射**:
```
battery_details.battery_level      → batteryLevel
battery_details.usable_battery_level → usableBatteryLevel
charging_details.charge_limit_soc  → chargeLimitSOC
charging_details.charger_power     → chargerPower
tpms_details.tpms_pressure_fl      → tirePressureFrontLeft
odometer_details.odometer          → odometer
climate_details.inside_temp        → insideTemp
vehicle_details.state              → state (online/asleep/driving/charging)
```

**Drive 嵌套映射**:
```
odometer_details.distance          → distanceKm
battery_details.start_battery_level → startBatteryLevel
range_rated.start_range            → startRatedRangeKm
positions.start_position.latitude  → startLatitude (仅详情端点)
```

**Charge 嵌套映射**:
```
charger_phases                     → chargingType (0=DC, 1/3=AC)
battery_details.start_battery_level → startBatteryLevel
```

**BatteryHealth 嵌套映射**:
```
max_range                          → ratedRangeKm
current_range                      → idealRangeKm
battery_health_percentage          → healthPercentage
```

**兼容策略**: 保留现有 `Charge` extension 兼容别名 (`chargeType`/`chargeEnergyUsed`/`fastChargerBrand`/`fastChargerType`)，避免改模型炸所有 View。

### 2.2 iOS ApiClient 补全

补齐 7 个端点（与 Android `TeslamateApi.kt` 对齐）:

| 端点 | 方法 | 返回类型 |
|------|------|---------|
| `/api/v1/cars/{id}` | GET | `CarApiResponse` |
| `/api/v1/cars/{id}/charges/current` | GET | `Charge?` |
| `/api/v1/cars/{id}/charges/{chargeId}` | GET | `Charge` |
| `/api/v1/cars/{id}/drives/{driveId}` | GET | `Drive` |
| `/api/v1/cars/{id}/battery` | GET | `BatteryHealth` |
| `/api/v1/cars/{id}/updates` | GET | `[UpdateItem]` |
| `/api/v1/settings` | GET | `GlobalSettings` |

### 2.3 iOS URL 安全校验

新增 `UrlSecurity.swift`:
- 验证 URL 格式合法性
- 拒绝向公网 IP/域名发送 HTTP（非 HTTPS）Bearer Token
- 允许 localhost/192.168.x.x/10.x.x.x HTTP（自托管场景）

### 2.4 Android 空安全修复

**文件**: `DashboardViewModel.kt` L40-43

```kotlin
// Before:
val cars = apiClient.api.getCars().data.cars
val status = apiClient.api.getCarStatus(carId).data

// After:
val response = apiClient.api.getCars()
val cars = response.body()?.data?.cars ?: emptyList()
val statusResponse = apiClient.api.getCarStatus(carId)
val status = statusResponse.body()?.data
```

### 2.5 Android Auth 冲突修复

**文件**: `NetworkModule.kt` L173-183

```kotlin
// Before: 两个 if，可能发送两个 Authorization header
// After: if/else 互斥，Bearer 优先
if (apiToken.isNotBlank()) {
    requestBuilder.addHeader("Authorization", "Bearer $apiToken")
} else if (basicAuthUsername.isNotBlank() && basicAuthPassword.isNotBlank()) {
    requestBuilder.addHeader("Authorization", Credentials.basic(basicAuthUsername, basicAuthPassword))
}
```

---

## 3. Phase 2 — 页面补全

### 3.1 Android 缺失页面 (5 个)

每个页面需要：ViewModel + Screen + NavGraph 路由注册 + strings.xml 中文

| 页面 | 数据源 | UI 组件 |
|------|--------|---------|
| **效率分析 EfficiencyScreen** | Drive 历史 | 散点图 (速度 vs 能耗) + 速度区间分布 |
| **成本分析 CostScreen** | Charge 历史 + TariffConfig | 月度堆叠柱状图 (AC/DC) + 位置费用排行 |
| **续航分析 RangeScreen** | Drive + CarStatus | 预估 vs 实际偏差折线 + 影响因素卡片 |
| **待机耗电 VampireScreen** | CarStatus 时序 | 空闲损耗功率 + 每日柱状图 + 原因分类 |
| **时间线 TimelineScreen** | Drive + Charge 事件 | 24h 横向时间轴 + 事件类型图标 |

**路由**: NavGraph.kt 添加 5 个新路由，MoreScreen 的入口链接已存在。

### 3.2 iOS 页面接真实数据 (3 个)

| 页面 | 当前状态 | 修复方案 |
|------|---------|---------|
| **CurrentChargeView** | 壳子已存在，用 CarStatus | 验证 `/charges/current` 端点，fallback 到 CarStatus |
| **SentryHistoryView** | 壳子已存在，用 mock | 标注 "endpoint unavailable"，保留 mock 展示 |
| **MileageView** | 壳子已存在，用 Drive 数据 | 从 Drive 历史聚合月度里程 + BatteryHealth 里程表 |

### 3.3 中文化

**Android**:
- `values-zh/strings.xml` 补齐新页面字符串 (效率分析/成本分析/续航分析/待机耗电/时间线/关于)
- 预计新增约 40 个中文字符串

**iOS**:
- 创建 `Localizable.strings` (zh-Hans)
- 覆盖: Tab 标签 (4)、页面标题 (15)、状态文本 (20)、按钮文案 (10)
- 在 `Localization.swift` 中确保 `L10n.string()` 正确加载

---

## 4. Phase 3 — Bug 收尾

### 4.1 iOS Bug 修复 (6 项)

| Bug | 文件 | 修复 |
|-----|------|------|
| 假 maxSpeed | DriveDetailView.swift L131 | `Int(drive.speedMax)` 替换 `avgSpeed * 1.5` |
| accuracy 恒 0% | RangeView.swift L27-31 | 重写：`(1 - abs(rated - actual) / rated) * 100` |
| BatteryTrendCard 硬编码 | DashboardView.swift L192 | 接 CarStatus 历史或标注 "Demo" |
| Timer 泄漏 | DashboardView.swift L8 | `@State var timerCancellable` + onAppear/onDisappear |
| widget Double→Int | DashboardView.swift L114-117 | 统一用 `Int()` 转换 |
| Dashboard 状态色 | DashboardView.swift | 替换为 Stitch 规范色 #059669/#F59E0B/#DC2626 |

### 4.2 Android Bug 修复 (7 项)

| Bug | 文件 | 修复 |
|-----|------|------|
| ApiClient 线程安全 | ApiClient.kt L30-64 | `@Volatile` + `synchronized` |
| DashboardScreen 可空 | DashboardScreen.kt L88-152 | 映射为非空 UI 模型 |
| MockMode 默认 true | SettingsRepository.kt L33 | 改为 `false` |
| 日期解析崩溃 | StatsRepository.kt L706 | `runCatching { LocalDate.parse(...) }` |
| 1% 外推 | BatteryViewModel.kt L165 | 加 `batteryLevel >= 10` 阈值 |
| Service 线程安全 | ChargingMonitorService.kt L67 | `ConcurrentHashMap.newKeySet()` |
| 异常吞没 | DashboardViewModel.kt L59-71 | 重新抛出 `CancellationException` |

---

## 5. 验证策略

### Phase 1 验证
- iOS: `CarStatus`/`Drive`/`Charge`/`BatteryHealth` 模型能正确 decode 嵌套 JSON 测试向量
- iOS: ApiClient 所有端点有方法签名
- Android: DashboardViewModel 不再 NPE（空数据测试）
- Android: 只发送一个 Authorization header

### Phase 2 验证
- Android: 5 个新页面有 Screen 文件 + NavGraph 路由 + strings.xml
- iOS: CurrentChargeView/MileageView 能从 API 拉数据
- 两端: Tab 标签和页面标题显示中文

### Phase 3 验证
- iOS: DriveDetailView 显示真实 maxSpeed
- iOS: RangeView accuracy 不为 0%
- Android: MockMode 默认 false
- Android: Stats 页面不因日期格式崩溃

---

## 6. 不在范围内

- UI 规范偏差 (字体/颜色/卡片样式) — 推迟到 P2 修复轮
- Low 级 Bug (死代码清理/DataStore 命名等)
- Widget target/entitlements — 推迟到 Mac 环境
- Xcode 真编译验证 — 需要 Mac

---

## 7. 预计工作量

| Phase | 项数 | 预估 |
|-------|------|------|
| Phase 1 | 5 | 数据层最关键，iOS 模型重写耗时最长 |
| Phase 2 | 9 | Android 页面可参考 git_ref，iOS 壳子已存在 |
| Phase 3 | 13 | 多数是小修复，批量处理 |
| **总计** | **27** | |
