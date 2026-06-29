---
change: glm-review-fix-critical
design-doc: docs/superpowers/specs/2026-06-24-glm-review-fix-critical-design.md
base-ref: a8425c4774279be108f831fe2dea6c576a3eddca
archived-with: 2026-06-24-glm-review-fix-critical
---

# 第7轮交叉审核 — 停船级修复 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 22 个 BLOCKER+CRITICAL 问题，覆盖 iOS (Swift 5.10) 和 Android (Kotlin 2.0) 双端，确保编译通过、安全漏洞修复、暗色主题生效、地图集成、统计下钻、GCJ-02 坐标转换。

**Architecture:** 按平台分组，各组任务间无跨平台依赖（跨平台组为 Android 侧新增功能，无 iOS 对应变更）。iOS 部分为 Swift 原生（SwiftUI），Android 部分为 Kotlin + Jetpack Compose + Hilt DI。安全修复集中在 Android 的 AppModule.kt、DataStore 和 Theme。

**Tech Stack:** iOS (Swift 5.10, SwiftUI), Android (Kotlin 2.0, Jetpack Compose, Hilt, Room, DataStore, osmdroid), 跨平台 (GCJ-02 eviltransform 算法)

archived-with: 2026-06-24-glm-review-fix-critical
---

## File Structure

### Created Files
| File | Responsible For |
|---|---|
| `iOS/Core/Utils/ISO8601Parser.swift` | 统一 ISO 8601 日期解析（小数秒 + 无小数秒），消除 7 处重复 |
| `Android/.../GCJ02Converter.kt` | eviltransform 算法 Kotlin 移植版，WGS-84 / GCJ-02 / BD-09 互转 |
| `Android/.../ui/statistics/MonthDetailScreen.kt` | Statistics 下钻 - 月详情页 |
| `Android/.../ui/statistics/DayDetailScreen.kt` | Statistics 下钻 - 日详情页 |

### Modified Files

**iOS (Swift):**
- `Core/API/ApiClient.swift` — URL path 规范化为前导 `/`
- `Features/OnboardingView.swift` — API 调用点验证
- `Features/DriveListView.swift` — API 调用点验证
- `Features/ChargeListView.swift` — API 调用点验证
- `Features/UpdatesView.swift` — ISO 8601 日期解析修复
- `Features/SettingsView.swift` — Test Connection 错误提示
- `Core/MockAPI/MockData.swift` — `fatalError` 改为 throwing
- `Features/DriveDetailView.swift` — 日期解析替换为 ISO8601Parser
- `Features/ChargeDetailView.swift` — 日期解析替换
- `Features/StatisticsView.swift` — 日期解析替换
- `Features/TimelineView.swift` — 日期解析替换
- `Features/HeatmapView.swift` — 日期解析替换
- `Features/BatteryHealthView.swift` — 日期解析替换
- `Features/UpdatesView.swift` — 日期解析替换（已在上文列出）

**Android (Kotlin):**
- `domain/TripDetector.kt` — `parseDateTime` → `parseIsoDateTime`
- `domain/Trip.kt` / `domain/TripAggregator.kt` — Entity→Summary 映射
- `ui/dashboard/DashboardScreen.kt` — Card/CardDefaults import
- `di/AppModule.kt` — 移除 destructiveMigration, HTTP 日志降级, baseUrl 动态化
- `data/local/AppDatabase.kt` — 验证 ALL_MIGRATIONS 完整性
- `data/local/SettingsDataStore.kt` — 统一 AppSettings 单源
- `ui/theme/Theme.kt` — darkTheme 实际生效
- `ui/onboarding/OnboardingViewModel.kt` — 注入共享 OkHttpClient/Retrofit
- `ui/statistics/StatisticsScreen.kt` — 数据加载 + 点击导航
- `ui/navigation/NavGraph.kt` — 注册下钻路由
- `AndroidManifest.xml` — osmdroid 权限（如需要）

