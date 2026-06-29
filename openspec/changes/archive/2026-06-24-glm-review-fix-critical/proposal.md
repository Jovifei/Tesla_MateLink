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
