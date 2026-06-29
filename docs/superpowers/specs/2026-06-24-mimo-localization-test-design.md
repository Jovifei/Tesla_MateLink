---
comet_change: mimo-localization-test
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-25-mimo-localization-test
status: final
---

# mimo-localization-test 深度设计文档

> 日期：2026-06-24
> 目标：中国本地化 + 真机测试

## 1. 高德地图集成

### 1.1 Android

**依赖**（`gradle/libs.versions.toml`）：
```toml
amap3d = { group = "com.amap.api", name = "3dmap", version = "9.6.1" }
amaplocation = { group = "com.amap.api", name = "location", version = "6.4.5" }
```

**build.gradle.kts dependencies**：
```kotlin
implementation(libs.amap3d)
implementation(libs.amaplocation)
```

**初始化**：`MateLinkApplication.onCreate()` 中设置 API Key

**封装**：新建 `ui/components/AmapComposeView.kt`，用 `AndroidView` 包装 `MapView`，暴露 `AmapRouteView(routePoints, markers)` 接口

**AndroidManifest 配置**：
```xml
<application>
    <meta-data android:name="com.amap.api.v2.apikey" android:value="YOUR_KEY" />
</application>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-feature android:glEsVersion="0x00020000" android:required="true" />
```

**生命周期管理**（Compose DisposableEffect）：
```kotlin
AndroidView(factory = { context -> MapView(context) }) { mapView ->
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose { mapView.onPause(); mapView.onDestroy() }
    }
}
```

**替换点**（6 个文件）：
- `DriveDetailScreen.kt:80-84` — 路线轨迹
- `TripDetailScreen.kt:105-110` — 路线渲染
- `ChargeDetailScreen.kt:79-82` — 充电位置地图
- `WhereWasIScreen.kt:73-75` — 位置历史地图
- `RegionsVisitedScreen.kt:79-84` — 区域地图（含 Polygon overlay）
- `TopDestinationsScreen.kt` — 目的地地图

### 1.2 iOS

**Podfile 完整配置**：
```ruby
platform :ios, '16.0'
target 'MateLink' do
  use_frameworks!
  pod 'AMap3DMap', '~> 9.6'
  pod 'AMapFoundation', '~> 1.8'
  pod 'AMapLocation', '~> 2.10'   # 定位功能
  pod 'AMapSearch', '~> 9.7'      # 地图搜索/POI
end
```

**Info.plist 权限**：
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>用于显示车辆位置和行驶路线</string>
```

**初始化**（`MateLinkApp.swift`）：
```swift
@main
struct MateLinkApp: App {
    init() {
        AMapServices.shared().apiKey = "YOUR_KEY"
    }
    // ...
}
```

**MAMapView UIViewRepresentable 封装**：将 `AmapView.swift` 中的 MapKit `Map` 替换为 `MAMapView` 封装
- 保持现有的 `isChineseLocale` 切换逻辑
- GCJ-02 转换：zh-CN 时 `wgs84ToGcj02()` 后传入高德，其他时直接使用 WGS-84

### 1.3 GCJ-02 坐标纠偏

**iOS**：已有完整实现 (`GCJ02Converter.swift:21-153`)，eviltransform 算法，精度 <1m

**Android**：新建 `util/GCJ02Converter.kt`，移植相同算法
- `a = 6378245.0`
- `ee = 0.00669342162296594323`
- 提供 `wgs84ToGcj02(lat, lng)` 和 `gcj02ToWgs84(lat, lng)`

### 1.4 地图切换逻辑

```kotlin
val useAmap = Locale.getDefault().language == "zh" 
    && Locale.getDefault().country == "CN"
