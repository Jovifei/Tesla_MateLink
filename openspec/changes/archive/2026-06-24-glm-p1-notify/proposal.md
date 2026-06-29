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
- 交互式通知 (action buttons)