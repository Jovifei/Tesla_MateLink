# Proposal: MateLink iOS Dashboard

## Summary
iOS Dashboard 实时状态页 + 2D 车辆图 + 位置地图 (F-003/004/005)

## Goals
- DashboardScreen: 电量+续航+状态徽章+5s 轮询+下拉刷新
- 2D 车辆图 (CarImage): 按车色+轮毂着色
- 位置地图缩略图
- 充电进度卡片 (充电中显示)

## Non-Goals
- 3D 车辆 → v1.2
- Widget → v1.1

## Scope
`Features/Dashboard/` — DashboardView.swift 增强
`Features/Dashboard/Components/` — BatteryCard/StatusBadge/CarImage

## Dependencies
- glm-mvp-foundation (AppState + API Client + Theme)

## Reference
- 直抄 matedroid `DashboardScreen.kt`
- 参考 Web `Dashboard.tsx`
