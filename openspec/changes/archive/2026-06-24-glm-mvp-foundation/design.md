# Design: MateLink MVP Foundation

## 1. Architecture Overview

借鉴 matedroid 的 Clean Architecture 三层模型，双端共用：

```
┌─────────────────────────────────────────┐
│              UI Layer                    │  Compose / SwiftUI
│  (Screens, Components, Theme)           │
├─────────────────────────────────────────┤
│           Domain Layer                   │  纯 Kotlin / Swift
│  (UseCases, Models, Repository Ifaces)  │  ← 两端独立但设计一致
├─────────────────────────────────────────┤
│            Data Layer                    │
│  (ApiClient, MockClient, Cache, Token)  │
└──────────────┬──────────────────────────┘
               │ HTTPS
               ▼
      TeslaMateApi (用户自托管)
```

**为什么不是 KMP**：Jovi 决策原生双端。但 Android/iOS 保持相同的三层架构设计和接口契约，确保未来可迁移到 KMP 共享 Domain+Data 层。

## 2. Project Structure

### app_glm/ 根目录

```
app_glm/
├── README.md
├── .gitignore
├── android/                       # Android 工程
│   ├── build.gradle.kts           # 根构建
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/matelink/
│   │       │   │   ├── MateLinkApp.kt          # Application
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── data/
│   │       │   │   │   ├── api/                # ★ ApiClient
│   │       │   │   │   │   ├── TeslaMateApi.kt
│   │       │   │   │   │   └── model/          # DTO
│   │       │   │   │   ├── local/              # ★ Room + MMKV
│   │       │   │   │   │   ├── AppDatabase.kt
│   │       │   │   │   │   └── dao/
│   │       │   │   │   └── repository/         # ★ 实现
│   │       │   │   ├── domain/
│   │       │   │   │   ├── model/              # 领域模型
│   │       │   │   │   ├── usecase/
│   │       │   │   │   └── repository/         # 接口
│   │       │   │   ├── di/                     # Hilt 模块
│   │       │   │   ├── ui/
│   │       │   │   │   ├── theme/              # ★ Theme
│   │       │   │   │   ├── navigation/
│   │       │   │   │   ├── onboarding/         # F-001
│   │       │   │   │   ├── settings/           # F-013
│   │       │   │   │   └── components/         # 通用组件
│   │       │   │   └── mock/                   # ★ Mock 数据
│   │       │   │       └── MockData.kt
│   │       │   └── res/
│   │       └── test/                           # 单元测试
│   └── gradle/
├── ios/                           # iOS 工程（吸收 mimo Features/Core 分层）
│   ├── MateLink.xcodeproj/
│   ├── MateLink/
│   │   ├── App/
│   │   │   ├── MateLinkApp.swift           # @main 入口
│   │   │   └── ContentView.swift           # TabView 根导航
│   │   ├── Features/                       # ★ 功能模块
│   │   │   ├── Dashboard/
│   │   │   │   ├── DashboardView.swift
│   │   │   │   ├── DashboardViewModel.swift
│   │   │   │   └── Components/
│   │   │   │       ├── VehicleImageView.swift
│   │   │   │       ├── StatusBadgeView.swift
│   │   │   │       └── InfoCardView.swift
│   │   │   ├── Drives/
│   │   │   │   ├── DriveListView.swift
│   │   │   │   ├── DriveDetailView.swift
│   │   │   │   └── DriveViewModel.swift
│   │   │   ├── Charges/
│   │   │   │   ├── ChargeListView.swift
│   │   │   │   ├── ChargeDetailView.swift
│   │   │   │   └── ChargeViewModel.swift
│   │   │   ├── Battery/
│   │   │   │   ├── BatteryHealthView.swift
│   │   │   │   └── BatteryHealthViewModel.swift
│   │   │   ├── Settings/
│   │   │   │   ├── SettingsView.swift
│   │   │   │   └── SettingsViewModel.swift
│   │   │   └── Onboarding/
│   │   │       ├── WelcomeView.swift       # F-001
│   │   │       ├── ServerConfigView.swift
│   │   │       └── ConnectionTestView.swift
│   │   ├── Core/                           # ★ 基础设施
│   │   │   ├── API/
│   │   │   │   ├── APIClient.swift         # URLSession + async/await + 10s timeout
│   │   │   │   ├── APIEndpoints.swift
│   │   │   │   └── APIError.swift
│   │   │   ├── Models/                     # Codable structs
│   │   │   │   ├── Car.swift
│   │   │   │   ├── CarStatus.swift
│   │   │   │   ├── Charge.swift
│   │   │   │   ├── Drive.swift
│   │   │   │   └── BatteryHealth.swift
│   │   │   ├── Storage/
│   │   │   │   ├── CacheManager.swift      # SwiftData / SwiftData
│   │   │   │   └── KeychainManager.swift   # Token 安全存储
│   │   │   ├── Theme/
│   │   │   │   ├── AppTheme.swift           # Light/Dark color scheme
│   │   │   │   └── CarColorAccent.swift     # 车色 → Accent 映射
│   │   │   └── Utils/
│   │   │       ├── GCJ02Converter.swift     # 坐标纠偏
│   │   │       ├── RouteSimplifier.swift    # 轨迹抽稀 (Douglas-Peucker)
│   │   │       └── UnitFormatter.swift      # km/mi, °C/°F
│   │   ├── Resources/
│   │   │   ├── Assets.xcassets/            # 车辆图片 + App 图标 + 色板
│   │   │   ├── mock_data.json               # ★ 共享 Mock 数据
│   │   │   └── Localizable.xcstrings       # 中英文本地化
│   │   └── Tests/
│   └── MateLink.xcworkspace/
└── docs/                          # 工程文档
    └── ARCHITECTURE.md
```