```

- `zh-CN` → 高德地图 SDK（GCJ-02 坐标系）
  - **GPS 原始数据是 WGS-84，必须经 `wgs84ToGcj02()` 转换后才能传入高德**
  - 高德 SDK 原生使用 GCJ-02，直接传入 WGS-84 会导致标注偏移 100-500m
- 其他 → osmdroid / MapKit（WGS-84 坐标系，GPS 数据直接使用，无需纠偏）
- **`isInChina` 边界检测**：iOS 已有实现（`lat 0.8293~55.8271, lng 72.004~137.8347`），Android 需同步移植

### 1.5 路线轨迹渲染

**Android**：
```kotlin
val polyline = aMap.addPolyline(
    PolylineOptions().addAll(gcj02Points)
        .color(routeColorArgb).width(8f)
)
```

**iOS**：
```swift
let polyline = MAPolyline(coordinates: &gcj02Coords, count: count)
mapView.addOverlay(polyline)
```

复用 `RouteSimplifier.kt` 做 Douglas-Peucker 简化。

## 2. 中文 UI（i18n）

### 2.1 Android

- **现状**：`values-zh/strings.xml` 仅有 `app_name` 一项，需补充 50+ 条翻译
- **复用**：参考 matedroid 的 `values-zh/strings.xml`（200+ 条已翻译字符串）
- 设置页添加语言切换

### 2.2 iOS

- **现状**：无 `Localizable.strings`，需新建 `zh-Hans.lproj/Localizable.strings`
- 翻译内容与 Android 保持一致
- 设置页添加语言切换

### 2.3 Web

- **现状**：代码中已引用 `react-i18next`（10 个文件使用 `t()`），但无翻译文件
- 创建 `src/messages/zh.json` + `src/messages/en.json`
- 补全所有 key（与 Android/iOS 保持一致）

## 3. 分时电价

### 3.1 配置页面

**数据结构**：
```kotlin
data class TariffConfig(
    val peakPrice: Double = 1.0,      // 峰时段价格 ¥/kWh
    val flatPrice: Double = 0.7,      // 平时段价格 ¥/kWh
    val valleyPrice: Double = 0.3,    // 谷时段价格 ¥/kWh
    val peakHours: List<IntRange> = listOf(8..10, 18..20),   // 峰时段：8:00-11:00, 18:00-21:00
    val flatHours: List<IntRange> = listOf(6..7, 11..17, 21..22),  // 平时段
    val valleyHours: List<IntRange> = listOf(23..23, 0..5)   // 谷时段：23:00-6:00
)
```

**默认时段说明**（符合中国大部分城市）：
- 峰时段：8:00-11:00, 18:00-21:00（用电高峰）
- 平时段：6:00-8:00, 11:00-18:00, 21:00-23:00
- 谷时段：23:00-6:00（深夜低谷）

### 3.2 计算逻辑（跨时段拆分计费）

**问题**：原方案仅取起始时刻定价，跨时段充电（如 22:30 充 3 小时）全程按谷价计算，错误。

**修正方案**：按分钟拆分各时段分别计费

```kotlin
fun calculateCost(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    totalEnergyKwh: Double,
    config: TariffConfig
): Double {
    val durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
    val energyPerMinute = totalEnergyKwh / durationMinutes
    var totalCost = 0.0
    var currentTime = startTime

    while (currentTime.isBefore(endTime)) {
        val hour = currentTime.hour
        val price = when {
            config.peakHours.any { hour in it } -> config.peakPrice
            config.flatHours.any { hour in it } -> config.flatPrice
            else -> config.valleyPrice
        }
        totalCost += energyPerMinute * price
        currentTime = currentTime.plusMinutes(1)
    }
    return totalCost
}
```

**整点归属规则**：左闭右开 `[08:00, 09:00)` 属于峰时段

**跨日处理**：`23:00 → 次日 02:00` 自动跨越日期边界

**复用参考**：iOS 已有 `TariffConfigView.swift` 实现，Android 直接移植数据结构和 UI 逻辑

### 3.3 UI

- 设置页新增 "分时电价" 入口
- 配置页面：峰/平/谷时段 + 电价输入
- 充电详情：显示分时电价成本

## 4. 真机测试

### 4.1 测试范围

**功能测试**：
- Dashboard 功能（电量、状态、胎压、趋势图）
- 充电历史功能（列表、详情、曲线图）
- 驾驶历史功能（列表、详情、轨迹地图）
- 电池健康功能（衰减趋势）
- 高德地图显示（zh-CN 正确显示高德，其他显示系统地图）
- 中文 UI（所有页面中文正确）
- 分时电价（峰/平/谷计算正确）
- 车辆切换（多车切换正常）

**专项测试**：
- GCJ-02 精度验证（误差 < 1m，对比已知坐标）
- i18n 语言切换（中→英→中，无崩溃）
- 分时电价边界（23:59 充电、00:01 充电、跨时段充电、跨日充电）
- 离线模式（无网络时缓存数据显示）
- 地图生命周期（切换页面后地图正确释放）

**异常/边界测试**：
- GPS 信号丢失时地图行为
- 弱网环境下地图加载
- 多车快速切换
- 深色模式下地图与 UI 适配
- GCJ-02 坐标边界值（极地、跨 180° 经线）
- 分时电价配置为空/非法值的异常处理

**Web 端测试**：
- react-i18next 语言切换
- 地图组件响应式布局
- 深色模式适配

**性能测试**：
- 启动时间（< 2s）
- 列表帧率（≥ 50fps）
- 内存占用（< 200MB）
- 地图渲染（缩放/平移流畅）

### 4.2 测试设备

- Android：需要 Android 10+ 设备
- iOS：需要 Mac + iPhone (iOS 16+)

## 5. 验收标准

| 功能 | 验收标准 |
|------|----------|
| 高德地图 | zh-CN 显示高德地图，其他显示系统地图 |
| GCJ-02 | 地图标注误差 < 1m（对比已知坐标验证） |
| 中文 UI | 所有页面中文显示正确，语言切换无崩溃 |
| 分时电价 | 充电成本按时段计算正确（含跨时段边界） |
| 离线模式 | 无网络时缓存数据显示正常 |
| 真机测试 | 核心功能无崩溃，性能达标（启动 < 2s，帧率 ≥ 50fps） |
