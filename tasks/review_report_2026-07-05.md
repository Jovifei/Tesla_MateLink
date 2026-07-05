# app_mimo 审查报告 — Stitch "简约白" 1:1 实现

> **日期**: 2026-07-05
> **审查范围**: `app_mimo/android` + `app_mimo/ios` 全部源码
> **审查基准**: Stitch 项目 `11493757920836657212` (MateLink - Tesla 监控 简约白) + PRD `MateLink_Stitch_Swiss_PRD_2026-07-05.md` + `MateLink_UI_PRD.md`
> **审查方式**: 5 维度并行审查 (完整性 / Android Bug / iOS Bug / UI 规范 / 数据层)

---

## 一、总评

| 维度 | Android | iOS | 总计 |
|------|---------|-----|------|
| **页面覆盖率** | 13/22 (59%) | 16/22 (73%) | — |
| **编译阻断 Bug** | 0 confirmed | 2 Critical | **2** |
| **代码 Bug (Critical)** | 1 | 2 | **3** |
| **代码 Bug (High)** | 4 | 4 | **8** |
| **代码 Bug (Medium)** | 7 | 5 | **12** |
| **代码 Bug (Low)** | 5 | 3 | **8** |
| **UI 规范偏差** | 10 | 8 | **18** |
| **数据层问题** | 4 | 8 | **12** |

**综合结论**: Android 端功能覆盖更广但 UI 规范偏差大；iOS 端页面更多但存在编译阻断和 API 模型不兼容问题。两端均未达到 Stitch "简约白" 的 1:1 还原。

---

## 二、页面完整性

### Android 缺失页面 (6 项)

| 缺失页面 | PRD 层级 | 影响 |
|----------|---------|------|
| **更多菜单页** | L1 | 第4个 Tab 直接跳 Settings，用户无法到达分析类页面 |
| **效率分析** | L2 | 无文件 |
| **成本分析** | L2 | TariffConfigScreen 是电价设置，不是成本分析 |
| **续航分析** | L2 | 无文件 |
| **待机耗电** | L2 | 无文件 |
| **时间线** | L2 | TripsScreen 是行程管理，不是 24h 活动时间线 |
| **关于页** | L2 | 无文件 |

### iOS 缺失页面 (3 项)

| 缺失页面 | PRD 层级 | 影响 |
|----------|---------|------|
| **当前充电** | L2 | 无实时充电监控页面 |
| **哨兵历史** | L2 | 无文件 |
| **里程钻取** | L2 | 无独立页面 (StatisticsView 部分覆盖) |

### iOS 半功能页面 (2 项)

| 页面 | 问题 |
|------|------|
| **时间线** | UI 完整但硬编码 MockAPI，真实模式返回空 |
| **固件版本** | UI 完整但真实模式 `updates = []` |

### 中文本地化

- **Android**: 13/14 页面使用 `stringResource()` + `values-zh`，基本到位
- **iOS**: 几乎全部英文，无本地化基础设施

---

## 三、Critical Bug (必须修复)

### C-1. iOS ChargeDetailView 编译失败
- **文件**: `ios/MateLink/Features/Charges/ChargeDetailView.swift`
- **问题**: 引用 `charge.chargeType`、`charge.chargeEnergyUsed`、`charge.fastChargerBrand`、`charge.fastChargerType` — 均不存在于 `Charge` 结构体
- **影响**: 该文件无法编译

### C-2. iOS ChargeDetailView Preview 编译失败
- **文件**: 同上 (Preview 块)
- **问题**: Preview 构造 Charge 使用不存在的字段

### C-3. iOS 所有数据模型与 TeslaMate API 不兼容 (4 个模型)
- **文件**: `ios/MateLink/Core/Models/CarStatus.swift`
- **问题**: iOS 的 `CarStatus`/`Drive`/`Charge`/`BatteryHealth` 全部使用**扁平 JSON 结构**，而 TeslaMate API v1.24+ 返回**嵌套结构**
- **影响**: iOS 连接真实服务器时，所有 API 响应解析失败
- **对比**: Android 的 `api/models/CarModels.kt` 正确建模了嵌套结构

### C-4. Android DashboardViewModel 空指针崩溃链
- **文件**: `android/.../dashboard/DashboardViewModel.kt` L40-43
- **问题**: `apiClient.api.getCars().data.cars` — `data` 可空，直接 `.cars` 会 NPE
- **严重度**: Critical — 生产环境最高频崩溃点

---

## 四、High Bug (高优先级)

| # | 平台 | 文件 | 问题 |
|---|------|------|------|
| H-1 | iOS | `OnboardingView.swift` | Token 未写入 Keychain，重启后丢失 |
| H-2 | iOS | `AppState.swift` | `loadCars()` 始终用 mock 数据，真实模式无效 |
| H-3 | iOS | `UpdatesView.swift` | 真实模式返回空数组 |
| H-4 | iOS | `ApiClient.swift` | 缺少 URL 安全校验，HTTP 明文传 Token |
| H-5 | iOS | `ApiClient.swift` | 缺少备用服务器降级机制 |
| H-6 | Android | `ApiClient.kt` | `cachedApi` 线程不安全，无同步 |
| H-7 | Android | `DashboardScreen.kt` | 可空字段直接用于算术/条件判断，编译或崩溃 |
| H-8 | Android | `NetworkModule.kt` | Basic + Bearer 双 Authorization Header 冲突 |

