# tesla-master-glm

<p align="center">
  <img src="https://img.shields.io/badge/platform-iOS%20%7C%20Android-blue.svg" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%7C%20Swift-orange.svg" alt="Language">
</p>

**tesla-master-glm** is a cross-platform mobile application for Tesla vehicle monitoring via TeslaMate API. provides Bluetooth connectivity and real-time vehicle data tracking for iOS (including Watch App) and Android.

---

## 📋 阶段验收状态 (2026-06-30)

> 基于 `app_glm/` 实际文件统计 + 17 轮交叉审核 + comet 工作流归档记录

### 总览

| 平台 | 完成度 | 测试 | CI/CD | 关键状态 |
|------|--------|------|-------|----------|
| **Android** | ~75% | 5 单元测试 | ❌ 缺失 | 20 Screen 齐全，RealCarRepository 已接入 |
| **iOS** | ~70% | 0（空目录） | ❌ 缺失 | 20 View + Watch + Widget，5 页面缺失 |
| **Web** | N/A | — | — | 仅 i18n 资源（de/fr/ja），非 web app |
| **Shared** | ✅ 完成 | — | — | api-types + mock + GCJ02 测试向量 |

### ✅ 已完成

#### Android (app_glm/android)
- **20 个 Screen** 全部实现：Dashboard / DriveList / DriveDetail / ChargeList / ChargeDetail / Battery / Cost / Destinations / Efficiency / Heatmap / Onboarding / Range / Settings / About / Statistics / DayDetail / MonthDetail / Timeline / Updates / Vampire
- **Clean Architecture**：7 Repository + 6 ViewModel + Hilt DI + Room (15 表) + Retrofit (16 端点)
- **RealCarRepository**（2026-06-29 归档）：Network-First + Room 缓存降级，`DelegatingCarRepository` 代理模式运行时切换 Mock/Real，`SettingsScreen` 数据源 Switch
- **中国本地化**：高德地图 + GCJ-02 坐标转换 + TOU 分时电价
- **后台能力**：WorkManager 同步 + 充电监控前台服务 + 3 种通知 + Glance Widget
- **单元测试**：MappersTest / RealCarRepositoryTest / DelegatingCarRepositoryTest / UrlSecurityTest（数据层覆盖）
- **17 轮交叉审核**：CRITICAL/HIGH 已清零

#### iOS (app_glm/ios)
- **20 个 View** + Widget + Watch App（4 文件）+ PhoneWCSessionManager
- 主线功能完整：Dashboard / Drive / Charge / Battery / Cost / Statistics / Timeline / Heatmap / Efficiency / Vampire / Range / Destinations / Updates / Vehicle3D / Settings / Onboarding / TariffConfig / NotificationManager
- WatchConnectivity 双向通信 + Complications

#### Shared (app_glm/shared)
- `api-types.ts` API 类型定义
- `mock_data.json` 开发用 mock 数据
- `gcj02_test_vectors.json` GCJ-02 转换测试向量

### 🔴 待完成

#### 高优先级
| # | 问题 | 平台 | 难度 |
|---|------|------|------|
| G-1 | 8 个分析页硬编码 mock（Battery/Heatmap/Efficiency/Vampire/Range/Destinations/Cost/Updates）。RealCarRepository 已打通切换路径，待真实后端接入验证 | Android | 中 |
| G-2 | iOS Drive/Charge 详情页图表用 sin()/random() 假数据 | iOS | 中 |
| G-3 | Android CI/CD 完全缺失（无 .github/workflows） | Android | 小 |
| G-4 | Android 上架配置缺失（play publisher/隐私政策/截图） | Android | 中 |
| G-5 | iOS 5 个声明功能页完全缺失：CurrentCharge / Mileage / Countries / Sentry / Trips（连占位文件都没有） | iOS | 大 |
| G-6 | iOS Settings 缺单位/时区设置 UI | iOS | 小 |

#### iOS/Watch 审核遗留（需 Mac/Xcode，17 轮审核发现）
| # | 严重级别 | 问题 |
|---|---|---|
| I-1 | CRITICAL | AnnualReportPDFView @State 数据竞争（Task.detached 读 @State） |
| I-2 | CRITICAL | PhoneWCSessionManager WCSession delegate 线程不安全 |
| I-3 | HIGH | DashboardView Timer 未取消（5s 轮询泄漏） |
| I-4 | HIGH | writeWidgetData 每 5s 重建 ImageRenderer |
| I-5 | HIGH | KeychainHelper.save 丢弃 SecItemAdd 返回值 |
| I-6 | HIGH | Vehicle3DView cacheNodes() fallback 分支未调用 |
| I-7 | HIGH | WatchConnectivityManager 缺 @MainActor |