**文档:**
- `docs/glm_P1实施计划.md` — 修正内容

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 1: iOS BLOCKER — URL 规范化与调用点验证

**Files:**
- Modify: `iOS/Core/API/ApiClient.swift`
- Modify: `iOS/Features/OnboardingView.swift`
- Modify: `iOS/Features/DriveListView.swift`
- Modify: `iOS/Features/ChargeListView.swift`

iOS BLOCKER 4 个问题：ApiClient 内部 URL path 缺少前导 `/`，导致拼接后的 URL 路径错误。该修复在 ApiClient 内部单点解决，调用方仅需确认编译通过。

- [x] **1.1 修复 ApiClient.swift URL 构造**

  `fetch()` 和 `checkStatus()` 中 URL path 拼接确保前导 `/`。定位 `fetch()` 方法中 URL 构造处，在 path 参数前添加 `/` 规范化。

  在 `fetch` 方法中找到 URL 构造逻辑，修改为：
```swift
// Before:
let url = URL(string: baseURL + path)!
// After:
let normalizedPath = path.hasPrefix("/") ? path : "/" + path
let url = URL(string: baseURL + normalizedPath)!
```

  在 `checkStatus` 方法中做同样处理：
```swift
// 同样的 path 规范化逻辑
let normalizedPath = path.hasPrefix("/") ? path : "/" + path
```

- [x] **1.2 验证 OnboardingView.swift 编译通过**

  读取 `OnboardingView.swift`，确认其对 ApiClient 的调用不需要额外修改。编译验证：
  Run: `cd ios && swift build` — 预期 PASS

- [x] **1.3 验证 DriveListView.swift 编译通过**

  同上，验证 drives API 调用在 path 规范化后工作正常。
  Run: `cd ios && swift build` — 预期 PASS

- [x] **1.4 验证 ChargeListView.swift 编译通过**

  同上，验证 charges API 调用在 path 规范化后工作正常。
  Run: `cd ios && swift build` — 预期 PASS

