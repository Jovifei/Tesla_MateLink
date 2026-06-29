# MateLink MVP 实施计划

> 品牌：**MateLink** ∣ 技术方案：**原生双端** (Kotlin + SwiftUI) + Web (React + Vite)
> 基于：PRD glm_00~09 + 20+ 参考仓库借鉴
> 日期：2026-06-23

## 一、核心决策

| 决策 | 结果 |
|---|---|
| 品牌名 | **Tesla_MateLink** |
| 技术方案 | **原生双端** (iOS Swift 优先 → Android Kotlin 跟进) + Web (React + Vite) |
| 商业模式 | 前期免费，后期可选付费功能 |
| 视觉风格 | Apple-Like + 车色 Accent |
| 差异化 | 中国本地化 > 3D 车辆 > 跨平台开源 > AI 查询 |
| 开发顺序 | **先 iOS 后 Android**（iOS 无完整开源参考，先做验证方案可行性；Android 有 matedroid 完整代码，后做更快） |
| iOS 缓存 | **SwiftData**（iOS 17+ 原生方案） |

## 二、工程结构

```
E:\project\tesla_master\
├── docs/                          # 文档区
│   ├── PRD/glm/                   # 10 份产品需求文档
│   └── git_ref/                   # 20+ 参考仓库源码
│       ├── glm/                   # Tier-1 核心参考
│       ├── mimo/                  # mimo 侧参考
│       └── openclaw/              # openclaw 侧参考
├── app_glm/                       # ★ Android + iOS 工程
│   ├── shared/                    # #0 共享层 (mock_data + api-types)
│   │   ├── mock_data.json
│   │   ├── api-types.ts
│   │   └── README.md
│   ├── android/                   # Kotlin + Jetpack Compose
│   ├── ios/                       # Swift + SwiftUI
│   └── docs/                      # 工语文档
├── web_matelink/                  # ★ Web 仪表盘
│   ├── src/                       # React + Vite + TS
│   ├── Dockerfile                 # nginx 部署
│   └── package.json
└── openspec/changes/              # OpenSpec 变更管理
    ├── glm-mvp-foundation/        # #1 ✅ open→design
    ├── glm-mvp-web/               # #7 ✅ open→design
    └── (待创建 #2-6)
```

## 三、8 个 Change 总览 (Web-First, iOS 优先)

> **品牌**：Tesla_MateLink  |  **iOS 缓存**：SwiftData  |  **开发顺序**：先 iOS → 后 Android

| # | Change | 平台 | 周期 | 依赖 | 状态 |
|---|---|---|---|---|---|
| 0 | `glm-shared-base` | 📁 共享 | 2d | 无 | ✅ design 就绪 |
| 7 | `glm-mvp-web` | 🌐 Web | 15d (W1-W3) | #0 | ✅ design 就绪 |
| ↳ | **🔴 Jovi 确认 Web 交互** | — | — | #7 | ⏳ 阻塞点 |
| 1 | `glm-mvp-foundation` | 🍏 iOS | 5d (W4) | #0 | ✅ design 就绪 |
| 2 | `glm-mvp-dashboard` | 🍏 iOS | 5d (W5) | #1 | 📋 |
| 3 | `glm-mvp-drives` | 🍏 iOS | 5d (W6) | #1 | 📋 |
| 4 | `glm-mvp-charges` | 🍏 iOS | 5d (W7) | #1 | 📋 |
| 5 | `glm-mvp-ios-finalize` | 🍏 iOS + 中国本地化 | 5d (W8) | #1-4 | 📋 |
| 6 | `glm-mvp-android` | 🤖 Android | 15d (W9-W12) | Jovi confirm + iOS 对齐 | 📋 |
| 6b| `glm-mvp-release` | 📱 双端 | 10d (W13-W14) | #1-6 | 📋 |

```
Week 1   │ #0 Shared Base (2d) ──────┘
Week 1-3 │ #7 Web Dashboard (15d) ──── 12 页完整交互 (React + Vite + Tailwind)
         │         ↓  Jovi 确认交互
Week 4-8 │ Phase 1: iOS 优先 ───────── Foundation → Dashboard → Drives → Charges → Battery + 中国本地化
Week 9-12│ Phase 2: Android 跟进 ───── 参考 matedroid 完整代码，与 iOS 功能对齐
Week 13-14│ Phase 3: Release ────────── 集成测试 + App Store + Google Play
```

**关键阻塞点**：Web 仪表盘完成后，Jovi 确认交互 → 再启动 iOS App 端开发。

### 平台标识

| 标识 | 含义 |
|---|---|
| 📱 | 移动端 |
| 🍏 | iOS (Phase 1 优先) |
| 🤖 | Android (Phase 2 跟进) |
| 🌐 | Web |
| 📁 | 共享层 |

## 四、#1 glm-mvp-foundation 详细任务 (19 任务, 18d)

