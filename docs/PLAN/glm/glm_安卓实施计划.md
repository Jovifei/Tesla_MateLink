# Tesla_MateLink Android 实施计划

> 日期：2026-06-23 | 技术栈：Kotlin 2.0 + Jetpack Compose + Material 3
> **核心借鉴**：matedroid (67★, 完整 Clean Architecture) — 90% 可直接复用
> 工程：`E:\project\tesla_master\app_glm\android/`
> **策略**：iOS 先做（无完整参考）→ Android 后做（有 matedroid 抄，更快）

## 零、开发环境

| 需求 | 说明 |
|---|---|
| **IDE** | Android Studio Hedgehog+ (免费) |
| **JDK** | 17 |
| **Kotlin** | 2.0+ |
| **Android SDK** | API 26 (min) / API 35 (target) |
| **Gradle** | 8.x Kotlin DSL |
| **模拟器** | Pixel 8 Pro (API 35) |
| **真机** | Android 12+ |

## 一、Checklist 总览

```
Phase 1: 📋 工程初始化 (T0, 4d)
Phase 2: 📋 数据层 + Onboarding (T1-T3, 5d)
Phase 3: 📋 Dashboard + Drives + Charges (T4-T6, 6d)
Phase 4: 📋 Battery + 分析页 + 中国本地化 (T7-T9, 5d)
Phase 5: 📋 测试 + Google Play 上架 (T10-T11, 3d)
```

## 二、Phase 1: 工程初始化 (W1)

### T0 — 创建 Android 工程

**借鉴**：matedroid 完整 Gradle 配置 + CI 脚本

```
app_glm/android/
├── build.gradle.kts                         ← 根构建 (借鉴 matedroid)
├── settings.gradle.kts
├── gradle.properties
├── gradle/
├── app/
│   ├── build.gradle.kts                     ← 依赖 (Compose/Material3/Retrofit/Room/Hilt)
│   └── src/main/java/com/teslamatelink/
│       ├── MateLinkApp.kt                   ← Application + Hilt
│       ├── MainActivity.kt                  ← Compose setContent
│       ├── data/
│       │   ├── api/
│       │   │   ├── TeslaMateApi.kt          ← Retrofit 接口 (16 endpoints)
│       │   │   ├── ApiInterceptor.kt        ← Bearer Token
│       │   │   └── model/                   ← 数据类 (Car, CarStatus, Drive, Charge...)
│       │   ├── local/
│       │   │   ├── AppDatabase.kt           ← Room + DAO
│       │   │   └── CacheManager.kt          ← MMKV / DataStore
│       │   └── repository/
│       │       ├── CarRepository.kt         ← Impl (Mock + Real)
│       │       └── StatusRepository.kt
│       ├── domain/
│       │   ├── model/                       ← 领域模型 (纯 Kotlin)
│       │   ├── usecase/                     ← GetCarStatus, GetDrives, GetCharges...
│       │   └── route/
│       │       ├── RouteSimplifier.kt       ← ★ 直抄 matedroid
│       │       └── TripAggregator.kt        ← ★ 直抄 matedroid
│       ├── di/                              ← Hilt Module
│       ├── ui/
│       │   ├── theme/                       ← ★ 直抄 matedroid Theme.kt + Color.kt
│       │   ├── navigation/
│       │   │   └── NavGraph.kt              ← NavHost 路由
│       │   ├── onboarding/
│       │   │   ├── OnboardingScreen.kt      ← F-001
│       │   │   └── OnboardingViewModel.kt
│       │   ├── dashboard/
│       │   │   ├── DashboardScreen.kt       ← ★ 直抄 matedroid
│       │   │   ├── DashboardViewModel.kt
│       │   │   └── components/
│       │   │       ├── BatteryCard.kt
│       │   │       ├── StatusBadge.kt
│       │   │       ├── InfoGrid.kt
│       │   │       └── ChargingCard.kt
│       │   ├── drives/
│       │   │   ├── DriveListScreen.kt       ← ★ 直抄 matedroid
│       │   │   ├── DriveDetailScreen.kt
│       │   │   └── DriveViewModel.kt
│       │   ├── charges/
│       │   │   ├── ChargeListScreen.kt
│       │   │   ├── ChargeDetailScreen.kt
│       │   │   └── ChargeViewModel.kt
│       │   ├── battery/
│       │   │   └── BatteryHealthScreen.kt   ← ★ 直抄 matedroid
│       │   ├── statistics/
│       │   │   └── StatisticsScreen.kt      ← ★ 直抄 matedroid MileageScreen
│       │   ├── updates/
│       │   │   └── UpdatesScreen.kt
│       │   ├── heatmap/
│       │   │   └── HeatmapScreen.kt
│       │   ├── efficiency/
│       │   │   └── EfficiencyScreen.kt
│       │   ├── vampire/
│       │   │   └── VampireScreen.kt
│       │   ├── cost/
│       │   │   └── CostScreen.kt
│       │   ├── destinations/
│       │   │   └── DestinationsScreen.kt
│       │   ├── timeline/
│       │   │   └── TimelineScreen.kt
│       │   ├── range/
│       │   │   └── RangeScreen.kt
│       │   ├── components/
│       │   │   ├── CarSwitcher.kt           ← ★ 直抄 matedroid
│       │   │   ├── CarImage.kt              ← 2D 车辆图
│       │   │   └── StatCard.kt
│       │   └── settings/
│       │       ├── SettingsScreen.kt
│       │       └── AboutScreen.kt
│       ├── widget/                          ← ★ 直抄 matedroid Widget
│       │   ├── BatteryWidget.kt
│       │   └── WidgetUpdateWorker.kt
│       └── mock/
│           └── MockData.kt                  ← 加载 mock_data.json
├── app/src/main/res/                        ← 资源
│   ├── values/colors.xml, strings.xml, themes.xml
│   ├── values-zh/                           ← 中文资源
│   └── drawable/                            ← 车辆图片
└── app/src/test/                            ← 单元测试
```

