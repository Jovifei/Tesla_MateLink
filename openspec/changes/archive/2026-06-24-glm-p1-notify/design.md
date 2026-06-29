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
2. Widget car image 缓存大小限制 — AppGroup UserDefaults 约 5MB，车图在合理范围内