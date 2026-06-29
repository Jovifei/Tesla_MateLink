# Comet Design Handoff

- Change: glm-review-fix-critical
- Phase: design
- Mode: compact
- Context hash: 5e7194845707489099d0ef5120e0234288ce8df3b10c29a759cb6e1656bc8197

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-review-fix-critical/proposal.md

- Source: openspec/changes/glm-review-fix-critical/proposal.md
- Lines: 1-47
- SHA256: f0af7afa9acf9f15ebdc9c47dca3d4057ace2c447489171f460e2ec6156f2412

```md
## Why

第7轮交叉审核（3 个 code-reviewer agent 并行审查 iOS 29 Swift + Android 95+ Kotlin）发现 66 个问题，其中 22 个为停船级（8 BLOCKER 编译错误 + 14 CRITICAL 安全/崩溃/数据丢失风险）。当前代码无法编译通过、存在 token 泄露和数据库破坏性迁移风险。必须在继续功能开发前修复。

## What Changes

### iOS 修复（4 项）
- **API URL 构造** — 修复 `ApiClient.swift` 中缺失 `/` 导致 drive/charge/onboarding 端点 404
- **MockData 优雅降级** — 移除 `fatalError` + `try!`，mock_data.json 缺失时不再崩溃
- **ISO 8601 日期解析** — 统一 7 处重复的日期解析逻辑，支持小数秒格式
- **Settings 连接测试反馈** — 空 catch 改为用户可见的错误提示

### Android 修复（9 项）
- **编译错误修复** — `parseDateTime`→`parseIsoDateTime`、`Trip` 类型匹配、`Card`/`CardDefaults` import
- **Room 数据库安全** — 移除 `fallbackToDestructiveMigration()`，防止版本升级时清空用户数据
- **HTTP 日志安全** — `HttpLoggingInterceptor.Level.BODY` 限制为 DEBUG only
- **暗色主题接线** — `MateLinkTheme(darkTheme = AppSettings.isDarkTheme)` 实际生效
- **Onboarding HTTP 配置** — 复用共享 `OkHttpClient` 而非裸 HTTP 请求
- **Retrofit baseUrl 运行时覆盖** — 支持用户配置的服务器地址
- **Widget 假数据替换** — `CarWidgetUpdateWorker` 从 `StatusRepository` 读取真实数据

### 跨平台修复（4 项）
- **Android Dashboard 地图** — 替换 🗺️ emoji 占位为实际地图组件（OSM/Google Maps）
- **Android Statistics 下钻** — 实现 Year→Month→Day→DriveDetail 导航
- **Android GCJ-02 坐标转换** — 移植 iOS `GCJ02Converter.swift` 逻辑到 Kotlin
- **计划文档修正** — 更正 `glm_P1实施计划.md` 中 G-01/G-02/G-03/F-107 的错误标记

### 计划文档（1 项）
- 修正 `docs/glm_P1实施计划.md`：移除 G-01/G-02 误标、G-03 缩小为仅 Android、F-107 标记 Android 缺口

## Capabilities

### New Capabilities
- `api-url-fix`: 统一的 API URL 构造逻辑，确保所有端点正确拼接
- `android-gcj02`: Android GCJ-02 坐标转换器（WGS-84 ↔ GCJ-02）
- `android-map-dashboard`: Android Dashboard 地图集成
- `android-stats-drilldown`: Android Statistics Year→Month→Day 下钻导航

### Modified Capabilities
无 — 本次修复不改变现有 spec 的行为约定，仅修复实现缺陷。

## Impact

- **iOS**: `ApiClient.swift`, `OnboardingView.swift`, `DriveListView.swift`, `ChargeListView.swift`, `UpdatesView.swift`, `SettingsView.swift`, `BatteryHealthView.swift`, `StatisticsView.swift` 等 8 个分析页面
- **Android**: `TripDetector.kt`, `TripAggregator.kt`, `DashboardScreen.kt`, `AppModule.kt`, `Theme.kt`, `OnboardingViewModel.kt`, `CarWidgetUpdateWorker.kt`, `UnitFormatter.kt`, `StatisticsScreen.kt` 等 20+ 文件
- **Docs**: `docs/glm_P1实施计划.md`
- **新增文件**: `Core/Utils/DateFormatter+ISO8601.swift`, `domain/GCJ02Converter.kt`, `ui/statistics/MonthDetailScreen.kt`, `ui/statistics/DayDetailScreen.kt`
```

