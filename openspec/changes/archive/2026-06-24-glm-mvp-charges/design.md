# Design: iOS Charge Detail

## Pattern: 直抄 matedroid `ChargeDetailScreen.kt`

## Key Components
- Header: address + charge_type badge + duration
- 4 StatCards: energy/cost/efficiency/battery
- 3 Curve Picker: Power/Voltage/Temp
- Swift Charts LineMark + Brush

## Data
- GET /charges/:id → Charge with samples

## Files
- ChargeDetailView.swift (~120 lines)