**依赖** (build.gradle.kts):
```kotlin
// ★ 直接从 matedroid/build.gradle.kts 抄:
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.7+")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose")
    // Network (★ 直抄)
    implementation("com.squareup.retrofit2:retrofit:2.11+")
    implementation("com.squareup.okhttp3:okhttp:4.12+")
    // Image (★ 直抄)
    implementation("io.coil-kt:coil-compose:2.7+")
    // DI (★ 直抄)
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    // Room (★ 直抄)
    implementation("androidx.room:room-runtime:2.6+")
    kapt("androidx.room:room-compiler:2.6+")
    // Charts (★ 直抄)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Local storage (★ 直抄)
    implementation("com.tencent:mmkv:1.3+")
    // Map (中国用高德)
    implementation("com.amap.api:3dmap:10.0+")
}
```

**工时**：3d (复制 matedroid 配置 → 改包名 → 调通 Gradle)

### T0.1 — 配置 CI/CD (借鉴 matedroid CI)

```yaml
# .github/workflows/android.yml
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew detekt ktlintCheck
  test:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew test
  build:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew assembleDebug
```

**工时**：1d

## 三、Phase 2: 数据层 (W1-W2)

### T1 — API Client (Retrofit + OkHttp)

**策略**：直抄 matedroid `data/api/` 目录

```kotlin
// data/api/TeslaMateApi.kt
// ★ 从 matedroid 改写端点为 TeslaMateApi v1.21
interface TeslaMateApi {
    @GET("api/ping")
    suspend fun ping(): PingResponse

    @GET("api/v1/cars")
    suspend fun getCars(): CarApiResponse

    @GET("api/v1/cars/{carId}/status")
    suspend fun getCarStatus(@Path("carId") id: Int): CarStatus

    @GET("api/v1/cars/{carId}/drives")
    suspend fun getDrives(@Path("carId") id: Int): List<Drive>

    @GET("api/v1/cars/{carId}/drives/{driveId}")
    suspend fun getDriveDetail(@Path("carId") id: Int, @Path("driveId") did: Int): Drive

    @GET("api/v1/cars/{carId}/charges")
    suspend fun getCharges(@Path("carId") id: Int): List<Charge>

    @GET("api/v1/cars/{carId}/charges/{chargeId}")
    suspend fun getChargeDetail(@Path("carId") id: Int, @Path("chargeId") cid: Int): Charge

    @GET("api/v1/cars/{carId}/charges/current")
    suspend fun getCurrentCharge(@Path("carId") id: Int): Charge?

    @GET("api/v1/cars/{carId}/battery-health")
    suspend fun getBatteryHealth(@Path("carId") id: Int): BatteryHealth

    @GET("api/v1/cars/{carId}/updates")
    suspend fun getUpdates(@Path("carId") id: Int): List<UpdateItem>
}
```

