# Design: iOS Drive Detail

## Pattern: 直抄 matedroid `DriveDetailScreen.kt`

## Key Components
- Header: start_address → end_address + 时间
- SpeedCurve / PowerCurve / AltitudeCurve / TempCurve / TireCurve: 5 Picker segments
- BatteryChangeBar: start% → end% with delta
- Swift Charts LineMark + Brush for zoom

## Data
- GET /drives/:id → Drive with positions[]

## Files
- DriveDetailView.swift (~150 lines)
- RouteSimplifier.swift (~50 lines, Douglas-Peucker)
