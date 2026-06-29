---
comet_change: glm-p1-notify
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-24-glm-p1-notify
status: final
---

# glm-p1-notify — P1 通知推送 + Widget 增强

## Context / Goals

- F-103: iOS Widget 从 3 字段升级到 Android 同等富度
- F-104: 推送通知基础设施 (iOS APNs + Android FCM)
- T-301~T-303: Android 现有通知代码验证

## Key Decisions

- D-1: iOS Widget 用 AppGroup UserDefaults 缓存车图
- D-2: 本地轮询 (BGAppRefreshTask) 替代远程 APNs
- D-3: Android 验证而非重写
- D-4: 7 种通知类型统一 `Notifiable` 协议

## Tasks (14)

### iOS Widget (T-101 ~ T-104)
- T-101: car image 背景
- T-102: status icons (lock/sentry/plug/temp)
- T-103: 充电详情 (V/A/phases/progress)
- T-104: lock screen variant + 位置

### iOS Notification (T-201 ~ T-207)
- T-201: NotificationManager.swift
- T-202: BackgroundTasks 轮询
- T-203~T-207: 7 种通知实现

### Android (T-301 ~ T-303)
- 验证现有通知基础设施