## 3. Key Design Decisions

### 3.1 ApiClient — 双端设计同步

**Android (Retrofit + OkHttp)**：
```kotlin
// data/api/TeslaMateApi.kt
interface TeslaMateApi {
    @GET("api/ping")  suspend fun ping(): PingResponse
    @GET("api/readyz") suspend fun readyz(): HealthzResponse
    @GET("api/v1/cars") suspend fun getCars(): CarApiResponse
    @GET("api/v1/cars/{carId}/status") suspend fun getCarStatus(@Path("carId") id: Int): CarStatus
    // ... 13 more endpoints
}
```

**iOS (URLSession + async/await)**：
```swift
// Data/API/TeslaMateAPI.swift
actor TeslaMateAPI {
    private let baseURL: String
    private let token: String?
    private let session: URLSession

    func ping() async throws -> PingResponse { ... }
    func readyz() async throws -> HealthzResponse { ... }
    func getCars() async throws -> CarApiResponse { ... }
    func getCarStatus(carId: Int) async throws -> CarStatus { ... }
}
```

**统一错误模型**（双端一致）：
```
ApiError
├── networkUnreachable(Error)
├── unauthorized (HTTP 401)
├── serverError(Int, String)  (HTTP 5xx)
├── timeout
└── unknown(Error)
```

### 3.2 Mock Mode — 共享数据结构

Mock 数据源：`mock_data.json`（双端共用同一份 JSON）

```json
{
  "cars": [ { ... Model 3 Long Range ... }, { ... Model Y Performance ... } ],
  "status": { ... },
  "drives": [ ... 30 days ... ],
  "charges": [ ... 30 days ... ]
}
```

**Android**：`asset/mock_data.json` → Gson 反序列化
**iOS**：`Bundle.main.url(forResource: "mock_data")` → JSONDecoder

Mock ↔ Real 切换：
- 存 `MockMode` 标志到本地
- 不重启 App，通过 Repository 层的工厂模式切换：
  ```kotlin
  // Android
  fun createApiClient(): TeslaMateApi = if (settings.isMockMode)
      MockTeslaMateApi() else RealTeslaMateApi(baseUrl, token)
  ```

### 3.3 主题系统 — Apple-Like + 车色

配色方案（与 PRD `glm_09` 一致）：

| 用途 | Light | Dark |
|---|---|---|
| Background | #FFFFFF | #000000 |
| Surface | #F5F5F7 | #1C1C1E |
| Card | #FFFFFF + shadow | #2C2C2E |
| Primary Text | #1D1D1F | #FFFFFF |
| Accent | **基于车色** | 基于车色 |

车色 → Accent 映射：

| 车色 | Accent (Light) | Accent (Dark) |
|---|---|---|
| DeepBlue | #1E3A8A | #3B82F6 |
| RedMultiCoat | #B91C1C | #EF4444 |
| PearlWhite | #6B7280 | #9CA3AF |
| MidnightSilver | #4B5563 | #6B7280 |
| SolidBlack | #18181B | #3F3F46 |

