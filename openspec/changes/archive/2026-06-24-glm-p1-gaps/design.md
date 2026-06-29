# Design: P1 Gaps

## 1. iOS TimelineView
- Data: Merge Drive[] + Charge[] sorted by start_date
- UI: LazyVStack with colored dots (blue=drive, orange=charge)
- Each row: time, type icon, description, metrics
- Reuse existing MockAPI data

## 2. iOS Widget
- WidgetKit: StaticConfiguration, TimelineProvider
- Small: battery% + ring
- Medium: battery + range + state
- Data: AppGroup shared UserDefaults from main app
- Refresh: TimelinePolicy .after(15min)

## 3. Dashboard Map
- iOS: Reuse existing AmapView.swift, embed as mini card in DashboardView
- Android: Simple MapView composable (MapKit-like, marker at car position)
- Both: show lat/lng from CarStatus, 200x150 area