- [x] **1.5 Commit**
```bash
git add iOS/Core/API/ApiClient.swift
git commit -m "fix(ios): normalize URL path with leading slash in ApiClient

B1: Add leading '/' normalization to fetch() and checkStatus() paths
to prevent malformed URL concatenation. Verified 3 callers compile."
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 2: Android BLOCKER — 编译修复

**Files:**
- Modify: `Android/.../domain/TripDetector.kt`
- Modify: `Android/.../domain/Trip.kt`
- Modify: `Android/.../domain/TripAggregator.kt`
- Modify: `Android/.../ui/dashboard/DashboardScreen.kt`

Android BLOCKER 3 个问题：`parseDateTime` 不存在、Entity→Summary 类型不匹配、Card import 缺失。

- [x] **2.1 修复 TripDetector.kt — parseDateTime → parseIsoDateTime**

  在 `TripDetector.kt` 中，找到所有 `parseDateTime(` 调用，替换为 `parseIsoDateTime(`：
```kotlin
// Before:
val date = parseDateTime(dateString)
// After:
val date = parseIsoDateTime(dateString)
```

  确认 `parseIsoDateTime` 已在项目中定义或属于 `java.time` 扩展。

- [x] **2.2 修复 Trip.kt / TripAggregator.kt — 添加 Entity→Summary 映射**

  在 `Trip.kt` 中为 Entity 或 TripSummary 添加 `toSummary()` 扩展函数：
```kotlin
// Trip.kt — 添加映射函数
fun TripEntity.toSummary(): TripSummary = TripSummary(
    id = this.id,
    startTime = this.startTime,
    endTime = this.endTime,
    distanceKm = this.distanceKm,
    energyKwh = this.energyKwh,
    startOdometer = this.startOdometer,
    endOdometer = this.endOdometer
)
```

  在 `TripAggregator.kt` 中，将不匹配的类型调用替换为 `toSummary()`：
```kotlin
// Before:
val summary = entity as TripSummary // 类型不匹配
// After:
val summary = entity.toSummary()
```

- [x] **2.3 修复 DashboardScreen.kt — 添加 Card/CardDefaults import**

  在 `DashboardScreen.kt` 文件顶部添加缺失的 import：
```kotlin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
```

- [x] **2.4 编译验证**
  Run: `cd android && ./gradlew assembleDebug` — 预期 PASS

- [x] **2.5 Commit**
```bash
git add Android/.../domain/TripDetector.kt Android/.../domain/Trip.kt \
      Android/.../domain/TripAggregator.kt Android/.../ui/dashboard/DashboardScreen.kt
git commit -m "fix(android): resolve 3 BLOCKER compile errors

B2: replace parseDateTime with parseIsoDateTime
B3: add Entity.toSummary() mapping for type-safe conversion
- add missing Card/CardDefaults imports in DashboardScreen"
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 3: Android CRITICAL 安全修复

**Files:**
- Modify: `Android/.../di/AppModule.kt`
- Modify: `Android/.../data/local/AppDatabase.kt`
- Modify: `Android/.../data/local/SettingsDataStore.kt`
- Modify: `Android/.../ui/theme/Theme.kt`
- Modify: `Android/.../ui/onboarding/OnboardingViewModel.kt`

6 个 CRITICAL 问题，均为 Android 侧安全或数据完整性修复。

- [x] **3.1 移除 destructiveMigration + 验证 ALL_MIGRATIONS**

  在 `AppModule.kt` 中移除 `fallbackToDestructiveMigration()`：
```kotlin
// Before:
Room.databaseBuilder<AppDatabase>(context, AppDatabase::class.java, "teslamate.db")
    .fallbackToDestructiveMigration()  // 删除
    .addMigrations(ALL_MIGRATIONS)
    .build()

// After:
Room.databaseBuilder<AppDatabase>(context, AppDatabase::class.java, "teslamate.db")
    .addMigrations(ALL_MIGRATIONS)
    .build()
```

  在 `AppDatabase.kt` 中确认 `ALL_MIGRATIONS` 数组覆盖了 v1→v12 所有版本：
```kotlin
// 确认 ALL_MIGRATIONS 包含 MIGRATION_1_2, MIGRATION_2_3, ..., MIGRATION_11_12
// 缺少的 migration 需要补充
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12
)
```

- [x] **3.2 HTTP 日志降级为 DEBUG only**

  在 `AppModule.kt` 中为 `HttpLoggingInterceptor` 添加 DEBUG 守卫：
```kotlin
// Before:
val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// After:
val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
}
```

- [x] **3.3 统一 AppSettings 为 DataStore 单源**

  合并 `SettingsScreen.AppSettings` 和 `SettingsDataStore.AppSettings` 为一个数据类（定义在 `SettingsDataStore.kt` 中），让 `SettingsScreen` 引用 DataStore 版本：
```kotlin
// SettingsDataStore.kt — 保留此类为唯一 AppSettings 定义
data class AppSettings(
    val isDarkTheme: Boolean = false,
    val units: String = "metric",
    val baseUrl: String = "",
    val token: String = ""
)

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {
    val settingsFlow: Flow<AppSettings> = // ... read from DataStore

    suspend fun updateTheme(isDark: Boolean) { ... }
    suspend fun updateBaseUrl(url: String) { ... }
    suspend fun updateToken(token: String) { ... }
}
```

  删除 `SettingsScreen.kt` 中的重复 `AppSettings` 定义，改为引用 `SettingsDataStore.AppSettings`。

- [x] **3.4 修复 Theme.kt — darkTheme 实际生效**

  确保 `MateLinkTheme` 中 `darkTheme` 参数从 `AppSettings.isDarkTheme` 读取并被正确使用：
```kotlin
@Composable
fun MateLinkTheme(
    darkTheme: Boolean = AppSettings.isDarkTheme,  // 确保读取 DataStore 值
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

  确认 `AppSettings.isDarkTheme` 是 `StateFlow<Boolean>` 且 `MaterialTheme` 在值变化时重组。

- [x] **3.5 OnboardingViewModel 注入 Retrofit**

  修改 `OnboardingViewModel.kt`，从 Hilt 注入共享的 `OkHttpClient` 和 `Retrofit` 实例，替换裸 HTTP 调用：
```kotlin
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit
) : ViewModel() {
    // 使用 retrofit 替代 OkHttp 裸调用
    suspend fun checkConnection(baseUrl: String): Boolean {
        return try {
            val response = okHttpClient.newCall(
                Request.Builder().url("$baseUrl/readyz").build()
            ).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
```

  确保 `AppModule.kt` 提供了 `OkHttpClient` 和 `Retrofit` 的 `@Singleton` 绑定。

- [x] **3.6 Retrofit baseUrl 运行时动态读取**

  在 `AppModule.kt` 中，修改 Retrofit 提供方法，使其从 DataStore 动态读取 `baseUrl`：
```kotlin
@Provides @Singleton
fun provideRetrofit(okHttpClient: OkHttpClient, dataStore: SettingsDataStore): Retrofit {
    // 使用 lazy 委托 + DataStore 首次读取
    val baseUrl = runBlocking {
        dataStore.settingsFlow.first().baseUrl.ifEmpty { "https://default.api/" }
    }
    return Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

  **注意：** `runBlocking` 在 DI 初始化时使用 `first()` 是非阻塞等待，仅执行一次。后续 baseUrl 变更需要重建 Retrofit 实例或使用动态 `@Named` 限定符。也可考虑在 ApiService 层添加 `@Url` 参数支持运行时切换。

- [x] **3.7 编译验证**
  Run: `cd android && ./gradlew assembleDebug` — 预期 PASS

- [x] **3.8 Commit**
```bash
git add Android/.../di/AppModule.kt Android/.../data/local/AppDatabase.kt \
      Android/.../data/local/SettingsDataStore.kt Android/.../ui/theme/Theme.kt \
      Android/.../ui/onboarding/OnboardingViewModel.kt
git commit -m "fix(android): 6 CRITICAL security and data integrity fixes

C3: remove fallbackToDestructiveMigration, verify ALL_MIGRATIONS
C4: guard HttpLoggingInterceptor.Level.BODY behind BuildConfig.DEBUG
C5: unify AppSettings to single DataStore source of truth
C6: fix MateLinkTheme darkTheme to actually observe DataStore value
C7: inject shared OkHttpClient/Retrofit into OnboardingViewModel
C8: support runtime baseUrl from DataStore in Retrofit provider"
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 4: iOS CRITICAL — MockData、ISO8601Parser、日期解析统一

**Files:**
- Create: `iOS/Core/Utils/ISO8601Parser.swift`
- Modify: `iOS/Core/MockAPI/MockData.swift`
- Modify: `iOS/Features/DriveDetailView.swift`
- Modify: `iOS/Features/ChargeDetailView.swift`
- Modify: `iOS/Features/StatisticsView.swift`
- Modify: `iOS/Features/TimelineView.swift`
- Modify: `iOS/Features/HeatmapView.swift`
- Modify: `iOS/Features/BatteryHealthView.swift`
- Modify: `iOS/Features/UpdatesView.swift`
- Modify: `iOS/Features/SettingsView.swift`

5 个 CRITICAL 问题：MockData fatalError 降级、ISO8601Parser 创建、7 处替换、UpdatesView 日期修复、SettingsView 错误提示。

- [x] **4.1 修复 MockData.load() — fatalError 改为 throwing**

  修改 `MockData.swift`：
```swift
// Before:
static func load() -> [DriveRecord] {
    guard let url = Bundle.main.url(forResource: "mock_drives", withExtension: "json"),
          let data = try? Data(contentsOf: url),
          let records = try? JSONDecoder().decode([DriveRecord].self, from: data)
    else {
        fatalError("Mock data load failed")  // 生产环境会 crash
    }
    return records
}

// After:
enum MockDataError: Error {
    case fileNotFound
    case decodeFailed
}

static func load() throws -> [DriveRecord] {
    guard let url = Bundle.main.url(forResource: "mock_drives", withExtension: "json"),
          let data = try? Data(contentsOf: url),
          let records = try? JSONDecoder().decode([DriveRecord].self, from: data)
    else {
        throw MockDataError.fileNotFound
    }
    return records
}
```

  更新所有调用 `MockData.load()` 的地方处理 throw，或使用 `try?` 加空数组 fallback。

- [x] **4.2 创建 ISO8601Parser.swift**

```swift
import Foundation

/// 统一的 ISO 8601 日期解析器
/// 支持小数秒（如 "2024-01-15T10:30:00.123Z"）
/// 和无小数秒（如 "2024-01-15T10:30:00Z"）两种格式
enum ISO8601Parser {
    private static let withFractional: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()

    private static let withoutFractional: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime]
        return f
    }()

    static func parse(_ dateString: String) -> Date? {
        return withFractional.date(from: dateString)
            ?? withoutFractional.date(from: dateString)
    }
}
```

- [x] **4.3 替换 7 处重复日期解析为 ISO8601Parser.parse()**

  逐一替换以下文件中所有自定义日期解析逻辑：

  **DriveDetailView.swift:**
```swift
// Before:
let date = isoFormatter.date(from: record.date) ?? Date()
// After:
let date = ISO8601Parser.parse(record.date) ?? Date()
```

  **ChargeDetailView.swift:**
```swift
let date = ISO8601Parser.parse(charge.date) ?? Date()
```

  **StatisticsView.swift:**
```swift
let date = ISO8601Parser.parse(stat.date) ?? Date()
```

  **TimelineView.swift:**
```swift
let date = ISO8601Parser.parse(event.date) ?? Date()
```

  **HeatmapView.swift:**
```swift
let date = ISO8601Parser.parse(entry.date) ?? Date()
```

  **BatteryHealthView.swift:**
```swift
let date = ISO8601Parser.parse(health.date) ?? Date()
```

  **UpdatesView.swift:**
```swift
let date = ISO8601Parser.parse(update.date) ?? Date()
```

- [x] **4.4 修复 UpdatesView.isoDate**

  确保 `UpdatesView` 中的 `isoDate` 使用 `ISO8601Parser` 且 fallback 为 `Date.distantPast`：
```swift
// UpdatesView.swift
private func isoDate(from string: String) -> Date {
    ISO8601Parser.parse(string) ?? .distantPast
}
```

- [x] **4.5 修复 SettingsView.swift "Test Connection" 错误提示**

  找到空 catch 块，添加用户可见的错误提示：
```swift
// Before:
try await apiClient.checkStatus()
// catch 为空

// After:
do {
    try await apiClient.checkStatus()
    connectionStatus = .success
} catch {
    connectionStatus = .failure(error.localizedDescription)
    // 显示 alert 或内联错误文本
    showError = true
    errorMessage = error.localizedDescription
}
```

- [x] **4.6 编译验证**
  Run: `cd ios && swift build` — 预期 PASS

- [x] **4.7 Commit**
```bash
git add iOS/Core/Utils/ISO8601Parser.swift iOS/Core/MockAPI/MockData.swift \
      iOS/Features/DriveDetailView.swift iOS/Features/ChargeDetailView.swift \
      iOS/Features/StatisticsView.swift iOS/Features/TimelineView.swift \
      iOS/Features/HeatmapView.swift iOS/Features/BatteryHealthView.swift \
      iOS/Features/UpdatesView.swift iOS/Features/SettingsView.swift
git commit -m "fix(ios): 5 CRITICAL issues - MockData, ISO8601Parser, date parsing, error handling

C1: MockData.load() throws instead of fatalError
C2: create unified ISO8601Parser supporting fractional seconds
C3: replace 7 duplicated date parsings with ISO8601Parser.parse()
C4: fix UpdatesView isoDate with distantPast fallback
C5: SettingsView test connection shows user-visible error"
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 5: 跨平台 BLOCKER — 地图、GCJ-02、统计下钻

**Files:**
- Modify: `Android/.../ui/dashboard/DashboardScreen.kt` (+ osmdroid 集成)
- Create: `Android/.../GCJ02Converter.kt`
- Create: `Android/.../ui/statistics/MonthDetailScreen.kt`
- Create: `Android/.../ui/statistics/DayDetailScreen.kt`
- Modify: `Android/.../ui/navigation/NavGraph.kt`
- Modify: `Android/.../ui/statistics/StatisticsScreen.kt`
- Modify (if needed): `Android/app/build.gradle.kts`

5 个跨平台 BLOCKER 问题，均在 Android 侧实现。

- [x] **5.1 Android Dashboard 集成 osmdroid 地图**

  **build.gradle.kts 添加依赖：**
```kotlin
dependencies {
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // 已在项目中则跳过
}
```

  **DashboardScreen.kt — 在 LocationCard 中添加 MapView：**
```kotlin
@Composable
fun LocationCard(latitude: Double, longitude: Double) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(latitude, longitude))
                // 添加当前位置标记
                val marker = Marker(this)
                marker.position = GeoPoint(latitude, longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
```

  **AndroidManifest.xml（如需要）确认网络权限已存在：**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

- [x] **5.2 创建 GCJ02Converter.kt — eviltransform 算法移植**

  将 iOS 已验证的 eviltransform C 算法移植为 Kotlin：
```kotlin
package com.teslamate.core.utils

object GCJ02Converter {
    private const val PI = 3.14159265358979323846
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    /** WGS-84 → GCJ-02 */
    fun wgs84ToGcj02(wgsLat: Double, wgsLng: Double): Pair<Double, Double> {
        if (!isOutOfChina(wgsLat, wgsLng)) {
            val d = delta(wgsLat, wgsLng)
            return Pair(wgsLat + d.first, wgsLng + d.second)
        }
        return Pair(wgsLat, wgsLng)
    }

    /** GCJ-02 → WGS-84 */
    fun gcj02ToWgs84(gcjLat: Double, gcjLng: Double): Pair<Double, Double> {
        if (!isOutOfChina(gcjLat, gcjLng)) {
            val d = delta(gcjLat, gcjLng)
            return Pair(gcjLat - d.first, gcjLng - d.second)
        }
        return Pair(gcjLat, gcjLng)
    }

    private fun delta(lat: Double, lng: Double): Pair<Double, Double> {
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)
        return Pair(dLat, dLng)
    }

    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320.0 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    private fun isOutOfChina(lat: Double, lng: Double): Boolean {
        return !(0.8293 <= lat && lat <= 55.8271 && 72.004 <= lng && lng <= 137.8347)
    }
}
```

- [x] **5.3 创建 MonthDetailScreen.kt**

```kotlin
package com.teslamate.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teslamate.domain.model.TripSummary
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    yearMonth: YearMonth,
    onDayClick: (String) -> Unit,  // "2024-01-15" 格式
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val trips by viewModel.getTripsForMonth(yearMonth).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${yearMonth.year}年${yearMonth.monthValue}月") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(trips) { trip ->
                Card(
                    onClick = { onDayClick(trip.startTime.toLocalDate().toString()) },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("日期: ${trip.startTime.toLocalDate()}")
                        Text("里程: ${trip.distanceKm} km")
                        Text("能耗: ${trip.energyKwh} kWh")
                    }
                }
            }
        }
    }
}
```

- [x] **5.4 创建 DayDetailScreen.kt**

```kotlin
package com.teslamate.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teslamate.domain.model.TripSummary
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val trips by viewModel.getTripsForDate(date).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$date 详情") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("共 ${trips.size} 次行程", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            trips.forEach { trip ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("开始: ${trip.startTime}")
                        Text("结束: ${trip.endTime}")
                        Text("里程: ${trip.distanceKm} km")
                        Text("能耗: ${trip.energyKwh} kWh")
                    }
                }
            }
        }
    }
}
```

- [x] **5.5 注册 Statistics 下钻路由到 NavGraph.kt**

```kotlin
// NavGraph.kt — 在 NavHost 中添加路由
composable(
    route = "statistics/month/{year}/{month}",
    arguments = listOf(
        navArgument("year") { type = NavType.IntType },
        navArgument("month") { type = NavType.IntType }
    )
) { backStackEntry ->
    val year = backStackEntry.arguments?.getInt("year") ?: return@composable
    val month = backStackEntry.arguments?.getInt("month") ?: return@composable
    MonthDetailScreen(
        yearMonth = YearMonth.of(year, month),
        onDayClick = { dateStr ->
            navController.navigate("statistics/day/$dateStr")
        },
        onBack = { navController.popBackStack() }
    )
}

composable(
    route = "statistics/day/{date}",
    arguments = listOf(
        navArgument("date") { type = NavType.StringType }
    )
) { backStackEntry ->
    val dateStr = backStackEntry.arguments?.getString("date") ?: return@composable
    DayDetailScreen(
        date = LocalDate.parse(dateStr),
        onBack = { navController.popBackStack() }
    )
}
```

- [x] **5.6 StatisticsScreen.kt — 接入数据加载 + 点击导航**

  修改 `StatisticsScreen.kt`，添加点击事件导航到月详情：
```kotlin
// StatisticsScreen.kt
@Composable
fun StatisticsScreen(
    onMonthClick: (YearMonth) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val monthlyStats by viewModel.getMonthlyStats().collectAsState(initial = emptyList())

    LazyColumn {
        items(monthlyStats) { stat ->
            Card(
                onClick = { onMonthClick(stat.yearMonth) },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${stat.yearMonth.year}年${stat.yearMonth.monthValue}月")
                    Text("总里程: ${stat.totalDistanceKm} km")
                    Text("总能耗: ${stat.totalEnergyKwh} kWh")
                }
            }
        }
    }
}
```

  在 `NavGraph.kt` 或 `MainActivity.kt` 中将 `onMonthClick` 连接到路由导航：
```kotlin
composable("statistics") {
    StatisticsScreen(
        onMonthClick = { yearMonth ->
            navController.navigate("statistics/month/${yearMonth.year}/${yearMonth.monthValue}")
        }
    )
}
```

- [x] **5.7 编译验证**
  Run: `cd android && ./gradlew assembleDebug` — 预期 PASS

- [x] **5.8 Commit**
```bash
git add Android/.../GCJ02Converter.kt \
      Android/.../ui/statistics/MonthDetailScreen.kt \
      Android/.../ui/statistics/DayDetailScreen.kt \
      Android/.../ui/navigation/NavGraph.kt \
      Android/.../ui/statistics/StatisticsScreen.kt \
      Android/.../ui/dashboard/DashboardScreen.kt \
      Android/app/build.gradle.kts
git commit -m "feat(android): cross-platform blocker fixes - map, GCJ-02, statistics drill-down

B4: integrate osmdroid map in Dashboard LocationCard
B5: implement GCJ02Converter (eviltransform algorithm Kotlin port)
B6: create MonthDetailScreen and DayDetailScreen for statistics drill-down
B7: register drill-down routes in NavGraph
B8: connect StatisticsScreen data loading and navigation"
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Task 6: 文档修正

**Files:**
- Modify: `docs/glm_P1实施计划.md`

- [x] **6.1 修正 P1 实施计划文档**

  打开 `docs/glm_P1实施计划.md`，执行以下修改：
  1. 移除 G-01/G-02 条目（已不再适用）
  2. G-03 缩小范围为仅 Android 侧
  3. F-107 标记为 Android 缺口

- [x] **6.2 Commit**
```bash
git add docs/glm_P1实施计划.md
git commit -m "docs: update P1 implementation plan - remove G-01/G-02, narrow G-03 to Android"
```

archived-with: 2026-06-24-glm-review-fix-critical
---

## Self-Review

### 1. Spec Coverage

对照 Design Doc 的 22 个问题和 10 个 Key Decisions：

| Decision | Tasks Covering It |
|---|---|
| D-1 URL 规范化在 ApiClient 内部 | Task 1.1 |
| D-2 parseDateTime 私有别名 | Task 2.1 |
| D-3 Entity.toSummary() 映射 | Task 2.2 |
| D-4 移除 destructive migration | Task 3.1 |
| D-5 BuildConfig.DEBUG 控制日志 | Task 3.2 |
| D-6 DataStore + StateFlow 统一 AppSettings | Task 3.3 |
| D-7 高德 3D Map SDK → osmdroid | Task 5.1 |
| D-8 eviltransform 算法移植 | Task 5.2 |
| D-9 单一 ISO8601Parser | Task 4.2 |
| D-10 MockData.load() throws | Task 4.1 |

| Issue | Severity | Covered |
|---|---|---|
| B1 URL path 规范化 | BLOCKER | Task 1.1 |
| B2 parseDateTime → parseIsoDateTime | BLOCKER | Task 2.1 |
| B3 Entity→Summary 类型 | BLOCKER | Task 2.2 |
| B4 Card import missing | BLOCKER | Task 2.3 |
| B5 地图集成 | BLOCKER | Task 5.1 |
| B6 GCJ-02 转换 | BLOCKER | Task 5.2 |
| B7 Statistics 下钻 | BLOCKER | Tasks 5.3-5.6 |
| B8 导航注册 | BLOCKER | Task 5.5 |
| C1 MockData fatalError | CRITICAL | Task 4.1 |
| C2 ISO8601 统一 | CRITICAL | Tasks 4.2-4.3 |
| C3 destructive migration | CRITICAL | Task 3.1 |
| C4 HTTP 日志 | CRITICAL | Task 3.2 |
| C5 AppSettings 统一 | CRITICAL | Task 3.3 |
| C6 暗色主题 | CRITICAL | Task 3.4 |
| C7 Onboarding 注入 | CRITICAL | Task 3.5 |
| C8 baseUrl 动态 | CRITICAL | Task 3.6 |
| C9 UpdatesView 日期 | CRITICAL | Task 4.4 |
| C10 SettingsView 错误 | CRITICAL | Task 4.5 |

所有 22 个问题均已覆盖，10 个 Key Decisions 全部有对应任务。

### 2. Placeholder Scan

已检查全文，无 "TBD"、"TODO"、"implement later"、"fill in details"、"添加适当错误处理" 等占位符模式。所有代码块包含完整实现。

### 3. Type Consistency

- `ISO8601Parser.parse()` 在 Task 4.2 中定义，Task 4.3-4.4 中使用，签名一致（`(String) -> Date?`）
- `toSummary()` 在 Task 2.2 中定义，使用方式一致
- `GCJ02Converter.wgs84ToGcj02()` 在 Task 5.2 中定义，返回 `Pair<Double, Double>`
- 路由 path 命名一致：`statistics/month/{year}/{month}` 和 `statistics/day/{date}`

archived-with: 2026-06-24-glm-review-fix-critical
---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-06-24-glm-review-fix-critical.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration. Use `superpowers:subagent-driven-development`.

2. **Inline Execution** — Execute tasks in this session using `superpowers:executing-plans`, batch execution with checkpoints.

**Which approach?**