**Android**：Material 3 Dynamic Color → 基于车色覆写 `seedColor`
**iOS**：`@Environment(\.self)` + custom `AccentColor` asset

### 3.4 离线缓存

| 层 | Android | iOS |
|---|---|---|
| KV 存储 | MMKV | UserDefaults (app group) |
| 结构化缓存 | Room (SQLite) | SwiftData |
| Token 安全存储 | EncryptedSharedPreferences | Keychain |

**缓存 key**：`cache_{carId}_{endpoint}_{page}`
**TTL**：列表 24h，详情 7d

### 3.5 导航架构

**Android**：Jetpack Navigation Compose
```kotlin
NavHost(navController, startDestination = "onboarding") {
    composable("onboarding") { OnboardingScreen() }
    composable("dashboard") { DashboardScreen() }
    composable("settings") { SettingsScreen() }
}
```

**iOS**：NavigationStack (iOS 16+)
```swift
NavigationStack(path: $router.path) {
    OnboardingView()
        .navigationDestination(for: Route.self) { route in ... }
}
```

## 4. Data Flow

```
User opens App
    │
    ├── 首次启动 → Onboarding Flow
    │   ├── 输入 URL + Token
    │   ├── ping → readyz → cars (三步检测)
    │   └── 成功 → 存 SecureStore → Dashboard
    │
    └── 非首次 → 读 SecureStore → Dashboard
                    │
                    ├── 网络可用 → Real ApiClient (Retrofit / URLSession)
                    │                  ↓
                    │              TeslaMateApi
                    │                  ↓
                    │              CacheManager.write(cache)
                    │
                    └── 网络不可用 → CacheManager.read → 显示缓存 + "Offline" banner
```

## 5. Technology Stack

| 层 | Android | iOS |
|---|---|---|
| Language | Kotlin 2.0+ | Swift 5.10+ |
| UI | Jetpack Compose + Material 3 | SwiftUI |
| Min SDK | API 26 (Android 8.0) | iOS 16.0 |
| Target SDK | API 35 (Android 15) | iOS 18.0 |
| Network | Retrofit 2 + OkHttp 4 + Gson | URLSession + async/await |
| DI | Hilt | Constructor Injection |
| Local DB | Room | SwiftData |
| KV | MMKV | UserDefaults |
| Secure Storage | EncryptedSharedPreferences | Keychain |
| Image | Coil | AsyncImage |
| Testing | JUnit 5 + MockK + Turbine | XCTest |
| Lint | Detekt + ktlint | SwiftLint |
| Build | Gradle Kotlin DSL | Xcode 16 |

## 6. Heavily Borrow From

| 模块 | Android 借鉴 | iOS 借鉴 |
|---|---|---|
| **整体架构** | matedroid Clean Architecture (data/domain/ui 三层) | 相同三层结构 |
| **API 设计** | matedroid `data/api/` Retrofit 接口 | teslamateapi Go struct → Swift Codable |
| **主题系统** | matedroid `ui/theme/` 车色调色板 | Tesla_Clone_Swiftui 配色 |
| **离线缓存** | matedroid `data/local/` Room 设计 | mytess 离线策略 |
| **Mock 模式** | teslamate-modern-dashboard Mock 方案 | 同左 |
| **错误处理** | matedroid 错误模型 | hedgiemate README 错误处理描述 |
| **设置页** | matedroid Settings Screen | t-buddy README 设置项清单 |
| **导航** | matedroid Navigation Compose | Tesla_Clone_Swiftui Views 结构 |

## 7. CI/CD

```yaml
# .github/workflows/pr-check.yml
jobs:
  android-lint:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew detekt ktlintCheck
  android-test:
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew test
  ios-lint:
    runs-on: macos-latest
    steps:
      - run: swiftlint --strict
```

## 8. Dependencies

- TeslaMate + TeslaMateApi v1.21+ (用户自托管，非本 change 范围)
- Android Studio Hedgehog+ / Xcode 16+

## 9. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| 双端维护成本高 | 严格保持相同的三层架构设计；数据层接口契约双端一致；未来可迁移 KMP |
| iOS 端无开源参考代码 | 借鉴 Tesla_Clone_Swiftui UI + hedgiemate/t-buddy 功能清单；自研 API client |
| 新人上手双代码库 | `app_glm/docs/ARCHITECTURE.md` 统一描述；Android 和 iOS 架构图一致 |
