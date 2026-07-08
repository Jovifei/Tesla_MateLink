# Tesla MateLink GLM

Language / 语言: [中文](#中文) | [English](#english)

---

## 中文

### 项目定位

`app_glm` 是 Tesla MateLink 的并行/参考实现仓库。它保留了较完整的 Android、iOS、Watch、Widget、共享数据模型和本地化资源，可作为 `app_mimo` 主线开发时的工程参考。

它不等同于当前产品主线。当前优先推进的应用是 `app_mimo`；`app_glm` 更适合用于参考以下方向：

- Android Clean Architecture 分层方式
- Repository、Room、Retrofit、mock/real 切换策略
- 后台同步、通知、Widget 和 Watch App 的工程组织
- TeslaMate API 类型、mock 数据和中国地图坐标处理

### 功能范围

| 功能域 | 说明 |
| --- | --- |
| 车辆状态 | 实时车辆摘要、电量、续航、胎压、在线状态和基础控制状态展示。 |
| 行程 | 行程列表、行程详情、统计页、目的地和时间线。 |
| 充电 | 充电历史、充电详情、费用、分时电价和统计分析。 |
| 能耗分析 | 电池、效率、热力图、待机耗电、续航分析和年度/月度维度页面。 |
| 地图与中国适配 | 高德地图接入、GCJ-02 坐标转换和本地化资源。 |
| 扩展能力 | Android Widget、iOS Widget、watchOS Companion、通知和后台任务源码。 |

### 工程结构

```text
app_glm/
|- android/                    Android app, Kotlin, Jetpack Compose
|  `- app/src/main/java/com/teslamatelink/
|     |- data/                 API, Room database, repositories
|     |- domain/               Domain models and mapping logic
|     |- ui/                   Compose screens and view models
|     `- notification/         Workers, services, notifications
|- ios/                        iOS SwiftUI app
|  |- MateLink/                iOS app source
|  |- MateLink Watch App/      watchOS companion source
|  `- MateLinkWidget/          iOS widget source
|- shared/                     Shared API types, mock data, test vectors
`- web_matelink/               Localization resources, not a complete web app
```

### 当前状态

| 平台 | 状态 |
| --- | --- |
| Android | 页面和数据层较完整，包含 Repository、Room、Retrofit、Hilt、WorkManager、通知和 Widget 相关源码。 |
| iOS | SwiftUI、Widget、Watch App 源码存在，但最终编译、签名、Widget/Watch wiring 仍需要 Mac/Xcode 实测。 |
| Shared | 包含 API 类型、mock 数据和 GCJ-02 测试向量。 |
| Web | 当前主要是本地化资源目录，不是完整 Web 应用。 |

### Android 开发

要求：

- Android Studio
- JDK 17
- Android SDK

常用命令：

```powershell
cd E:\project\tesla_master\app_glm\android
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

配置提示：

- 高德地图能力需要在 `local.properties` 中配置 API Key。
- `local.properties`、`.gradle/`、`.idea/` 和 `app/build/` 属于本地生成内容，不应提交。

### iOS / Watch / Widget 开发

要求：

- Mac
- Xcode
- 对应 Apple Developer 签名配置

说明：

- iOS、Widget 和 Watch 源码可作为产品能力参考，但必须在 Mac/Xcode 中确认 target、entitlements、App Group、WatchConnectivity 和签名配置。
- Windows 侧只能完成源码审查，不能声明 Apple 平台构建通过。

### 共享资源

`shared/` 用于保存跨端参考数据：

- `api-types.ts`：TeslaMate API 类型定义参考。
- `mock_data.json`：开发与页面演示用 mock 数据。
- `gcj02_test_vectors.json`：GCJ-02 坐标转换测试向量。

### 与 app_mimo 的关系

- `app_mimo` 是当前主线产品仓库。
- `app_glm` 是参考实现，不应无差别复制到 `app_mimo`。
- 从 `app_glm` 借鉴代码时，应先确认 `app_mimo` 的当前架构、路由、主题和 mock/real 数据边界。

### Git

当前远端：

```text
https://github.com/Jovifei/tesla-master-glm.git
```

推荐流程：

```powershell
cd E:\project\tesla_master\app_glm
git pull --rebase --autostash origin main
git status
git add <changed-files>
git commit -m "docs: describe app_glm"
git push origin main
```

---

## English

### Purpose

`app_glm` is a parallel/reference implementation of Tesla MateLink. It keeps a broad set of Android, iOS, Watch, Widget, shared model, and localization assets that can be used as engineering reference while developing the current `app_mimo` product track.

It is not the current primary product line. `app_mimo` is the active app track; `app_glm` is best used as reference for:

- Android Clean Architecture layering
- Repository, Room, Retrofit, and mock/real switching patterns
- Background sync, notifications, widgets, and Watch App organization
- TeslaMate API types, mock data, and China map coordinate handling

### Feature Scope

| Area | Description |
| --- | --- |
| Vehicle status | Vehicle summary, battery, range, tire pressure, online state, and basic control states. |
| Trips | Drive list, drive detail, statistics, destinations, and timeline. |
| Charging | Charge history, charge detail, costs, time-of-use tariffs, and statistics. |
| Energy analytics | Battery, efficiency, heatmap, vampire drain, range analysis, and yearly/monthly pages. |
| Maps and China support | AMap integration, GCJ-02 coordinate conversion, and localization resources. |
| Extensions | Android Widget, iOS Widget, watchOS companion, notifications, and background task source. |

### Project Structure

```text
app_glm/
|- android/                    Android app, Kotlin, Jetpack Compose
|  `- app/src/main/java/com/teslamatelink/
|     |- data/                 API, Room database, repositories
|     |- domain/               Domain models and mapping logic
|     |- ui/                   Compose screens and view models
|     `- notification/         Workers, services, notifications
|- ios/                        iOS SwiftUI app
|  |- MateLink/                iOS app source
|  |- MateLink Watch App/      watchOS companion source
|  `- MateLinkWidget/          iOS widget source
|- shared/                     Shared API types, mock data, test vectors
`- web_matelink/               Localization resources, not a complete web app
```

### Current Status

| Platform | Status |
| --- | --- |
| Android | Broad source coverage, including repositories, Room, Retrofit, Hilt, WorkManager, notifications, and Widget-related code. |
| iOS | SwiftUI, Widget, and Watch App sources exist, but final build, signing, widget/watch wiring, and device proof require Mac/Xcode. |
| Shared | API types, mock data, and GCJ-02 test vectors are present. |
| Web | Currently localization resources only, not a complete web application. |

### Android Development

Requirements:

- Android Studio
- JDK 17
- Android SDK

Common commands:

```powershell
cd E:\project\tesla_master\app_glm\android
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Configuration notes:

- AMap features require an API key in `local.properties`.
- `local.properties`, `.gradle/`, `.idea/`, and `app/build/` are local generated files and should not be committed.

### iOS / Watch / Widget Development

Requirements:

- Mac
- Xcode
- Apple Developer signing setup

Notes:

- iOS, Widget, and Watch sources can be used as product capability references, but target wiring, entitlements, App Groups, WatchConnectivity, and signing must be verified on Mac/Xcode.
- Windows-side review is source-level only and does not prove Apple platform builds.

### Shared Resources

`shared/` contains cross-platform reference data:

- `api-types.ts`: TeslaMate API type reference.
- `mock_data.json`: mock data for development and screen demos.
- `gcj02_test_vectors.json`: GCJ-02 coordinate conversion test vectors.

### Relationship To app_mimo

- `app_mimo` is the current primary product repository.
- `app_glm` is a reference implementation and should not be copied wholesale into `app_mimo`.
- Before borrowing code from `app_glm`, check `app_mimo`'s current architecture, routing, theme, and mock/real data boundaries.

### Git

Remote:

```text
https://github.com/Jovifei/tesla-master-glm.git
```

Recommended workflow:

```powershell
cd E:\project\tesla_master\app_glm
git pull --rebase --autostash origin main
git status
git add <changed-files>
git commit -m "docs: describe app_glm"
git push origin main
```