## openspec/changes/glm-review-fix-critical/design.md

- Source: openspec/changes/glm-review-fix-critical/design.md
- Lines: 1-145
- SHA256: ad39e87760a0b85c638e0884279b4ba008c02d9465ea6792866ad44275357665

[TRUNCATED]

```md
## Context

Tesla_MateLink 当前 iOS 29 Swift + Android 95+ Kotlin 代码库经第7轮交叉审核发现 22 个停船级缺陷。iOS 端 API URL 拼接错误导致 drive/charge/onboarding 全部 404；Android 端 3 处编译错误阻止构建；双端均存在安全漏洞（token 泄露、数据破坏性迁移）。修复必须在功能开发前完成。

**约束：**
- iOS: Swift 5.10 + SwiftUI + Swift Charts + WidgetKit + MapKit
- Android: Kotlin 2.0 + Jetpack Compose + Material 3 + Hilt + Retrofit + Room v12
- API: TeslaMateApi v1.21+ (16 endpoints)
- 不引入新的第三方依赖（iOS 已有 MapKit，Android 地图用 OSM osmdroid 或 Google Maps）

## Goals / Non-Goals

**Goals:**
1. iOS ApiClient URL 构造在所有调用路径正确
2. Android 3 处编译错误全部修复
3. 安全漏洞修复：token 保护、数据库迁移安全、HTTP 日志降级
4. Android 暗色主题 toggle 实际生效
5. Android Dashboard 集成真实地图
6. Android Statistics 实现下钻导航
7. Android GCJ-02 坐标转换器
8. iOS MockData 优雅降级
9. iOS ISO 8601 日期解析统一
10. 计划文档修正

**Non-Goals:**
- 44 个 HIGH/MEDIUM/LOW 问题 → `glm-review-fix-quality`
- 新功能开发
- Web 平台修改
- 单元测试编写（当前项目无测试基础设施）

## Decisions

### D-1: iOS URL 构造 — 防御式修复在 ApiClient 内部

**选择：** 在 `TeslaMateAPI.fetch()` 和 `checkStatus()` 中统一处理 path 的前导 `/`

```swift
// 修复前：URL(string: "\(baseURL)\(path)")
// → "http://hostapi/v1/cars/1/drives" (错误)

// 修复后：
let normalizedPath = path.hasPrefix("/") ? path : "/\(path)"
guard let url = URL(string: "\(baseURL)\(normalizedPath)")
```

**替代方案：** 修改每个调用点统一加 `/` — 拒绝，因为跨多个文件且易再次出错。防御式修复在单点解决问题。

### D-2: Android parseDateTime → parseIsoDateTime

**选择：** 添加私有别名函数，不改动调用点

```kotlin
private fun parseDateTime(s: String?): Instant? = parseIsoDateTime(s)
```

**替代方案：** 逐一替换 8 个调用点 — 拒绝，风险高且纯机械替换。

### D-3: Trip 类型不匹配 — 添加映射扩展

**选择：** 为 `DriveEntity`/`ChargeEntity` 添加 `toSummary()` 扩展函数，在 `TripAggregator.buildTrip()` 中调用映射

```kotlin
fun DriveEntity.toSummary(): DriveSummary = DriveSummary(...)
fun ChargeEntity.toSummary(): ChargeSummary = ChargeSummary(...)
```

### D-4: Room destructive migration — 移除 fallback + 确保迁移链完整

**选择：** 直接移除 `.fallbackToDestructiveMigration()`。当前 11 个 MIGRATION_1_2 ~ MIGRATION_11_12 已声明但未被引用 — 同时修复 `ALL_MIGRATIONS` 列表确保完整覆盖。

### D-5: HTTP 日志安全 — 构建类型检查

**选择：** 使用 `BuildConfig.DEBUG` 控制日志级别

```kotlin
val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY 
            else HttpLoggingInterceptor.Level.NONE
```

### D-6: 暗色主题 — 统一 AppSettings 并接线
```

Full source: openspec/changes/glm-review-fix-critical/design.md

## openspec/changes/glm-review-fix-critical/tasks.md

