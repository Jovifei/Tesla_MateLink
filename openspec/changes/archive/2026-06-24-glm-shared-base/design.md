# Design: MateLink Shared Base

## Scope

极简——只为 Web 开发提供 Mock 数据 + TS 类型。不含任何运行时代码。

## Files

```
app_glm/shared/
├── README.md              # 使用说明
├── mock_data.json         # 2 cars + 30d drives + 30d charges + battery + updates
└── api-types.ts           # TypeScript types (all 16 endpoint response shapes)
```

## mock_data.json Schema

```jsonc
{
  "cars": [ Car, Car ],
  "status": { "1": CarStatus, "2": CarStatus },
  "drives": [ Drive × 60 ],
  "charges": [ ChargingProcess × 30 ],
  "chargeDetails": { "1": ChargeDetail, "2": ChargeDetail, ... },
  "driveDetails": { "1": DriveDetail, "2": DriveDetail, ... },
  "batteryHealth": { "1": BatteryHealth, "2": BatteryHealth },
  "updates": { "1": [Update × 3], "2": [Update × 2] },
  "globalSettings": GlobalSettings
}
```

## api-types.ts

```typescript
// All types match teslamateapi v1.21.x response shapes
export interface Car { car_id, name, car_details, car_exterior, ... }
export type CarState = 'online' | 'offline' | 'asleep' | 'charging' | 'driving';
export interface CarStatus { state, battery_level, ... }
export interface Drive { id, distance_km, duration_min, efficiency, ... }
export interface DriveDetail extends Drive { positions: Position[] }
export interface ChargingProcess { id, charge_energy_added, cost, ... }
export interface ChargeDetail extends ChargingProcess { charges: ChargeSample[] }
export interface BatteryHealth { original_capacity_kwh, current_capacity_kwh, ... }
export interface Update { version, start_date, end_date }
export interface GlobalSettings { unit_of_length, ... }
// ... total ~30 interfaces
```
