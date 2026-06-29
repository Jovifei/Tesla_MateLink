# glm_05 · 数据模型与 API

## 1. 数据来源

App 的数据**全部**来自用户自托管的 [TeslaMateApi](https://github.com/tobiasehlert/teslamateapi)，不经我们服务器。

```
App  ──HTTPS──►  TeslaMateApi  ──►  PostgreSQL + MQTT  ──►  TeslaMate  ──►  Tesla Fleet API
```

## 2. API 端点清单

完整路由前缀：`/api/v1`（业务）+ `/api`（运维健康检查）

### 2.1 业务端点

| 方法 | 路径 | 用途 | P0 | P1 |
|---|---|---|---|---|
| GET | `/cars` | 车辆列表 | ✅ | |
| GET | `/cars/:carId` | 单车基本信息 | ✅ | |
| GET | `/cars/:carId/status` | ★ 实时状态（MQTT 缓存） | ✅ | |
| GET | `/cars/:carId/drives` | 行程列表（分页） | ✅ | |
| GET | `/cars/:carId/drives/:driveId` | 行程详情（含轨迹） | ✅ | |
| GET | `/cars/:carId/charges` | 充电会话列表 | ✅ | |
| GET | `/cars/:carId/charges/current` | 当前充电 | ✅ | |
| GET | `/cars/:carId/charges/:chargeId` | 充电详情（含曲线） | ✅ | |
| GET | `/cars/:carId/battery-health` | 电池健康 | ✅ | |
| GET | `/cars/:carId/updates` | 软件更新历史 | ✅ | |
| GET | `/cars/:carId/logging` | 日志开关状态 | | ✅ |
| PUT | `/cars/:carId/logging/:command` | 切换日志（resume/suspend） | | ✅ |
| GET | `/globalsettings` | 全局设置 | | ✅ |
| POST | `/cars/:carId/command/:command` | 车端命令（v2.0） | | v2.0 |
| POST | `/cars/:carId/wake_up` | 唤醒（v2.0） | | v2.0 |

### 2.2 运维端点（吸收 mimo 的发现）

| 方法 | 路径 | 用途 | App 端使用场景 |
|---|---|---|---|
| GET | `/api/ping` | 连通性测试（最轻量） | ✅ **F-001 Test Connection 首选**（替代原 `/cars`） |
| GET | `/api/healthz` | 健康检查 | 后台周期性探测 |
| GET | `/api/readyz` | 就绪检查（DB + MQTT 都就绪） | Onboarding 的"完整就绪"判断 |

**F-001 Test Connection 流程优化**：
1. 先调 `/api/ping`（最轻量，验证 URL 正确性）
2. 若成功，再调 `/api/readyz`（验证 DB + MQTT 就绪）
3. 若成功，再调 `/cars`（验证 Token 权限 + 拿到车辆数）
4. 三步全过才算"完全就绪"

## 3. 数据模型（TypeScript 类型）

### 3.1 Car（车辆基本信息）

**真实 API 响应结构**（基于 `teslamateapi v1.21.x` 源码，吸收 mimo 的修正）：

```json
{
  "data": {
    "cars": [
      {
        "car_id": 1,
        "name": "我的特斯拉",
        "car_details": {
          "eid": 123456789,
          "vid": 987654321,
          "vin": "5YJ3E1EA1PF000001",
          "model": "Model 3",
          "trim_badging": "LR",
          "efficiency": 142.5
        },
        "car_exterior": {
          "exterior_color": "SolidBlack",
          "spoiler_type": "None",
          "wheel_type": "AeroTurbine19"
        },
        "car_settings": {
          "suspend_min": 15,
          "suspend_after_idle_min": 15,
          "req_not_unlocked": false,
          "free_supercharging": false,
          "use_streaming_api": false
        },
        "teslamate_details": {
          "inserted_at": "2024-01-01T00:00:00Z",
          "updated_at": "2026-06-22T00:00:00Z"
        },
        "teslamate_stats": {
          "total_charges": 234,
          "total_drives": 567,
          "total_updates": 12
        }
      }
    ]
  }
}
```

**App 端 TypeScript 映射**（应用层扁平化，便于使用）：

```typescript
// src/api/types.ts

// 底层：真实 API 响应结构（用于 fetch 反序列化）
export interface CarApiResponse {
  data: { cars: CarRaw[] };
}

export interface CarRaw {
  car_id: number;
  name: string;
  car_details: {
    eid: number;
    vid: number;
    vin: string;
    model: string;
    trim_badging: string;
    efficiency: number;
  };
  car_exterior: {
    exterior_color: string;
    spoiler_type: string;
    wheel_type: string;
  };
  car_settings: {
    suspend_min: number;
    suspend_after_idle_min: number;
    req_not_unlocked: boolean;
    free_supercharging: boolean;
    use_streaming_api: boolean;
  };
  teslamate_details: {
    inserted_at: string;
    updated_at: string;
  };
  teslamate_stats: {
    total_charges: number;
    total_drives: number;
    total_updates: number;
  };
}

// 应用层：扁平化模型（便于 UI 直接使用）
export interface Car {
  id: number;
  name: string;
  vin: string;
  model: string;
  trim_badging: string;
  exterior_color: string;
  spoiler_type: string;
  wheel_type: string;
  efficiency: number;
  total_charges: number;
  total_drives: number;
  total_updates: number;
}

// 映射函数
export function flattenCar(raw: CarRaw): Car {
  return {
    id: raw.car_id,
    name: raw.name,
    vin: raw.car_details.vin,
    model: raw.car_details.model,
    trim_badging: raw.car_details.trim_badging,
    exterior_color: raw.car_exterior.exterior_color,
    spoiler_type: raw.car_exterior.spoiler_type,
    wheel_type: raw.car_exterior.wheel_type,
    efficiency: raw.car_details.efficiency,
    total_charges: raw.teslamate_stats.total_charges,
    total_drives: raw.teslamate_stats.total_drives,
    total_updates: raw.teslamate_stats.total_updates,
  };
}
```

**注意**：
- `eid`/`vid` 是 Tesla 内部 ID，通过 API 暴露但 App 端无需使用（应用层模型忽略）
- `efficiency` 字段是 TeslaMate 计算的车辆出厂效率（Wh/km），App 用于衰减计算

来源：`teslamate/lib/teslamate/log/car.ex` + `teslamateapi/src/v1_TeslaMateAPICars.go` + mimo PRD §附录 B

### 3.2 CarStatus（实时状态）

```typescript
export type CarState = 'online' | 'offline' | 'asleep' | 'charging' | 'driving';

export interface CarStatus {
  car_id: number;
  state: CarState;
  since: string;              // ISO timestamp
  healthy: boolean;

  // 里程
  odometer: number;           // km

  // 电池
  battery_level: number;      // 0-100
  usable_battery_level: number;
  charge_energy_added: number; // kWh
  charge_limit_soc: number;   // 50-100
  charge_limit_soc_max?: number;
  charge_limit_soc_min?: number;
  ideal_battery_range_km: number;
  est_battery_range_km: number;
  rated_battery_range_km?: number;

  // 充电中
  charger_power: number;      // kW
  charger_actual_current: number;
  charger_voltage: number;
  charger_phases?: number;
  charge_port_door_open: boolean;
  time_to_full_charge: number; // hours
  charge_current_request?: number;
  charge_current_request_max?: number;

  // 气候
  inside_temp: number;        // °C
  outside_temp: number;
  is_climate_on: boolean;
  is_front_defroster_on?: boolean;
  is_rear_defroster_on?: boolean;
  fan_status?: number;
  driver_temp_setting?: number;
  passenger_temp_setting?: number;

  // 位置
  latitude: number;
  longitude: number;
  heading?: number;
  speed?: number;             // km/h（driving 时）
  shift_state?: 'P' | 'R' | 'N' | 'D';

  // 车门/车窗
  doors?: {
    driver_front: boolean;     // true = open
    driver_rear: boolean;
    passenger_front: boolean;
    passenger_rear: boolean;
  };
  windows?: {
    driver_front: 'open' | 'closed' | 'vent';
    driver_rear: 'open' | 'closed' | 'vent';
    passenger_front: 'open' | 'closed' | 'vent';
    passenger_rear: 'open' | 'closed' | 'vent';
  };
  locked?: boolean;
  trunk_open?: boolean;
  frunk_open?: boolean;

  // 胎压
  tire_pressure?: {
    front_left: number;       // bar
    front_right: number;
    rear_left: number;
    rear_right: number;
  };

  // Sentry
  sentry_mode?: boolean;

  // 软件
  car_version?: string;

  // 时间戳
  read_at?: string;
}
```

来源：`teslamateapi/src/v1_TeslaMateAPICarsStatus.go` + TeslaMate MQTT topic `teslamate/cars/:id/#`

### 3.3 Drive（行程）

```typescript
export interface Drive {
  id: number;
  car_id: number;
  start_date: string;         // ISO
  end_date: string;
  distance_km: number;
  duration_min: number;
  efficiency: number;         // Wh/km
  consumption_kWh: number;
  start_ideal_range_km: number;
  end_ideal_range_km: number;
  start_position: Position;
  end_position: Position;
  outside_temp_avg?: number;
  inside_temp_avg?: number;
}

export interface DriveDetail extends Drive {
  positions: Position[];       // 完整轨迹
  start_geofence?: Geofence;
  end_geofence?: Geofence;
}

export interface Position {
  date: string;
  latitude: number;
  longitude: number;
  elevation?: number;        // m
  speed?: number;            // km/h
  power?: number;            // kW（负值=动能回收）
  shift_state?: string;
  odometer?: number;
  outside_temp?: number;
  inside_temp?: number;
  tire_pressure?: {
    front_left: number;
    front_right: number;
    rear_left: number;
    rear_right: number;
  };
}
```

来源：`teslamate/lib/teslamate/log/drive.ex` + `position.ex`

### 3.4 Charge & ChargingProcess

TeslaMate 区分：
- `charging_processes`：一次完整充电会话
- `charges`：充电过程中的瞬时采样

```typescript
// 一次充电会话（列表项）
export interface ChargingProcess {
  id: number;
  car_id: number;
  start_date: string;
  end_date: string | null;    // null = 进行中
  charge_energy_added: number; // kWh
  charge_energy_used: number;
  start_battery_level: number;
  end_battery_level: number | null;
  start_ideal_range_km: number;
  end_ideal_range_km: number | null;
  start_position: Position;
  end_position?: Position;
  cost?: number;
  cost_currency?: string;
  charge_type?: 'AC' | 'DC';
  address?: string;
}

// 充电详情（含瞬时采样）
export interface ChargeDetail extends ChargingProcess {
  charges: ChargeSample[];     // 瞬时采样序列
}

export interface ChargeSample {
  date: string;
  battery_level: number;
  battery_heater?: boolean;
  charge_energy_added: number;
  charger_actual_current: number;
  charger_power: number;
  charger_voltage: number;
  conn_charge_cable?: string;
  fast_charger_present?: boolean;
  fast_charger_brand?: string;
  fast_charger_type?: string;
  ideal_battery_range_km: number;
  outside_temp?: number;
}
```

来源：`teslamate/lib/teslamate/log/charge.ex` + `charging_process.ex`

### 3.5 BatteryHealth（电池健康）

```typescript
export interface BatteryHealth {
  car_id: number;
  original_capacity_kwh: number;
  current_capacity_kwh: number;
  capacity_degradation_percent: number;     // 例如 5.2
  original_range_km: number;
  current_range_km: number;
  range_loss_percent: number;
  mileage_km: number;
  history: BatteryHealthPoint[];            // 历史曲线
}

export interface BatteryHealthPoint {
  date: string;
  capacity_kwh: number;
  mileage_km: number;
}
```

计算逻辑：参考 `teslamate/grafana/dashboards/battery-health.json` 的 SQL，由 TeslaMateApi 端完成。

### 3.6 Update（软件更新）

```typescript
export interface Update {
  id: number;
  car_id: number;
  start_date: string;
  end_date: string;
  version: string;
}
```

### 3.7 GlobalSettings（全局设置）

```typescript
export interface GlobalSettings {
  car_id?: number;
  display_name?: string;
  unit_of_length: 'km' | 'mi';
  unit_of_temperature: 'C' | 'F';
  unit_of_pressure: 'bar' | 'psi';
  unit_of_energy: 'kWh' | 'Wh';
  preferred_language?: string;
  // ...
}
```

## 4. API 调用模式

### 4.1 轮询 vs 长连接

| 端点 | 模式 | 间隔 |
|---|---|---|
| `/status` | 轮询 | 5s（前台）/ 5min（后台，受系统限制） |
| `/drives` `/charges` | 按需 | 用户打开页面时 |
| `/drives/:id` `/charges/:id` | 按需 | 用户点详情时 |
| `/battery-health` | 按需 + 缓存 | 5min staleTime |

**未来可选**：MQTT 直连订阅 `teslamate/cars/:id/#`，实时性更好但需处理连接保活/认证。

### 4.2 分页

TeslaMateApi 支持分页（通过 `?page=N&limit=M`），App 用 `useInfiniteQuery` 上拉加载。

```typescript
// 分页响应
export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  limit: number;
  total?: number;
}
```

注：TeslaMateApi 当前可能不返回 `total`，App 用"返回数 < limit 判定无更多"。

### 4.3 错误处理

| HTTP 状态 | 含义 | App 行为 |
|---|---|---|
| 200 | 成功 | 返回数据 |
| 401 | Token 错误 | 清除本地配置，跳转 Onboarding |
| 403 | 命令未启用 | 提示用户检查 TeslaMateApi 配置 |
| 404 | 车辆/资源不存在 | Toast 提示 + 返回上一页 |
| 500 | 服务器错误 | Toast + "Retry" 按钮 |
| 超时 | 10s | Toast "Connection timeout" |
| 网络不可达 | — | 离线模式 |

### 4.4 错误响应体

```typescript
export interface ApiErrorResponse {
  error: string;
  detail?: string;
  status?: number;
}
```

## 5. 数据缓存策略

### 5.1 React Query 缓存

| Query Key | staleTime | gcTime | refetchInterval |
|---|---|---|---|
| `['cars']` | 5min | 30min | — |
| `['carStatus', carId]` | 3s | 5min | 5s（前台） |
| `['drives', carId]` | 1min | 30min | — |
| `['driveDetail', carId, driveId]` | 5min | 24h | — |
| `['charges', carId]` | 1min | 30min | — |
| `['chargeDetail', carId, chargeId]` | 5min | 24h | — |
| `['batteryHealth', carId]` | 1h | 24h | — |
| `['updates', carId]` | 1h | 24h | — |

### 5.2 离线缓存（MMKV）

| Key | TTL | 用途 |
|---|---|---|
| `cache_carStatus_{carId}` | 5min | 离线时显示最后状态 |
| `cache_drives_{carId}_page_N` | 24h | 离线时查看列表 |
| `cache_charges_{carId}_page_N` | 24h | 离线时查看列表 |
| `cache_driveDetail_{carId}_{driveId}` | 7d | 离线查看详情 |
| `cache_chargeDetail_{carId}_{chargeId}` | 7d | 离线查看详情 |

## 6. Mock 数据规范

### 6.1 Mock 数据文件

```
src/mock/
├── data.json              # 主数据
├── cars.json              # 2 辆虚拟车
├── drives.json            # 30 天行程
├── charges.json           # 30 天充电
├── battery-health.json
├── updates.json
└── status.json            # 实时状态（Mock 模式下静态）
```

### 6.2 Mock 数据特征

| 字段 | 值 |
|---|---|
| 车辆 1 | Model 3 Long Range, Deep Blue, 19" Sport |
| 车辆 2 | Model Y Performance, Red Multi-Coat, 21" Überturbine |
| 行程数 | 60 条（30 天） |
| 充电数 | 30 条（含 5 条 DC 快充） |
| 电池健康 | 95.3%（衰减 4.7%） |
| 软件 | 2024.38.8 |

### 6.3 Mock Client

```typescript
// src/api/mockClient.ts
export class MockTeslaMateClient implements TeslaMateClientInterface {
  async getCars(): Promise<Car[]> {
    return mockData.cars;
  }
  async getCarStatus(carId: number): Promise<CarStatus> {
    return mockData.status[carId];
  }
  // ... 其他方法
}
```

### 6.4 切换

```typescript
// src/api/index.ts
export const apiClient: TeslaMateClientInterface =
  useSettings.getState().mockMode
    ? new MockTeslaMateClient()
    : new TeslaMateClient(
        useSettings.getState().serverUrl!,
        useSettings.getState().apiToken ?? undefined
      );
```

## 7. 数据安全

### 7.1 敏感数据存储

| 数据 | 存储 | 加密 |
|---|---|---|
| Server URL | MMKV | 否（非敏感） |
| API Token | expo-secure-store | ✅ Keychain/Keystore |
| 车辆数据 | MMKV | 否（非敏感） |
| 缓存 | MMKV | 否 |

### 7.2 传输安全

- **强制 HTTPS**：iOS 默认 ATS、Android `usesCleartextTraffic=false`
- **例外**：允许用户连接 `http://10.x.x.x` / `http://192.168.x.x`（局域网自托管）
- **证书验证**：不允许自签名证书（用户应自行配 Traefik + Let's Encrypt）

### 7.3 隐私原则

| 原则 | 实现 |
|---|---|
| 不收集车辆数据 | App 仅在用户设备上处理数据 |
| 不上传用户数据 | 不调用任何第三方分析 API 上传车辆数据 |
| 不存储 Tesla 账号 | App 只持有 TeslaMateApi Token，不触碰 Tesla 账号 |
| 崩溃报告可选 | 用户可在 Settings 关闭 Sentry 上报 |

## 8. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_06_非功能需求.md`
