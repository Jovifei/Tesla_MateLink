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

**选择：** 合并 `SettingsScreen.AppSettings` 和 `SettingsDataStore.AppSettings`，使用单一 DataStore 源 + StateFlow 暴露给 Compose。`MateLinkTheme(darkTheme = settings.isDarkTheme)` 在 `MainActivity` 已接线但 `Theme.kt` 内部 `isSystemInDarkTheme()` 优先级覆盖了它 → 修复 `MateLinkTheme` 的实现。

### D-7: Android 地图 — 高德 AMap SDK

**选择：** 使用高德 3D Map SDK 作为 Android Dashboard 地图引擎，配合 D-8 的 GCJ-02 转换器

**替代方案：** Google Maps SDK — 拒绝，需要 Google Play Services，自托管场景不友好；OpenStreetMap/osmdroid — 拒绝，中国境内无偏移但用户偏好高德。

### D-8: Android GCJ-02 — 移植 iOS 算法

**选择：** 将 `GCJ02Converter.swift` 的 eviltransform 算法移植为 Kotlin object

```kotlin
object GCJ02Converter {
    private const val PI = Math.PI
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323
    
    fun wgs84ToGcj02(lat: Double, lon: Double): Pair<Double, Double>
    fun gcj02ToWgs84(lat: Double, lon: Double): Pair<Double, Double>
    fun isInChina(lat: Double, lon: Double): Boolean
}
```

### D-9: iOS 日期解析 — 单一工具文件

**选择：** 创建 `Core/Utils/ISO8601Parser.swift`，用两遍解析（先小数秒，后无小数秒），替换 7 处重复实现

```swift
enum ISO8601Parser {
    static let fractionalFormatter: ISO8601DateFormatter = { ... }()
    static let basicFormatter: ISO8601DateFormatter = { ... }()
    
    static func parse(_ string: String) -> Date? {
        fractionalFormatter.date(from: string) ?? basicFormatter.date(from: string)
    }
}
```

### D-10: iOS MockData 降级 — throw 替代 fatalError

**选择：** `MockData.load()` 改为 throwing 函数，调用方 `.task { do { try load() } catch { error = error } }` 展示错误状态

## Risks / Trade-offs

- **[高德 AMap SDK 依赖]** → Android 新增高德 3D Map SDK 依赖（D-7 决策）。风险低，高德 SDK 成熟稳定，中国境内地图偏移合规。
- **[AppSettings 合并]** → 涉及 SettingsScreen.kt 和 SettingsDataStore.kt 重构。回归风险中等，需注意向后兼容。
- **[ISO 8601 统一]** → 7 处调用点替换。风险低，两遍解析语义不变。
- **[Retrofit baseUrl 动态覆盖]** → 使用 OkHttp Interceptor 动态改写，增加轻微请求开销。可接受。

## Migration Plan

1. **Step 1**: 修复 BLOCKER 编译错误（B1-B4）→ 代码可编译
2. **Step 2**: 修复 CRITICAL 安全/数据问题（C3-C6）→ 安全合规
3. **Step 3**: 修复 iOS CRITICAL（C1-C2）→ iOS 不崩溃
4. **Step 4**: 修复跨平台 BLOCKER（B5-B8）→ 功能补全
5. **Step 5**: 计划文档修正 → 文档准确
6. **Verify**: 编译验证 + 手动冒烟测试

回滚：每步独立 commit，git revert 可逐回滚。

## Open Questions

无 — 三个审查 agent 已提供精确文件路径、行号和修复方向。