**工时**：1d (★ 直抄 matedroid 然后改端点路径)

### T2 — Mock 数据 + 离线缓存

**Mock**: `app/src/main/assets/mock_data.json` (复制 shared/mock_data.json)
**Room DB**: 直抄 matedroid `data/local/AppDatabase.kt` (表: drives, charges, battery_health)
**工时**：2d

### T3 — Onboarding (F-001)

**借鉴**：matedroid Settings screen (复用 UI pattern 改成引导页)
**工时**：2d

## 四、Phase 3: 核心页面 (W2-W3)

### T4 — Dashboard (F-003/004/005) ★ 直抄 matedroid

**策略**：90% 直抄 matedroid `DashboardScreen.kt`，微调：
- 2D 车辆图用 Coil (matedroid 已有)
- 状态徽章颜色 (matedroid 已有)
- 5s 轮询 (matedroid 用 WorkManager，改用 ViewModel + viewModelScope)
- 胎压卡片 (matedroid 已有)
- 充电进度卡片 (matedroid 已有)

```kotlin
// ★ 核心轮询逻辑 (直抄 matedroid ViewModel)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val carRepo: CarRepository,
    private val statusRepo: StatusRepository
) : ViewModel() {
    private val _status = MutableStateFlow<CarStatus?>(null)
    val status = _status.asStateFlow()

    init { viewModelScope.launch { while(true) { _status.value = statusRepo.get(carId); delay(5000) } } }
}
```

**工时**：2d (★ 直抄为主)

### T5 — Drives (F-006/007) ★ 直抄 matedroid

**DriveListScreen**: 直抄 matedroid `DriveListScreen.kt`
**DriveDetailScreen**: 直抄 matedroid `DriveDetailScreen.kt` (地图 + MPAndroidChart 曲线)
**轨迹抽稀**: 直抄 matedroid `RouteSimplifier.kt`
**工时**：2d

### T6 — Charges (F-008/009)

**ChargeListScreen**: 直抄 matedroid `ChargeListScreen.kt` + AC/DC 筛选
**ChargeDetailScreen**: 直抄 matedroid `ChargeDetailScreen.kt` (MPAndroidChart 功率曲线)
**工时**：2d

## 五、Phase 4: 分析页 + 本地化 (W3)

### T7 — Battery + Updates + Statistics ★ 直抄 matedroid

| 页面 | 借鉴 matedroid 源文件 | 工时 |
|---|---|---|
| BatteryHealthScreen | `BatteryHealthScreen.kt` ★ 直抄 | 1d |
| UpdatesScreen | — 新建, 列表 + update frequency chart | 1d |
| StatisticsScreen | `MileageScreen.kt` ★ 直抄钻取模式 | 1.5d |

### T8 — Web-Only 6 页 → Android

| 页面 | 实现要点 | 工时 |
|---|---|---|
| HeatmapScreen | LazyVerticalGrid + 颜色映射 | 1d |
| DestinationsScreen | 高德/Google Map + 表格排序 | 1d |
| EfficiencyScreen | MPAndroidChart ScatterChart | 0.5d |
| VampireScreen | 停车掉电计算 + LineChart | 0.5d |
| RangeScreen | 预估 vs 实际 LineChart | 0.5d |
| CostScreen | 月成本 StackedBar + 排行榜 | 1d |
| TimelineScreen | LazyColumn + 颜色编码卡片 | 0.5d |

**合计**：5d

### T9 — 中国本地化 ★ 借鉴 chinese-dashboards

| 任务 | 实现 | 工时 |
|---|---|---|
| 高德地图集成 | AMap SDK + GCJ-02 纠偏 | 1.5d |
| 分时电价 | Settings 配置 + 充电成本重算 | 1.5d |
| 中文资源 | `res/values-zh/strings.xml` | 0.5d |

