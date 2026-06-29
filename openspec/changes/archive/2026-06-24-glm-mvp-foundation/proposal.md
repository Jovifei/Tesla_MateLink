# Proposal: MateLink MVP Foundation

## Summary

初始化 MateLink 移动 App 工程骨架（Android `app_glm/android` + iOS `app_glm/ios`），搭建数据层基础设施（API Client、Mock Mode、离线缓存、认证），实现首次配置（Onboarding）、设置页、主题切换、多车选择。作为后续 5 个 MVP change 的公共依赖。

## Why

- 没有工程骨架，后续模块（Dashboard/Drives/Charges）无处落地
- 没有 API Client 统一封装，各个模块各自写网络代码，难以维护
- 没有 Mock 模式，开发期依赖真实 TeslaMate 实例，效率低
- 没有离线缓存，弱网场景体验差

## Goals

- [x] 创建 Android (Kotlin + Jetpack Compose) + iOS (Swift + SwiftUI) 双端工程
- [x] 实现 API Client 层：封装 TeslaMateApi 所有 16 个端点，含 Bearer Token 认证
- [x] 实现 Mock Mode：内置虚拟数据，切换不重启
- [x] 实现离线缓存：MMKV（Android）/ Core Data（iOS）存储最近 30 天数据
- [x] 实现 F-001 首次配置：三步连通性检测（ping → readyz → cars）
- [x] 实现 F-013 基础设置：服务器 URL/Token/单位/时区
- [x] 实现 F-012 浅色/深色主题：跟随系统 + 手动切换 + 基于车色的强调色
- [x] 实现 F-002 车辆选择：多车账号下切换
- [x] 搭建 CI/CD 骨架：GitHub Actions + Detekt/SwiftLint

## Non-Goals

- Dashboard 实时状态（F-003 ~ F-005）→ `glm-mvp-dashboard`
- 行程列表/详情（F-006 ~ F-007）→ `glm-mvp-drives`
- 充电列表/详情（F-008 ~ F-009）→ `glm-mvp-charges`
- 电池健康 + 更新（F-010 ~ F-011）→ `glm-mvp-battery-updates`
- 上架准备 → `glm-mvp-release`
- 中国本地化（v1.1）、3D 车辆（v1.2）、Apple Watch（v1.2）、车端命令（v2.0）

## Scope

| 平台 | 目录 | 语言 |
|---|---|---|
| Android | `app_glm/android/` | Kotlin + Jetpack Compose |
| iOS | `app_glm/ios/` | Swift + SwiftUI |
| 根 | `app_glm/` | Gradle Kotlin DSL + Xcode Workspace |

## Key Decisions (confirmed by Jovi)

| 决策 | 结果 |
|---|---|
| 品牌名 | **MateLink** |
| 技术方案 | **原生双端** (Kotlin + SwiftUI) |
| 商业模式 | 前期免费，后期可选付费功能 |
| 视觉风格 | Apple-Like + 基于车色的强调色 |
| 差异化优先级 | 中国本地化 > 3D 车辆 > 跨平台开源 > AI 查询 |

## Reference Repos (heavily borrow)

| 维度 | Android | iOS |
|---|---|---|
| 架构 | matedroid (Clean Architecture) | t-buddy + hedgiemate 功能清单 |
| UI | matedroid (Material 3 + Compose) | Tesla_Clone_Swiftui (SwiftUI 组件) |
| API 参考 | teslamateapi Go 源码 | 同左 |
| 数据设计 | teslamate Ecto schema | 同左 |

## Acceptance Criteria

- [ ] `./gradlew assembleDebug` 构建成功（Android）
- [ ] Xcode Build 通过（iOS，需要 Mac）
- [ ] Onboarding 三步检测全通过（Mock 模式下模拟）
- [ ] 主题切换：浅色 ↔ 深色 ↔ 跟随系统
- [ ] 车辆切换：2 辆以上 Mock 车可切换
- [ ] Mock 模式开关不重启生效
- [ ] 离线模式：清网络 → Dashboard 显示缓存数据 + "Offline" banner
- [ ] CI 流水线：PR 触发 Lint + 构建检查