- Source: openspec/changes/glm-review-fix-critical/tasks.md
- Lines: 1-41
- SHA256: b35d30a901f6d4f52b98dd14ff09d6b923a0b3ff34a49b0fdb525c22fb21ddd3

```md
## 1. iOS BLOCKER 修复

- [ ] 1.1 修复 `ApiClient.swift` URL 构造：在 `fetch()` 和 `checkStatus()` 中添加 path 前导 `/` 规范化
- [ ] 1.2 修复 `OnboardingView.swift` — ping/readyz 调用点同步验证（B1 关联）
- [ ] 1.3 修复 `DriveListView.swift` — drives API 调用验证
- [ ] 1.4 修复 `ChargeListView.swift` — charges API 调用验证

## 2. Android BLOCKER 修复

- [ ] 2.1 修复 `TripDetector.kt` — `parseDateTime` → `parseIsoDateTime`
- [ ] 2.2 修复 `Trip.kt` / `TripAggregator.kt` — 添加 Entity→Summary 映射，修复类型不匹配
- [ ] 2.3 修复 `DashboardScreen.kt` — 添加 `Card`/`CardDefaults` import

## 3. Android CRITICAL 安全修复

- [ ] 3.1 移除 `AppModule.kt` 中 `fallbackToDestructiveMigration()`，确保 `ALL_MIGRATIONS` 完整覆盖 v1→v12
- [ ] 3.2 修复 `AppModule.kt` HTTP 日志：`HttpLoggingInterceptor.Level.BODY` 限制为 `BuildConfig.DEBUG` only
- [ ] 3.3 合并 `SettingsScreen.AppSettings` 和 `SettingsDataStore.AppSettings` 为单一 DataStore 源
- [ ] 3.4 修复 `Theme.kt` — `MateLinkTheme(darkTheme = AppSettings.isDarkTheme)` 实际生效
- [ ] 3.5 修复 `OnboardingViewModel.kt` — 注入共享 `OkHttpClient`/`Retrofit` 替代裸 HTTP
- [ ] 3.6 修复 `AppModule.kt` Retrofit `baseUrl` — 支持运行时从 DataStore 动态读取

## 4. iOS CRITICAL 修复

- [ ] 4.1 修复 `MockData.load()` — `fatalError` + `try!` 改为 throwing + 优雅降级
- [ ] 4.2 创建 `Core/Utils/ISO8601Parser.swift` — 统一两遍 ISO 8601 解析（小数秒+无小数秒）
- [ ] 4.3 替换 7 处重复日期解析为 `ISO8601Parser.parse()`：DriveDetailView、ChargeDetailView、StatisticsView、TimelineView、HeatmapView、BatteryHealthView、UpdatesView
- [ ] 4.4 修复 `UpdatesView.isoDate` — 使用 `ISO8601Parser` 替代默认格式，fallback 改为 `Date.distantPast`
- [ ] 4.5 修复 `SettingsView.swift` "Test Connection" — 空 catch 改为用户可见的错误提示

## 5. 跨平台 BLOCKER 修复

- [ ] 5.1 Android Dashboard 集成 osmdroid：添加依赖，实现 `LocationCard` 地图展示
- [ ] 5.2 Android 实现 `GCJ02Converter.kt` — 移植 iOS eviltransform 算法
- [ ] 5.3 Android Statistics 下钻：创建 `MonthDetailScreen.kt` + `DayDetailScreen.kt`
- [ ] 5.4 Android `NavGraph.kt` — 注册 Statistics 下钻路由
- [ ] 5.5 Android `StatisticsScreen.kt` — 接入 CarRepository 数据加载 + 点击导航

## 6. 文档修正

- [ ] 6.1 修正 `docs/glm_P1实施计划.md`：移除 G-01/G-02、G-03 缩小为仅 Android、F-107 标记 Android 缺口
```

## openspec/changes/glm-review-fix-critical/specs/android-gcj02/spec.md

- Source: openspec/changes/glm-review-fix-critical/specs/android-gcj02/spec.md
- Lines: 1-16
- SHA256: 5a36a145e206784df4f547ff532ebaa649ac1df3edc0e0c4116f08b8941315b4

