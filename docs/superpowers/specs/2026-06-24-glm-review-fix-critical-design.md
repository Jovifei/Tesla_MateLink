---
comet_change: glm-review-fix-critical
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-24-glm-review-fix-critical
status: final
---

# 第7轮交叉审核 — 停船级修复设计

## Context

3 个 code-reviewer agent 并行审查 iOS (29 Swift) + Android (95+ Kotlin)，发现 66 个问题。本文档覆盖 22 个停船级问题（8 BLOCKER + 14 CRITICAL）。

## Goals

1. iOS/Android 代码可编译
2. 安全漏洞修复（token 保护、数据库安全、HTTP 日志降级）
3. Android 暗色主题生效
4. Android Dashboard 集成高德地图
5. Android Statistics 下钻导航
6. Android GCJ-02 坐标转换
7. iOS MockData 优雅降级 + ISO 8601 统一

## Architecture

```
iOS (Swift 5.10)                    Android (Kotlin 2.0)
├── Core/API/ApiClient.swift ← B1   ├── domain/
│   └── URL path 规范化              │   ├── TripDetector.kt ← B2
├── Core/Utils/                      │   ├── TripAggregator.kt ← B3
│   └── ISO8601Parser.swift ← NEW    │   ├── GCJ02Converter.kt ← NEW (B8)
├── Features/                        │   └── UnitFormatter.kt
│   ├── OnboardingView ← B1          ├── data/
│   ├── DriveListView ← B1           │   ├── local/AppDatabase.kt ← C3
│   ├── ChargeListView ← B1          │   └── local/SettingsDataStore.kt ← C5
│   ├── UpdatesView ← C2             ├── di/AppModule.kt ← C3,C4,C6
│   ├── SettingsView ← H1            ├── ui/
│   ├── DashboardView                │   ├── theme/Theme.kt ← C5
│   └── ...8 分析页面                 │   ├── dashboard/DashboardScreen.kt ← B4,B5
└── MockAPI ← C1                     │   ├── onboarding/OnboardingViewModel ← C6
                                     │   ├── statistics/
                                     │   │   ├── StatisticsScreen.kt ← B7
                                     │   │   ├── MonthDetailScreen.kt ← NEW
                                     │   │   └── DayDetailScreen.kt ← NEW
                                     │   └── widget/CarWidgetUpdateWorker.kt
                                     └── AndroidManifest.xml
```

## Key Decisions

| # | Decision | Rationale |
|---|---|---|
| D-1 | URL 规范化在 ApiClient 内部 | 防御式修复，单点解决，不改调用方 |
| D-2 | parseDateTime 私有别名 | 不改 8 个调用点，零风险 |
| D-3 | Entity.toSummary() 映射 | 保持 Clean Architecture 分层 |
| D-4 | 移除 destructive migration | 用户数据不可恢复 |
| D-5 | BuildConfig.DEBUG 控制日志 | 标准做法 |
| D-6 | DataStore + StateFlow 统一 AppSettings | 单一真相源 |
| D-7 | 高德 3D Map SDK | 用户指定，配合 GCJ-02 |
| D-8 | eviltransform 算法移植 | iOS 已验证算法正确 |
| D-9 | 单一 ISO8601Parser | 消除 7 处重复实现 |
| D-10 | MockData.load() throws | 替代 fatalError crash |

## Tasks (22)

1. iOS BLOCKER (4): URL 修复 + 3 个调用点验证
2. Android BLOCKER (3): parseDateTime, Trip 类型, Card import
3. Android CRITICAL 安全 (6): Room, HTTP日志, AppSettings, Theme, Onboarding, baseUrl
4. iOS CRITICAL (5): MockData, ISO8601, 7处替换, UpdatesView, Settings反馈
5. 跨平台 BLOCKER (5): AMap 地图, GCJ02, MonthDetailScreen, DayDetailScreen, NavGraph+数据加载
6. 文档修正 (1): P1实施计划