#### 工程化短板
- **测试覆盖**：Android 仅数据层（UI/ViewModel 零测试），iOS 测试目录空壳
- **CI/CD**：双端均无流水线
- **Web 平台**：未启动（`web_matelink/` 仅 3 个 i18n JSON，无 app 框架）
- **真实后端联调**：RealCarRepository 代码就位但未对真实 TeslaMate 后端验证

> 完整待办清单见项目根 [`docs/TODO.md`](../docs/TODO.md)

---

## Features

### 🚗 Vehicle Monitoring
- Real-time vehicle status tracking
- Battery health monitoring
- Tire pressure monitoring (TPMS)
- Charge session tracking
- Drive history and statistics

### 💰 Cost Calculation
- Time-of-Use (ToU) tariff support
- Multiple pricing tiers (peak / flat / valley)
- Charging cost estimation
- Monthly/annual energy cost statistics

### 🗺️ Navigation & Mapping
- GCJ-02 coordinate conversion for China map compatibility
- Drive route heatmap visualization
- Geocoded destination tracking
- Amap integration

### 📊 Efficiency Analysis
- Energy efficiency tracking by trip
- Historical efficiency comparison
- Vampire discharge monitoring ("vampire mode")

### 🔔 Notifications & Widgets
- Android 12+ widgets for vehicle status
- iOS Widget support
- Charging completion notifications
- Sentry monitoring alerts

### 🌐 Multi-language Support
- English (en)
- Simplified Chinese (zh-Hans)
- German (de)
- French (fr)
- Japanese (ja)

## Project Structure

```
app_glm/
├── android/                    # Android application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/teslamatelink/
│   │       │   ├── data/          # Data layer (API, local DB, repository)
│   │       │   ├── domain/        # Business logic
│   │       │   ├── ui/            # UI screens (Compose)
│   │       │   └── notification/ # Notifications & workers
│   │       └── res/              # Resources & localization
│   └── build.gradle.kts
├── ios/                        # iOS application (Swift + SwiftUI)
│   ├── MateLink/
│   │   ├── App/                 # App entry point
│   │   ├── Core/                # Core models & utilities
│   │   ├── Features/            # Feature modules
│   │   └── Widget/              # iOS Widget
│   └── MateLink Watch App/      # watchOS Companion App
├── shared/                     # Shared types & mock data
│   ├── api-types.ts             # API type definitions
│   └── mock_data.json          # Mock data for development
└── web_matelink/               # i18n resource directory (de/fr/ja messages, NOT a web app)
    └── src/messages/           # localized message JSON files
```

## Tech Stack

### Android
- **Kotlin** + **Jetpack Compose** - Modern declarative UI
- **Hilt** - Dependency injection
- **Room** - Local database
- **WorkManager** - Background sync
- **Paging 3** - Pagination
- **Jetpack Navigation** - In-app navigation

### iOS
- **Swift** + **SwiftUI** - declarative UI framework
- **WatchConnectivity** - iOS-watchOS communication
- **Core Location** - Location services

## Architecture

Clean architecture with separation of concerns:
- **Data Layer**: API sources, local database, repositories
- **Domain Layer**: Use cases, business logic, models
- **Presentation Layer**: UI, view models, state management

## Building

### Prerequisites
- Android: Android Studio Electric Eel+, JDK 17
- iOS: Xcode 16+, Swift 6
- Android `minSdk = 28`, `targetSdk = 35`

### Android
```bash
cd android
./gradlew assembleDebug
```

### iOS
```bash
# Open with Xcode
open ios/MateLink.xcodeproj
```

## Configuration

### AMap API Key
For map functionality, obtain an API key from [AMAP](https://lbs.amap.com/) and set it in `local.properties`:
```properties
AMAP_API_KEY=your_api_key_here
```

## Credits

Based on the original [TeslaMate](https://github.com/adriankumpf/teslamate) project for vehicle data logging.

## License

Copyright © 2024 Jovi. All rights reserved.
