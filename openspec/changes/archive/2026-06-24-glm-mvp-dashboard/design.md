# Design: iOS Dashboard

## Reference: 直抄 matedroid `DashboardScreen.kt` 架构

## Pattern
MVVM: DashboardViewModel (StateFlow) → DashboardScreen (Compose → SwiftUI @StateObject)

## Key Components
- BatteryCard: 电量% + 进度条 + 续航
- StatusBadge: CarState → Color + Label
- CarImage: 2D 车辆图, Coil → AsyncImage, tintColor 着色
- ChargingCard: 充电中额外卡片 (power/added/remaining)
- InfoGrid: 4 格 (位置/里程/温度/胎压)
- 5s Timer: Timer.publish 每 5s 触发 refresh()

## Data
- GET /cars (Car list)
- GET /cars/:id/status (polling 5s)
- GET /charges/current (充电中)

## Files
- DashboardView.swift (enhance existing ~130 lines → ~200 lines)
- Components/BatteryCard.swift, StatusBadge.swift, CarImage.swift, ChargingCard.swift, InfoGrid.swift
