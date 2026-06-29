# Comet Design Handoff

- Change: glm-p1-notify
- Phase: design
- Mode: compact
- Context hash: 5c8bf3eb5176692e3c39dc481e87e9572b8afe0e6cbccd288cb2a2faa2073d14

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p1-notify/proposal.md

- Source: openspec/changes/glm-p1-notify/proposal.md
- Lines: 1-56
- SHA256: 8922e137298e8939189c9dd4d18b94f79e89f5ee1fd75ca90d42e3ade5af665c

```md
## Why

`glm-p1-china` 完成归档后，P1 剩余两个方向：通知推送 (F-103/F-104) 和打磨 (F-105/F-106/F-108)。通知推送是独立功能，不依赖打磨，可并行推进。Android 已有完整推送基础设施（ChargingNotificationManager、SentryNotificationManager、BootReceiver、ChargingMonitorService），iOS 完全没有通知代码。Widget 也是 Android 功能远强于 iOS。

## What Changes

### F-103: iOS Widget 增强（当前太简陋）
- **T-101**: Widget 添加 car image 背景（缓存 + 绘制）
- **T-102**: Widget 添加 lock/sentry/plug/temp status icons（匹配 Android 布局）
- **T-103**: Widget 添加充电详情（V/A/phases/progress bar）
- **T-104**: Widget lock screen variant + 位置显示

### F-104: 推送通知基础设施
- **iOS (T-201~T-207)**:
  - T-201: UNUserNotificationCenter setup + notification categories
  - T-202: BackgroundTasks 框架实现后台状态轮询（每隔 N 分钟）
  - T-203: Sentry 通知（sentry_armed → 事件触发）
  - T-204: 充电完成通知（charging → complete 状态变化）
  - T-205: TPMS 胎压低告警
  - T-206: 车辆软件更新通知
  - T-207: 里程成就/电池健康/CarState 通知

- **Android (T-301~T-303)**:
  - T-301: 验证现有 ChargingNotificationManager + SentryNotificationManager 完整性和编译
  - T-302: 验证 BootReceiver + ChargingMonitorService 注册和生命周期
  - T-303: AndroidManifest 确保所有 notification receiver/service 注册

## Capabilities

### New Capabilities
- `ios-widget-enhancement`: iOS Widget 从 3 字段升级到 Android 同等富度
- `push-notifications`: 双端推送通知（APNs + FCM）

### Modified Capabilities
无

## Impact

### Files Created
- iOS: `Features/Notifications/NotificationManager.swift`, `Widget/MateLinkWidget.swift` (修改), `Widget/CarImageCache.swift`
- Android: 修改现有 notification/ 目录文件（T-301~T-303 验证修复）

### Files Modified
- iOS: `App/MateLinkApp.swift` (通知注册), `Widget/MateLinkWidget.swift` (大量重写)
- Android: `AndroidManifest.xml` (验证 receiver/service 注册), `widget/CarWidget.kt` (验证)

### Dependencies
- iOS: UserNotifications.framework, BackgroundTasks.framework
- Android: 已有 androidx.work, firebase-messaging (如需要)

## Non-Goals
- 效率评分 Golden Foot (→ glm-p1-polish)
- 访问地区统计 (→ glm-p1-polish)  
- 多语言 (→ glm-p1-polish)
- Web 推送通知
- 自定义通知音效
- 交互式通知 (action buttons)```

## openspec/changes/glm-p1-notify/design.md

- Source: openspec/changes/glm-p1-notify/design.md
- Lines: 1-58
- SHA256: 25878f6d5e694d6f1390d1bda21124e2d965f5aeac5ce4e04f54cf5517ea5c81

