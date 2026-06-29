## 1. iOS BLOCKER 修复

- [x] 1.1 修复 `ApiClient.swift` URL 构造：在 `fetch()` 和 `checkStatus()` 中添加 path 前导 `/` 规范化
- [x] 1.2 修复 `OnboardingView.swift` — ping/readyz 调用点同步验证（B1 关联）
- [x] 1.3 修复 `DriveListView.swift` — drives API 调用验证
- [x] 1.4 修复 `ChargeListView.swift` — charges API 调用验证

## 2. Android BLOCKER 修复

- [x] 2.1 修复 `TripDetector.kt` — `parseDateTime` → `parseIsoDateTime`
- [x] 2.2 修复 `Trip.kt` / `TripAggregator.kt` — 添加 Entity→Summary 映射，修复类型不匹配
- [x] 2.3 修复 `DashboardScreen.kt` — 添加 `Card`/`CardDefaults` import

## 3. Android CRITICAL 安全修复

- [x] 3.1 移除 `AppModule.kt` 中 `fallbackToDestructiveMigration()`，确保 `ALL_MIGRATIONS` 完整覆盖 v1→v12
- [x] 3.2 修复 `AppModule.kt` HTTP 日志：`HttpLoggingInterceptor.Level.BODY` 限制为 `BuildConfig.DEBUG` only
- [x] 3.3 合并 `SettingsScreen.AppSettings` 和 `SettingsDataStore.AppSettings` 为单一 DataStore 源
- [x] 3.4 修复 `Theme.kt` — `MateLinkTheme(darkTheme = AppSettings.isDarkTheme)` 实际生效
- [x] 3.5 修复 `OnboardingViewModel.kt` — 注入共享 `OkHttpClient`/`Retrofit` 替代裸 HTTP
- [x] 3.6 修复 `AppModule.kt` Retrofit `baseUrl` — 支持运行时从 DataStore 动态读取

## 4. iOS CRITICAL 修复

- [x] 4.1 修复 `MockData.load()` — `fatalError` + `try!` 改为 throwing + 优雅降级
- [x] 4.2 创建 `Core/Utils/ISO8601Parser.swift` — 统一两遍 ISO 8601 解析（小数秒+无小数秒）
- [x] 4.3 替换 7 处重复日期解析为 `ISO8601Parser.parse()`：DriveDetailView、ChargeDetailView、StatisticsView、TimelineView、HeatmapView、BatteryHealthView、UpdatesView
- [x] 4.4 修复 `UpdatesView.isoDate` — 使用 `ISO8601Parser` 替代默认格式，fallback 改为 `Date.distantPast`
- [x] 4.5 修复 `SettingsView.swift` "Test Connection" — 空 catch 改为用户可见的错误提示

## 5. 跨平台 BLOCKER 修复

- [x] 5.1 Android Dashboard 集成 AMap：实现 `LocationCard` 地图展示（GCJ-02 显示 + AMap SDK 占位）
- [x] 5.2 Android 实现 `GCJ02Converter.kt` — 移植 iOS eviltransform 算法
- [x] 5.3 Android Statistics 下钻：创建 `MonthDetailScreen.kt` + `DayDetailScreen.kt`
- [x] 5.4 Android `NavGraph.kt` — 注册 Statistics 下钻路由
- [x] 5.5 Android `StatisticsScreen.kt` — 接入点击导航

## 6. 文档修正

- [x] 6.1 修正 `docs/glm_P1实施计划.md`：移除 G-01/G-02、G-03 缩小为仅 Android、F-107 标记 Android 缺口