| ID | 任务 | 工时 | 借鉴 |
|---|---|---|---|
| T-001 | 创建 Android 工程 (Compose + Gradle) | 1d | matedroid build.gradle.kts |
| T-002 | 创建 iOS 工程 (SwiftUI + Xcode) | 1d | Tesla_Clone_Swiftui |
| T-003 | CI/CD 骨架 (GitHub Actions) | 0.5d | — |
| T-004 | 工语文档 ARCHITECTURE.md | 1.5d | matedroid CLAUDE.md |
| T-005 | Android API Client (Retrofit) | 2.5d | matedroid data/api/ |
| T-006 | iOS API Client (URLSession) | 2.5d | teslamateapi Go 源码 |
| T-007 | Mock 数据 mock_data.json | 1d | teslamate-modern-dashboard |
| T-008 | Android 离线缓存 (Room + MMKV) | 2d | matedroid data/local/ |
| T-009 | iOS 离线缓存 (Core Data + Keychain) | 2d | mytess 离线策略 |
| T-010 | Android Onboarding (F-001) | 1.5d | matedroid settings |
| T-011 | iOS Onboarding (F-001) | 1.5d | 同左 |
| T-012 | Android Theme (F-012) | 1.5d | matedroid ui/theme/ |
| T-013 | iOS Theme (F-012) | 1.5d | Tesla_Clone_Swiftui |
| T-014 | Android Settings (F-013) | 1.5d | matedroid settings |
| T-015 | iOS Settings (F-013) | 1.5d | 同左 |
| T-016 | Android Car Switcher (F-002) | 1d | matedroid 车选 |
| T-017 | iOS Car Switcher (F-002) | 1d | 同左 |
| T-018 | E2E 集成测试 | 0.5d | — |
| T-019 | 代码评审 | 0.5d | Detekt + SwiftLint |

## 五、#7 glm-mvp-web 详细任务 (13 任务, 15d)

| ID | 任务 | 工时 | 借鉴 |
|---|---|---|---|
| TW-001 | Vite + React + TS 初始化 | 1d | teslamate-modern-dashboard |
| TW-002 | API Client + Mock (Web) | 1d | #1 T-005 设计 |
| TW-003 | Layout + 侧边栏 + 路由 | 1d | TeslamateCyberUI |
| TW-004 | Dashboard 页 | 1.5d | teslamate-modern-dashboard |
| TW-005 | Drives + Drive Detail | 2d | TeslamateCyberUI |
| TW-006 | Charges + Charge Detail | 1.5d | TeslamateCyberUI |
| TW-007 | Battery Health | 1d | matedroid |
| TW-008 | Statistics 钻取 | 2d | teslamate-modern-dashboard |
| TW-009 | Heatmap 热力图 | 1.5d | teslamate-modern-dashboard |
| TW-010 | Top Destinations | 1d | teslamate-modern-dashboard |
| TW-011 | Efficiency Curve | 1d | teslamate efficiency SQL |
| TW-012 | Settings + Mock 切换 | 1d | #1 T-014 设计 |
| TW-013 | Docker + CI/CD | 0.5d | nginx 标准 |

## 六、Web 交互设计摘要

| 页面 | 核心交互 | 数据 |
|---|---|---|
| **Dashboard** | 实时卡片 + 7d 趋势图 | GET /status | 5s 轮询 |
| **Drives** | 列表 + 分页 + 日期筛选 | GET /drives?page=N | 按需 |
| **Drive Detail** | 全屏 Leaflet 轨迹 + Recharts 速度/功率/海拔曲线 | GET /drives/:id | 按需 |
| **Charges** | 列表 + AC/DC 筛选 + 月度柱状图 | GET /charges | 按需 |
| **Charge Detail** | 充电功率曲线 (Recharts Area) + 缩放 | GET /charges/:id | 按需 |
| **Battery Health** | 衰减趋势 + Gauge 图 | GET /battery-health | 1h |
| **Statistics** | ★ Year→Month→Day→Drive 四级钻取 | GET /drives (聚合) | 按需 |
| **Heatmap** | ★ 15 天 × 24 时 热力图 hover/click | GET /drives (聚合) | 按需 |
| **Top Destinations** | ★ Leaflet Cluster markers + 大小/颜色编码 + popup | 本地聚合 | 按需 |
| **Efficiency Curve** | ★ 散点回归 + 温度颜色编码 + 区域选择 | 借鉴 efficiency SQL | 按需 |

## 七、开发环境

| 工具 | 版本 | 用途 |
|---|---|---|
| Android Studio | Hedgehog+ | Android 开发 |
| Xcode | 16+ | iOS 开发（需要 Mac） |
| JDK | 17 | Android 构建 |
| Kotlin | 2.0+ | Android 语言 |
| Swift | 5.10+ | iOS 语言 |
| Node.js | 20 LTS | Web 开发 |
| Docker | latest | Web 部署 |

## 八、下一步

- ✅ 实施计划文档完成 (Web-First + iOS 优先)
- ✅ #0 `glm-shared-base` open 完成 → design 就绪
- ✅ #7 `glm-mvp-web` open 完成 → design 就绪
- ✅ #1 `glm-mvp-foundation` open 完成 → design 就绪 (已改为 iOS 优先)
- ⏭ **立即行动**：先走 #0 shared-base 的 build（2d），再走 #7 web 的 build（15d），Jovi 确认交互后，启动 #1 iOS Foundation

**三阶段执行顺序**：
1. **#0 shared-base** (2d) → 产出 `app_glm/shared/mock_data.json` + `api-types.ts`
2. **#7 glm-mvp-web** (15d) → 产出 `web_matelink/` 12 页 Web 仪表盘 → **Jovi 确认交互**
3. **Phase 1: iOS 优先** (#1-5, W4-W8) → iOS App (Swift + SwiftUI + SwiftData)
4. **Phase 2: Android 跟进** (#6, W9-W12) → Android App (Kotlin + Compose, 参考 matedroid)
5. **Phase 3: Release** (#6b, W13-W14) → App Store + Google Play
