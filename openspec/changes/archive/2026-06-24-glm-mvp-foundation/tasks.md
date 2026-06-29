# Tasks: MateLink MVP Foundation

## Phase: Foundation Setup (Week 1)

- [x] **T-001** — 创建 Android 工程 (`app_glm/android/`)
  - `gradle init` + Kotlin DSL + Jetpack Compose + Material 3
  - 配置 Detekt + ktlint + Gradle wrapper
  - 借鉴：matedroid `build.gradle.kts`
  - 验证：`./gradlew assembleDebug` 成功

- [x] **T-002** — 创建 iOS 工程 (`app_glm/ios/`)
  - Xcode 16 + SwiftUI App 模板 + Swift 5.10
  - 配置 SwiftLint + Build Settings (iOS 16 target)
  - 借鉴：Tesla_Clone_Swiftui 工程结构
  - 验证：Xcode Build 通过

- [x] **T-003** — 搭建 CI/CD 骨架
  - GitHub Actions: PR 触发 lint + test
  - Android: detekt + ktlint + test
  - iOS: swiftlint (macOS runner)
  - 验证：PR 触发流水线通过

- [x] **T-004** — 创建工语文档 `app_glm/docs/ARCHITECTURE.md`
  - Clean Architecture 三层图
  - 目录结构说明
  - 技术栈清单
  - 借鉴指南（参考哪些仓库的哪些文件）

## Phase: Data Layer (Week 1-2)

- [x] **T-005** — Android API Client
  - `data/api/TeslaMateApi.kt`: Retrofit 接口（16 个端点）
  - `data/api/model/`: DTO 类（Car, CarStatus, Drive, Charge, BatteryHealth, Update, GlobalSettings）
  - `data/api/RealTeslaMateApi.kt`: 实现 (OkHttp + Bearer Token interceptor)
  - `data/api/MockTeslaMateApi.kt`: Mock 实现 (读 mock_data.json)
  - 错误模型：`ApiError` sealed class
  - 借鉴：matedroid `data/api/`
  - 验证：Mock 模式下 getCars() 返回 2 辆车

- [x] **T-006** — iOS API Client
  - `Data/API/TeslaMateAPI.swift`: actor-based URLSession 客户端
  - `Data/API/Models/`: Codable structs (同 Android DTO 字段一致)
  - Mock 实现：`MockTeslaMateAPI.swift`
  - 错误模型：`ApiError` enum
  - 验证：Mock 模式下 getCars() 返回 2 辆车

- [x] **T-007** — Mock 数据 (`app_glm/mock_data.json`)
  - 2 辆虚拟车 (Model 3 LR DeepBlue + Model Y Perf RedMultiCoat)
  - 30 天 drives (60 条) + charges (30 条)
  - Battery health 数据 + 软件更新历史
  - 实时 status（静态 mock）
  - 借鉴：teslamate-modern-dashboard Mock 数据
  - 验证：JSON 格式正确（Android Gson / iOS JSONDecoder 均可解析）

- [x] **T-008** — Android 离线缓存
  - `data/local/AppDatabase.kt`: Room DB (drives, charges, battery_health 三表)
  - `data/local/CacheManager.kt`: MMKV 封装 (设置/状态/缓存 TTL)
  - 借鉴：matedroid `data/local/`
  - 验证：缓存写入 → 断网 → 从缓存读取成功

- [x] **T-009** — iOS 离线缓存
  - `Data/Local/Persistence.swift`: Core Data Stack
  - `Data/Local/CacheManager.swift`: 缓存 TTL + Keychain 封装
  - 验证：同上

## Phase: Onboarding (Week 2)

- [x] **T-010** — Android Onboarding (F-001)
  - `ui/onboarding/OnboardingScreen.kt`: Welcome + URL/Token 输入
  - 三步连通性检测：ping → readyz → cars
  - 成功跳转 Dashboard（占位），失败显示具体哪步失败
  - SSOT: URL + Token → EncryptedSharedPreferences
  - 借鉴：matedroid settings screen 布局
  - 验证：Mock 模式三步全过 → 跳转；填错 URL → 显示 "Cannot reach server"

- [x] **T-011** — iOS Onboarding (F-001)
  - `UI/Onboarding/OnboardingView.swift`: 同上设计
  - 三步检测 + 错误映射
  - SSOT: URL + Token → Keychain
  - 验证：同上

## Phase: Theme + Settings (Week 2)

- [x] **T-012** — Android Theme (F-012)
  - `ui/theme/Theme.kt`: Material 3 动态主题
  - `ui/theme/Color.kt`: 车色 → Accent 映射
  - Light / Dark / System 三态切换
  - 存储选择到 MMKV
  - 借鉴：matedroid `ui/theme/`
  - 验证：切换 Dark → 全 App 变色；车色 DeepBlue → Accent #1E3A8A

- [x] **T-013** — iOS Theme (F-012)
  - `UI/Theme/AppTheme.swift`: ColorScheme 定义
  - Light / Dark / System 三态
  - 车色调色板
  - 验证：同上

- [x] **T-014** — Android Settings (F-013)
  - `ui/settings/SettingsScreen.kt`: URL/Token 编辑 + 单位/时区 + Theme 选择
  - Mock Mode 开关（不重启生效）
  - 借鉴：matedroid settings
  - 验证：修改 URL → 缓存更新 → 下次启动使用新 URL

- [x] **T-015** — iOS Settings (F-013)
  - `UI/Settings/SettingsView.swift`: 同上
  - 验证：同上

## Phase: Car Selection (Week 2)

- [x] **T-016** — Android Car Switcher (F-002)
  - `ui/components/CarSwitcher.kt`: Bottom Sheet 显示车辆列表
  - 当前车高亮 + 选中切换
  - 存储 currentCarId 到 MMKV
  - 借鉴：matedroid 车辆选择 UI
  - 验证：2 辆 Mock 车可切换 → Dashboard 数据跟随变化

- [x] **T-017** — iOS Car Switcher (F-002)
  - `UI/Components/CarSwitcher.swift`: .sheet 弹出车辆列表
  - 验证：同上

## Week 2 收尾

- [x] **T-018** — E2E 集成测试
  - Mock 模式全流程：启动 → Onboarding (三步全过) → Dashboard (空壳) → Settings → 切车 → 换 Theme
  - 验证：全流程无崩溃

- [x] **T-019** — 代码评审
  - Android: Detekt 无 error / ktlint 通过
  - iOS: SwiftLint 无 error
  - CI 全部绿色

---

## Dependencies

- **无外部依赖**：本 change 是 MVP 基础，不依赖其他 change
- **被依赖**：所有后续 MVP change (#2-5) 依赖本 change

## Estimated Effort

| 阶段 | 工时 |
|---|---|
| Foundation Setup (T-001 ~ T-004) | 4d |
| Data Layer (T-005 ~ T-009) | 5d |
| Onboarding (T-010 ~ T-011) | 3d |
| Theme + Settings (T-012 ~ T-015) | 3d |
| Car Selection (T-016 ~ T-017) | 2d |
| 集成测试 + 审核 (T-018 ~ T-019) | 1d |
| **总计** | **18d (~3.5 周)** |
