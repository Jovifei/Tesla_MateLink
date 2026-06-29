# Proposal: P1 Gaps — Cross-Platform Parity

## Summary
补 P0 交叉审核发现的 3 个缺口：iOS Timeline 屏幕、iOS Widget、双端 Dashboard 地图。

## Goals
- iOS TimelineView (借鉴 Android TimelineScreen)
- iOS Widget (借鉴 Android CarWidget，WidgetKit 实现)
- Dashboard 地图组件 (iOS: AmapView 接入，Android: MapView)

## Non-Goals
- 推送通知 → glm-p1-notify
- 多语言 → glm-p1-polish

## Dependencies
- P0 archived changes (all code base exists)