```md
## ADDED Requirements

### Requirement: WGS-84 到 GCJ-02 坐标转换
系统 SHALL 提供 `GCJ02Converter` 对象，支持 WGS-84 ↔ GCJ-02 双向坐标转换，包含中国境内判断。

#### Scenario: 中国境内坐标转换
- **WHEN** 输入 WGS-84 坐标 (39.9087, 116.3975)（北京）
- **THEN** `wgs84ToGcj02()` 返回偏移后的 GCJ-02 坐标，偏移量 ~100-700m

#### Scenario: 中国境外坐标不变
- **WHEN** 输入 WGS-84 坐标 (48.8566, 2.3522)（巴黎）
- **THEN** `wgs84ToGcj02()` 返回原坐标无偏移

#### Scenario: GCJ-02 反算 WGS-84
- **WHEN** 输入 GCJ-02 坐标
- **THEN** `gcj02ToWgs84()` 返回对应的 WGS-84 坐标，精度 < 0.5m
```

## openspec/changes/glm-review-fix-critical/specs/android-map-dashboard/spec.md

- Source: openspec/changes/glm-review-fix-critical/specs/android-map-dashboard/spec.md
- Lines: 1-16
- SHA256: d343642dba9a08cd2a744121923e5e79802b5d20970b44d926dd347bdba4a7ab

```md
## ADDED Requirements

### Requirement: Dashboard 地图展示
Android Dashboard SHALL 在 LocationCard 中展示实际地图，标记车辆当前位置。

#### Scenario: 有位置数据时显示地图
- **WHEN** Dashboard 加载车辆状态且 `latitude`/`longitude` 非空
- **THEN** LocationCard 展示 OpenStreetMap 地图，车辆位置标记为 marker

#### Scenario: 无位置数据时显示占位
- **WHEN** 车辆状态中 latitude 或 longitude 为空
- **THEN** LocationCard 展示 "No location data" 文字提示，不显示空白地图

#### Scenario: 点击地图跳转
- **WHEN** 用户点击 LocationCard 地图
- **THEN** 导航到全屏地图视图（预留路由）
```

## openspec/changes/glm-review-fix-critical/specs/android-stats-drilldown/spec.md

- Source: openspec/changes/glm-review-fix-critical/specs/android-stats-drilldown/spec.md
- Lines: 1-20
- SHA256: 58cfebfbbf9418112386ef1dd6bf9c21cdbb7ee70d5310a619309ea37a75e56a

```md
## ADDED Requirements

### Requirement: Statistics 三级下钻导航
Android Statistics SHALL 支持 Year → Month → Day → DriveDetail 四级 NavigationStack 导航。

#### Scenario: 年份网格展示
- **WHEN** 用户进入 Statistics 页面
- **THEN** 展示当前年份的 12 个月网格，每月显示行程次数、总里程

#### Scenario: 点击月份进入日视图
- **WHEN** 用户点击某个月份卡片
- **THEN** 导航到该月的日视图，显示每天行程摘要列表

#### Scenario: 点击日期进入行程详情
- **WHEN** 用户点击某个日期的行程
- **THEN** 导航到 DriveDetailScreen，传入 driveId

#### Scenario: 数据加载
- **WHEN** Statistics 页面初始化
- **THEN** 从 CarRepository 加载当年所有 drives 数据并聚合
```

## openspec/changes/glm-review-fix-critical/specs/api-url-fix/spec.md

- Source: openspec/changes/glm-review-fix-critical/specs/api-url-fix/spec.md
- Lines: 1-16
- SHA256: f3856aacd8f2e6d2b3d0ac059aa111c4af8941aca155326c64feb1dff43def2a

```md
## ADDED Requirements

### Requirement: API URL 路径规范化
TeslaMateAPI SHALL 自动处理 API path 的前导 `/`，确保所有调用方无论传入 `"api/v1/cars/1/drives"` 还是 `"/api/v1/cars/1/drives"` 都能正确构造 URL。

#### Scenario: 不带前导斜杠的路径
- **WHEN** 调用 `fetch("api/v1/cars/1/drives")`
- **THEN** 构造的 URL 为 `{baseURL}/api/v1/cars/1/drives`

#### Scenario: 带前导斜杠的路径
- **WHEN** 调用 `fetch("/api/v1/cars/1/status")`
- **THEN** 构造的 URL 为 `{baseURL}/api/v1/cars/1/status`

#### Scenario: checkStatus 路径规范化
- **WHEN** 调用 `checkStatus("api/ping")`
- **THEN** 构造的 URL 为 `{baseURL}/api/ping`
```