```md
## Context

P1 阶段剩余方向。glm-review-fix-critical 已修复 Android 安全漏洞和数据库完整性，glm-p1-china 完成高德集成和 TOU 显示。通知推送是独立的基础设施层，不依赖其他 P1 工作。

## Goals / Non-Goals

**Goals:**
1. iOS Widget 匹配 Android 功能广度（car image + status icons + charging details）
2. iOS 推送通知基础设施（7 种通知类型）
3. Android 现有通知代码验证和修复

**Non-Goals:**
- F-105~F-108 (→ glm-p1-polish)
- Web 推送
- 自定义通知音效/交互式按钮

## Decisions

### D-1: iOS Widget 不使用 WidgetKit Canvas 渲染车图
**选择：** WidgetKit 的 `Image(uiImage:)` 支持静态缓存图像。用 AppGroup UserDefaults 存储 base64 编码的车图（与现有 Dashboard → Widget 通信方式一致）。Dashboard 加载车辆图像后写入 AppGroup cache，Widget 读取展示。

### D-2: iOS 通知用 BackgroundTasks 而非远程 APNs
**选择：** BGAppRefreshTask + BGProcessingTask 定期轮询 TeslaMateApi 状态变化。不需要 Apple Push Notification Service（自托管服务器没有 APNs 注册需求）。本地通知在状态变化时触发 UNUserNotificationCenter。

**拒绝远程推送：** 需要搭建推送服务器（Vapor/Node.js），TeslaMate 自托管场景不适用。本地轮询更简单。

### D-3: Android 通知代码验证而非重写
**选择：** Android 已有完整的通知基础设施（ChargingNotificationManager、SentryNotificationManager、ChargingMonitorService、BootReceiver、TpmsStateDataStore）。本次 only 验证编译 + 修复潜在 bug，不重写。

### D-4: 7 种通知类型统一接口
iOS 侧抽象：
```swift
protocol Notifiable {
    var title: String { get }
    var body: String { get }
    var categoryIdentifier: String { get }
    func trigger()
}
```
具体实现：SentryNotification、ChargingCompleteNotification、TpmsAlertNotification、UpdateAvailableNotification、MileestoneNotification、BatteryHealthNotification、CarStateNotification

## Risks / Trade-offs

| 风险 | 严重度 | 缓解 |
|---|---|---|
| BackgroundTasks 执行间隔不可控（iOS 调度） | MEDIUM | 至少 ~15min 一次，可接受 |
| Widget 在非首页时 cache 过时 | LOW | Dashboard 写入 cache 时带上时间戳，Widget 显示 "N min ago" |
| Android notification 代码有未找到的类型 | LOW | T-303 编译验证发现即修 |

## Migration Plan

1. T-101~T-104: iOS Widget rewrite（独立可验证）
2. T-201~T-207: iOS notification infrastructure（逐步添加）
3. T-301~T-303: Android notification 验证（编译检查）

## Open Questions

1. BackgroundTasks 在 iOS 模拟器上需要手动触发 — 真机测试后才确认正常工作
2. Widget car image 缓存大小限制 — AppGroup UserDefaults 约 5MB，车图在合理范围内```

## openspec/changes/glm-p1-notify/tasks.md

- Source: openspec/changes/glm-p1-notify/tasks.md
- Lines: 1-21
- SHA256: 1605312f03cee335e475dcad55f47d379dcf3f43da9e711c9a6e16e872c0ea2d

```md
## 1. iOS Widget 增强

- [ ] T-101 添加 car image 背景缓存和绘制到 `MateLinkWidget.swift`
- [ ] T-102 Widget 添加 lock/sentry/plug/temp 状态图标行
- [ ] T-103 Widget 添加充电详情行（V/A/phases + progress bar）
- [ ] T-104 Widget lock screen variant + 位置文本显示

## 2. iOS 推送通知基础设施

- [ ] T-201 创建 `NotificationManager.swift`（UNUserNotificationCenter setup + categories + 基础框架）
- [ ] T-202 实现 BackgroundTasks 后台轮询框架（BGAppRefreshTask + 状态变化检测）
- [ ] T-203 实现 Sentry 通知（sentry_armed → alert 事件触发）
- [ ] T-204 实现充电完成通知（charging → complete 状态变化检测）
- [ ] T-205 实现 TPMS 胎压低告警通知
- [ ] T-206 实现车辆软件更新可用通知
- [ ] T-207 实现里程成就/电池健康/CarState 通知

## 3. Android 通知验证

- [ ] T-301 验证 `ChargingNotificationManager` + `SentryNotificationManager` 编译和完整
- [ ] T-302 验证 `BootReceiver` + `ChargingMonitorService` 生命周期
- [ ] T-303 验证 `AndroidManifest.xml` 通知相关 receiver/service/foreground-service type 注册```