**合计**：3.5d

## 六、Phase 5: 测试 + 上架

### T10 — 真机测试

| 测试项 | 设备 | 验收 |
|---|---|---|
| 启动 < 2s | Pixel 6+ | 冷启动计时 |
| Dashboard 5s 轮询 | Mock | 无崩溃 |
| 列表滚动 60fps | Profile GPU Render | 绿线 |
| 内存 < 250MB | Memory Profiler | — |
| 65 个 Compose 重组测试 | — | 无卡顿 |

**工时**：1.5d

### T11 — Google Play 上架

| 步骤 | 说明 |
|---|---|
| Google Play Console 创建 App | package: com.teslamatelink |
| 数据安全表 | "不收集用户数据" |
| App Bundle 签名 | Play App Signing |
| 内测发布 | 内部测试轨道 (50 人) |
| 截图 | 5 张 Google Play 截图 |
| 正式发布 | Production 轨道 |

**工时**：1.5d

## 七、总工时

| Phase | 任务 | 工时 | 直抄占比 |
|---|---|---|---|
| 1 | T0 工程 + CI | 4d | 90% |
| 2 | T1 API + T2 Mock + T3 Onboarding | 5d | 80% |
| 3 | T4 Dashboard + T5 Drives + T6 Charges | 6d | **95%** |
| 4 | T7 Battery+Stats + T8 分析6页 + T9 中国 | 10d | 60% |
| 5 | T10 测试 + T11 上架 | 3d | — |
| — | **总计** | **28d (~5.5 周)** | **~75%** |

## 八、双端工时对照

| 任务 | iOS | Android | Android优势 |
|---|---|---|---|
| 工程初始化 | 0d (已完成) | 4d | — |
| API Client | 0d | 1d | 直抄 matedroid |
| Mock + 缓存 | 0d | 2d | 直抄 matedroid |
| Onboarding | 0d | 2d | — |
| Dashboard | 0d | 2d | **直抄 95%** |
| Drives | 📋 1d | 2d | 直抄 95% |
| Charges | 📋 1d | 2d | 直抄 90% |
| Battery+Stats | 📋 1.5d | 2.5d | 直抄 80% |
| 分析 7 页 | 📋 5d | 5d | 平 |
| 中国本地化 | 📋 3.5d | 3.5d | 平 |
| 测试+上架 | 📋 5d | 3d | Android 审核更快 |
| **总计** | **~15d** | **~28d** | — |

> **Android 虽然工时多，但直抄占比 75%，实际执行更顺畅、风险更低。**

## 九、借鉴对照 (matedroid → MateLink Android)

| matedroid 源文件 | MateLink 目标 | 直抄度 |
|---|---|---|
| `build.gradle.kts` | `app/build.gradle.kts` | ★★★ 90% |
| `MainActivity.kt` | `MainActivity.kt` | ★★★ 80% |
| `data/api/*.kt` | `data/api/TeslaMateApi.kt` | ★★☆ 70% (改端点) |
| `data/local/*.kt` | `data/local/` | ★★★ 95% |
| `data/model/*.kt` | `data/api/model/` | ★★★ 90% |
| `ui/theme/*.kt` | `ui/theme/` | ★★★ 100% |
| `ui/navigation/*.kt` | `ui/navigation/NavGraph.kt` | ★★★ 85% |
| `ui/dashboard/*.kt` | `ui/dashboard/` | ★★★ 95% |
| `ui/drives/*.kt` | `ui/drives/` | ★★★ 95% |
| `ui/charges/*.kt` | `ui/charges/` | ★★★ 90% |
| `ui/battery/*.kt` | `ui/battery/` | ★★★ 95% |
| `ui/settings/*.kt` | `ui/settings/` | ★★★ 90% |
| `ui/components/*.kt` | `ui/components/` | ★★★ 95% |
| `widget/*.kt` | `widget/` | ★★★ 100% |
| `domain/route/*.kt` | `domain/route/` | ★★★ 100% |

## 十、下一步

1. Jovi 确认本计划
2. iOS 完成后 → Android 工程初始化
3. 按 T0→T11 顺序执行
4. Google Play 上架 → comet-verify → comet-archive
