## 1. iOS Widget 增强

- [x] T-101 添加 car image 背景缓存和绘制到 `MateLinkWidget.swift`
- [x] T-102 Widget 添加 lock/sentry/plug/temp 状态图标行
- [x] T-103 Widget 添加充电详情行（V/A/phases + progress bar）
- [x] T-104 Widget lock screen variant + 位置文本显示

## 2. iOS 推送通知基础设施

- [x] T-201 创建 `NotificationManager.swift`（UNUserNotificationCenter setup + categories + 基础框架）
- [x] T-202 实现 BackgroundTasks 后台轮询框架（BGAppRefreshTask + 状态变化检测）
- [x] T-203 实现 Sentry 通知（sentry_armed → alert 事件触发）
- [x] T-204 实现充电完成通知（charging → complete 状态变化检测）
- [x] T-205 实现 TPMS 胎压低告警通知
- [x] T-206 实现车辆软件更新可用通知
- [x] T-207 实现里程成就/电池健康/CarState 通知

## 3. Android 通知验证

- [x] T-301 验证 `ChargingNotificationManager` + `SentryNotificationManager` 编译和完整
- [x] T-302 验证 `BootReceiver` + `ChargingMonitorService` 生命周期
- [x] T-303 验证 `AndroidManifest.xml` 通知相关 receiver/service/foreground-service type 注册