## openspec/changes/glm-p1-notify/specs/ios-widget-enhancement/spec.md

- Source: openspec/changes/glm-p1-notify/specs/ios-widget-enhancement/spec.md
- Lines: 1-32
- SHA256: 3f56ca43ae729c95ec39b8c7a66fd4664e3870dd124151747de3a5fd273db1ba

```md
## ADDED Requirements

### Requirement: Widget 显示车辆图像
iOS Widget SHALL 在 medium size 布局中显示车辆图像背景，默认显示缓存的 car image，无图像时显示车型名占位。

#### Scenario: 有缓存车辆图像
- **WHEN** AppGroup UserDefaults 中有有效的 carImage 缓存
- **THEN** Widget 渲染 Image(uiImage:) 作为背景

#### Scenario: 无缓存车辆图像
- **WHEN** AppGroup UserDefaults 中无 carImage 缓存
- **THEN** Widget 显示 "Tesla" 文字占位，不渲染图像

### Requirement: Widget 显示状态图标行
iOS Widget SHALL 在 medium size 布局中显示锁车/sentry/插电/车内温度四个状态图标。

#### Scenario: 正常显示
- **WHEN** 有状态数据
- **THEN** Widget 显示 lock/unlock 🔒、sentry armed/off 🛡️、plug ⚡、temp 🌡️ 图标行

### Requirement: Widget 显示充电详情
iOS Widget SHALL 在 charging 状态下显示电压/电流/相位/进度条。

#### Scenario: 充电中
- **WHEN** 车辆状态 charging 且 carImage 数据包含 chargerVoltage/chargerActualCurrent/chargePhases/chargeLimitSoc
- **THEN** Widget 显示 "N V / N A / N相" + 充电进度条

### Requirement: Widget Lock Screen 变体
iOS Widget SHALL 支持 lock screen 显示电池百分比 + 续航 + 状态。

#### Scenario: Lock screen
- **WHEN** 添加到 lock screen
- **THEN** Widget 显示简洁布局：电池圆圈 + "%" + "NNN km" + 状态文字```

## openspec/changes/glm-p1-notify/specs/push-notifications/spec.md

- Source: openspec/changes/glm-p1-notify/specs/push-notifications/spec.md
- Lines: 1-28
- SHA256: dce7666e95c1a4ecce3b05135f0234ab6d56c100f80bd87dc5b3c4f3627753c1

```md
## ADDED Requirements

### Requirement: Sentry 通知
iOS SHALL 在 sentry_armed → alert 事件触发时发送本地推送通知。

#### Scenario: Sentry 告警
- **WHEN** 后台轮询检测到 sentryMode 状态从 armed 变为 alert
- **THEN** 系统发送标题为 "Sentry Alert" 的本地通知

### Requirement: 充电完成通知
iOS SHALL 在充电从 charging → complete 状态变化时发送本地通知。

#### Scenario: 充电完成
- **WHEN** 后台轮询检测到 chargingState 从 "Charging" 变为 "Complete"
- **THEN** 系统发送含充电量和费用的本地通知

### Requirement: TPMS 胎压低告警
iOS SHALL 在任何一个轮胎压力低于阈值时发送通知。

#### Scenario: 胎压低
- **WHEN** 后台轮询检测到任意 tpmsPressure 值 < 2.0 bar
- **THEN** 系统发送含具体轮胎和压力值的告警通知

### Requirement: 后台轮询
iOS SHALL 使用 BackgroundTasks 框架定期轮询 TeslaMateApi 状态变化。

#### Scenario: 后台刷新
- **WHEN** 应用在后台且 BGAppRefreshTask 被调度
- **THEN** 系统调用 notificationManager.checkForUpdates() 检测状态变化```