---

## 五、Medium Bug (12 项)

| # | 平台 | 文件 | 问题 |
|---|------|------|------|
| M-1 | iOS | `DriveDetailView.swift` | 假 maxSpeed = avgSpeed × 1.5，忽略真实 `speedMax` |
| M-2 | iOS | `DashboardView.swift` | Timer 永不停止，内存泄漏 |
| M-3 | iOS | `RangeView.swift` | accuracy 计算恒为 0% (数学恒等式) |
| M-4 | iOS | `DashboardView.swift` | BatteryTrendCard 硬编码数据 |
| M-5 | Android | `SettingsRepository.kt` | MockMode 默认 true，新用户看到假数据无提示 |
| M-6 | Android | `ChargingNotificationWorker.kt` | N+1 次冗余 API 调用 |
| M-7 | Android | `StatsRepository.kt` | `LocalDate.parse()` 无异常保护，日期格式错误即崩 |
| M-8 | Android | `BatteryViewModel.kt` | 1% 电量外推 100% 续航，误差放大 100 倍 |
| M-9 | Android | `ChargingMonitorService.kt` | `mutableSetOf` 非线程安全 |
| M-10 | Android | `DashboardViewModel.kt` | 吞掉所有异常含 `CancellationException` |
| M-11 | 两端 | — | Mock 数据结构不一致 (Android 嵌套 / iOS 扁平) |
| M-12 | Android | `TariffConfig.kt` | 分时电价线性假设，DC 快充误差大 |

---

## 六、UI 规范偏差 (Precision Minimalist 主题)

### 两端共同问题

| 偏差 | 规范要求 | 实际 | 严重度 |
|------|---------|------|--------|
| **字体** | Inter + JetBrains Mono | 系统默认 (Roboto / SF Pro) | HIGH |
| **数字字体特性** | tabular-nums 对齐 | 无 tnum 设置 | MEDIUM |
| **强调色** | 金色 `#A16207` | 完全缺失 | HIGH |
| **状态色** | 绿 `#059669` / 橙 `#F59E0B` / 红 `#DC2626` | Material / System 默认色 | HIGH |

### Android 特有

| 偏差 | 严重度 |
|------|--------|
| ElevatedCard 有阴影 (规范禁止阴影) | HIGH |
| 圆角 12dp (规范 8px) | MEDIUM |
| DashboardScreen 硬编码第三套颜色 | HIGH |
| 无 1px 边框 | MEDIUM |

### iOS 特有

| 偏差 | 严重度 |
|------|--------|
| `.regularMaterial` 背景 (规范要求纯白) | HIGH |
| 圆角 16 (规范 8) | MEDIUM |
| 无卡片边框 | MEDIUM |
| StatCard 用蓝色 (规范无蓝色) | MEDIUM |

---

## 七、数据层问题

### iOS 模型不兼容 (致命)

iOS 全部 4 个核心模型 (`CarStatus`/`Drive`/`Charge`/`BatteryHealth`) 的 JSON CodingKeys 使用扁平字段名，与 TeslaMate API v1.24+ 的嵌套响应不匹配。**连接真实服务器时 iOS 将完全无法工作**。

### 其他数据层问题

| # | 问题 | 严重度 |
|---|------|--------|
| iOS 缺少 7 个 API 端点 vs Android | HIGH |
| iOS JSONDecoder 无日期策略配置 | LOW |
| Android 域模型 `data/model/` 为死代码 | LOW |
| Android 401/403 处理为空操作 (TODO) | MEDIUM |
| 两端 Mock 数据结构不一致 | MEDIUM |

---

## 八、修复优先级建议

### P0 — 阻断级 (不修无法用)

1. **iOS ChargeDetailView** — 补齐 Charge 结构体缺失字段或改写 View
2. **iOS 数据模型** — 对齐 TeslaMate API 嵌套结构 (CarStatus/Drive/Charge/BatteryHealth)
3. **Android DashboardViewModel** — 空安全链 `?.data?.cars ?: emptyList()`

### P1 — 功能级 (核心功能缺失)

4. **iOS Onboarding Token** — 写入 Keychain
5. **iOS loadCars()** — 真实模式分支
6. **iOS UpdatesView** — 真实 API 调用
7. **Android More 菜单页** — 创建分析入口集散页
8. **Android 缺失 5 个分析页面** — Efficiency / Cost / Range / Vampire / Timeline
9. **iOS 缺失 3 个页面** — CurrentCharge / Sentry / Mileage

### P2 — 规范级 (视觉还原)

10. **两端字体** — 打包 Inter + JetBrains Mono
11. **两端状态色** — 替换为 Stitch 规范色
12. **两端强调色** — 添加金色 `#A16207`
13. **Android 卡片** — ElevatedCard → OutlinedCard，圆角 8，去阴影
14. **iOS 卡片** — 去 material 背景，圆角 8，加边框

### P3 — 质量级 (稳定性)

15. Android 线程安全修复 (ApiClient / Service)
16. iOS Timer 泄漏修复
17. iOS RangeView accuracy 逻辑重写
18. Android 异常处理改进

---

*报告由 5 个并行审查代理生成，基于实际源码逐文件审读。未改动任何代码。*
