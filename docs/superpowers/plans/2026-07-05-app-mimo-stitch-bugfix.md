# app_mimo Stitch "简约白" Bug 修复实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复审查报告中的 P0+P1 级问题，使 app_mimo 达到 Stitch "简约白" 功能完整性（27 项修复）。

**Architecture:** 三阶段串行（数据层 → 页面补全 → Bug 收尾），平台内 Android/iOS 可并行。参考 `docs/git_ref/mimo/` Android 嵌套 API 模型。

**Tech Stack:** Kotlin/Jetpack Compose (Android), Swift/SwiftUI (iOS), TeslaMate REST API v1.24+

**基准分支:** `codex/app-mimo-stitch-1to1`

**Spec:** `docs/superpowers/specs/2026-07-05-app-mimo-stitch-bugfix-design.md`

---

## 文件结构

### Phase 1 — 修改文件

| 文件 | 操作 | 责任 |
|------|------|------|
| `app_mimo/ios/MateLink/Core/Models/CarStatus.swift` | **重写** | iOS 4 模型嵌套化 |
| `app_mimo/ios/MateLink/Core/API/ApiClient.swift` | 修改 | 补齐 7 端点 + URL 安全 |
| `app_mimo/ios/MateLink/Core/API/UrlSecurity.swift` | **新建** | URL 安全校验 |
| `app_mimo/android/.../dashboard/DashboardViewModel.kt` | 修改 | 空安全修复 |
| `app_mimo/android/.../di/NetworkModule.kt` | 修改 | Auth 冲突修复 |

### Phase 2 — 创建/修改文件

| 文件 | 操作 | 责任 |
|------|------|------|
| `app_mimo/android/.../screens/efficiency/EfficiencyScreen.kt` | **新建** | 效率分析页 |
| `app_mimo/android/.../screens/efficiency/EfficiencyViewModel.kt` | **新建** | 效率分析 VM |
| `app_mimo/android/.../screens/cost/CostScreen.kt` | **新建** | 成本分析页 |
| `app_mimo/android/.../screens/cost/CostViewModel.kt` | **新建** | 成本分析 VM |
| `app_mimo/android/.../screens/range/RangeScreen.kt` | **新建** | 续航分析页 |
| `app_mimo/android/.../screens/range/RangeViewModel.kt` | **新建** | 续航分析 VM |
| `app_mimo/android/.../screens/vampire/VampireScreen.kt` | **新建** | 待机耗电页 |
| `app_mimo/android/.../screens/vampire/VampireViewModel.kt` | **新建** | 待机耗电 VM |
| `app_mimo/android/.../screens/timeline/TimelineScreen.kt` | **新建** | 时间线页 |
| `app_mimo/android/.../screens/timeline/TimelineViewModel.kt` | **新建** | 时间线 VM |
| `app_mimo/android/.../navigation/NavGraph.kt` | 修改 | 注册 5 新路由 |
| `app_mimo/android/.../res/values-zh/strings.xml` | 修改 | 中文字符串 |
| `app_mimo/ios/MateLink/Resources/zh-Hans.lproj/Localizable.strings` | **新建** | iOS 中文 |
| `app_mimo/ios/MateLink/Core/Utils/Localization.swift` | 修改 | 加载 zh-Hans |

### Phase 3 — 修改文件

| 文件 | 操作 | 责任 |
|------|------|------|
| `app_mimo/ios/.../Drives/DriveDetailView.swift` | 修改 | maxSpeed 修复 |
| `app_mimo/ios/.../Range/RangeView.swift` | 修改 | accuracy 重写 |
| `app_mimo/ios/.../Dashboard/DashboardView.swift` | 修改 | Timer/BatteryTrend/widget/状态色 |
| `app_mimo/android/.../api/ApiClient.kt` | 修改 | 线程安全 |
| `app_mimo/android/.../screens/dashboard/DashboardScreen.kt` | 修改 | 可空字段 |
| `app_mimo/android/.../repository/SettingsRepository.kt` | 修改 | MockMode 默认 |
| `app_mimo/android/.../repository/StatsRepository.kt` | 修改 | 日期解析 |
| `app_mimo/android/.../screens/battery/BatteryViewModel.kt` | 修改 | 外推阈值 |
| `app_mimo/android/.../service/ChargingMonitorService.kt` | 修改 | 线程安全 |
| `app_mimo/android/.../screens/dashboard/DashboardViewModel.kt` | 修改 | 异常处理 |

---

## Phase 1: 数据层对齐

### Task 1: iOS 模型嵌套化 — CarStatus

**Files:**
- Modify: `app_mimo/ios/MateLink/Core/Models/CarStatus.swift:1-35`

- [ ] **Step 1: 创建嵌套解码辅助结构体**

在 `CarStatus.swift` 顶部（`CarState` 之后）添加嵌套容器类型：

```swift
// MARK: - Nested API response containers (TeslaMate API v1.24+)

private struct BatteryDetails: Codable {
    let batteryLevel: Int?
    let usableBatteryLevel: Int?
    let estBatteryRange: Double?
    let ratedBatteryRange: Double?
    let idealBatteryRange: Double?

    enum CodingKeys: String, CodingKey {
        case batteryLevel = "battery_level"
        case usableBatteryLevel = "usable_battery_level"
        case estBatteryRange = "est_battery_range"
        case ratedBatteryRange = "rated_battery_range"
        case idealBatteryRange = "ideal_battery_range"
    }
}

private struct ChargingDetails: Codable {
    let pluggedIn: Bool?
    let chargingState: String?
    let chargeEnergyAdded: Double?
    let chargeLimitSoc: Int?
    let chargePortDoorOpen: Bool?
    let chargerActualCurrent: Int?
    let chargerPhases: Int?
    let chargerPower: Int?
    let chargerVoltage: Int?
    let chargeCurrentRequest: Int?
    let chargeCurrentRequestMax: Int?
    let timeToFullCharge: Double?

    var isDcCharging: Bool { chargerPhases == 0 }

    enum CodingKeys: String, CodingKey {
        case pluggedIn = "plugged_in"
        case chargingState = "charging_state"
        case chargeEnergyAdded = "charge_energy_added"
        case chargeLimitSoc = "charge_limit_soc"
        case chargePortDoorOpen = "charge_port_door_open"
        case chargerActualCurrent = "charger_actual_current"
        case chargerPhases = "charger_phases"
        case chargerPower = "charger_power"
        case chargerVoltage = "charger_voltage"
        case chargeCurrentRequest = "charge_current_request"
        case chargeCurrentRequestMax = "charge_current_request_max"
        case timeToFullCharge = "time_to_full_charge"
    }
}

private struct ClimateDetails: Codable {
    let isClimateOn: Bool?
    let insideTemp: Double?
    let outsideTemp: Double?

    enum CodingKeys: String, CodingKey {
        case isClimateOn = "is_climate_on"
        case insideTemp = "inside_temp"
        case outsideTemp = "outside_temp"
    }
}

private struct TpmsDetails: Codable {
    let pressureFl: Double?
    let pressureFr: Double?
    let pressureRl: Double?
    let pressureRr: Double?

    enum CodingKeys: String, CodingKey {
        case pressureFl = "tpms_pressure_fl"
        case pressureFr = "tpms_pressure_fr"
        case pressureRl = "tpms_pressure_rl"
        case pressureRr = "tpms_pressure_rr"
    }
}

private struct CarStatusContainer: Codable {
    let healthy: Bool?
    let locked: Bool?
    let sentryMode: Bool?
    let windowsOpen: Bool?
    let doorsOpen: Bool?
    let centerDisplayState: String?

    enum CodingKeys: String, CodingKey {
        case healthy, locked
        case sentryMode = "sentry_mode"
        case windowsOpen = "windows_open"
        case doorsOpen = "doors_open"
        case centerDisplayState = "center_display_state"
    }
}

private struct CarGeodata: Codable {
    let geofence: String?
    let latitude: Double?
    let longitude: Double?
}

private struct DrivingDetails: Codable {
    let shiftState: String?
    let power: Int?
    let speed: Int?
    let heading: Int?
    let elevation: Int?

    enum CodingKeys: String, CodingKey {
        case shiftState = "shift_state"
        case power, speed, heading, elevation
    }
}

private struct OdometerDetails: Codable {
    let odometer: Double?
}

private struct CarVersions: Codable {
    let version: String?
    let updateAvailable: Bool?
    let updateVersion: String?

    enum CodingKeys: String, CodingKey {
        case version
        case updateAvailable = "update_available"
        case updateVersion = "update_version"
    }
}
```

- [ ] **Step 2: 重写 CarStatus 为嵌套解码**

替换现有 `CarStatus` struct（保留 `CarState` enum）：

```swift
struct CarStatus: Codable {
    let carId: Int
    let state: CarState
    let since: String?

    // Decoded from nested containers
    let healthy: Bool
    let odometer: Double
    let batteryLevel: Int
    let usableBatteryLevel: Int
    let usableBatteryRangeKm: Double
    let idealBatteryRangeKm: Double
    let chargeEnergyAdded: Double
    let chargeLimitSoc: Int
    let chargerPower: Double
    let chargerActualCurrent: Int
    let chargerVoltage: Int
    let chargePortDoorOpen: Bool
    let timeToFullCharge: Double
    let insideTemp: Double
    let outsideTemp: Double
    let isClimateOn: Bool
    let locked: Bool
    let sentryMode: Bool
    let pluggedIn: Bool
    let tirePressureFrontLeft: Double
    let tirePressureFrontRight: Double
    let tirePressureRearLeft: Double
    let tirePressureRearRight: Double
    let latitude: Double
    let longitude: Double
    let elevation: Double
    let speed: Int
    let power: Double
    let heading: Int
    let shiftState: String?
    let version: String?
    let isDcCharging: Bool

    enum CodingKeys: String, CodingKey {
        case carId = "car_id"
        case state, since
        case batteryDetails = "battery_details"
        case chargingDetails = "charging_details"
        case climateDetails = "climate_details"
        case tpmsDetails = "tpms_details"
        case carStatus = "car_status"
        case carGeodata = "car_geodata"
        case drivingDetails = "driving_details"
        case odometerDetails = "odometer_details"
        case carVersions = "car_versions"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        carId = try container.decode(Int.self, forKey: .carId)
        state = try container.decodeIfPresent(CarState.self, forKey: .state) ?? .offline
        since = try container.decodeIfPresent(String.self, forKey: .since)

        let battery = try container.decodeIfPresent(BatteryDetails.self, forKey: .batteryDetails)
        let charging = try container.decodeIfPresent(ChargingDetails.self, forKey: .chargingDetails)
        let climate = try container.decodeIfPresent(ClimateDetails.self, forKey: .climateDetails)
        let tpms = try container.decodeIfPresent(TpmsDetails.self, forKey: .tpmsDetails)
        let status = try container.decodeIfPresent(CarStatusContainer.self, forKey: .carStatus)
        let geodata = try container.decodeIfPresent(CarGeodata.self, forKey: .carGeodata)
        let driving = try container.decodeIfPresent(DrivingDetails.self, forKey: .drivingDetails)
        let odo = try container.decodeIfPresent(OdometerDetails.self, forKey: .odometerDetails)
        let versions = try container.decodeIfPresent(CarVersions.self, forKey: .carVersions)

        healthy = status?.healthy ?? false
        odometer = odo?.odometer ?? 0
        batteryLevel = battery?.batteryLevel ?? 0
        usableBatteryLevel = battery?.usableBatteryLevel ?? 0
        usableBatteryRangeKm = battery?.ratedBatteryRange ?? 0
        idealBatteryRangeKm = battery?.idealBatteryRange ?? 0
        chargeEnergyAdded = charging?.chargeEnergyAdded ?? 0
        chargeLimitSoc = charging?.chargeLimitSoc ?? 0
        chargerPower = Double(charging?.chargerPower ?? 0)
        chargerActualCurrent = charging?.chargerActualCurrent ?? 0
        chargerVoltage = charging?.chargerVoltage ?? 0
        chargePortDoorOpen = charging?.chargePortDoorOpen ?? false
        timeToFullCharge = charging?.timeToFullCharge ?? 0
        insideTemp = climate?.insideTemp ?? 0
        outsideTemp = climate?.outsideTemp ?? 0
        isClimateOn = climate?.isClimateOn ?? false
        locked = status?.locked ?? false
        sentryMode = status?.sentryMode ?? false
        pluggedIn = charging?.pluggedIn ?? false
        tirePressureFrontLeft = tpms?.pressureFl ?? 0
        tirePressureFrontRight = tpms?.pressureFr ?? 0
        tirePressureRearLeft = tpms?.pressureRl ?? 0
        tirePressureRearRight = tpms?.pressureRr ?? 0
        latitude = geodata?.latitude ?? 0
        longitude = geodata?.longitude ?? 0
        elevation = Double(driving?.elevation ?? 0)
        speed = driving?.speed ?? 0
        power = Double(driving?.power ?? 0)
        heading = driving?.heading ?? 0
        shiftState = driving?.shiftState
        version = versions?.version
        isDcCharging = charging?.isDcCharging ?? false
    }
}
```

- [ ] **Step 3: 验证编译**

确认 `CarStatus` 的所有消费方（DashboardView, CurrentChargeView 等）仍能访问同名属性。属性名保持不变，只是解码路径变了。

- [ ] **Step 4: Commit**

```bash
git add app_mimo/ios/MateLink/Core/Models/CarStatus.swift
git commit -m "fix(ios): nest CarStatus model for TeslaMate API v1.24+"
```

---

### Task 2: iOS 模型嵌套化 — Drive / Charge / BatteryHealth

**Files:**
- Modify: `app_mimo/ios/MateLink/Core/Models/CarStatus.swift:37-115`

- [ ] **Step 1: 添加 Drive 嵌套容器**

在 `CarStatus` 之后添加：

```swift
// MARK: - Drive nested containers

private struct DriveOdometerDetails: Codable {
    let distance: Double?
}

private struct DriveBatteryDetails: Codable {
    let startBatteryLevel: Int?
    let endBatteryLevel: Int?

    enum CodingKeys: String, CodingKey {
        case startBatteryLevel = "start_battery_level"
        case endBatteryLevel = "end_battery_level"
    }
}

private struct DriveRangeDetails: Codable {
    let startRange: Double?
    let endRange: Double?

    enum CodingKeys: String, CodingKey {
        case startRange = "start_range"
        case endRange = "end_range"
    }
}
```

- [ ] **Step 2: 重写 Drive struct**

替换现有 `Drive` struct：

```swift
struct Drive: Codable, Identifiable {
    let id: Int
    let carId: Int
    let startDate: String
    let endDate: String?
    let distanceKm: Double
    let durationMin: Int
    let efficiency: Double
    let startAddress: String?
    let endAddress: String?
    let startLatitude: Double?
    let startLongitude: Double?
    let endLatitude: Double?
    let endLongitude: Double?
    let startBatteryLevel: Int
    let endBatteryLevel: Int
    let startIdealRangeKm: Double
    let endIdealRangeKm: Double
    let outsideTempAvg: Double?
    let speedMax: Double
    let powerMax: Double
    let powerMin: Double
    let elevationGain: Double?
    let elevationLoss: Double?

    var consumptionKwh: Double { distanceKm * efficiency / 1000.0 }

    enum CodingKeys: String, CodingKey {
        case id, carId = "car_id"
        case startDate = "start_date", endDate = "end_date"
        case odometerDetails = "odometer_details"
        case batteryDetails = "battery_details"
        case rangeRated = "range_rated"
        case rangeIdeal = "range_ideal"
        case startAddress = "start_address", endAddress = "end_address"
        case startLatitude = "start_latitude", startLongitude = "start_longitude"
        case endLatitude = "end_latitude", endLongitude = "end_longitude"
        case durationMin = "duration_min", efficiency
        case outsideTempAvg = "outside_temp_avg"
        case speedMax = "speed_max", powerMax = "power_max", powerMin = "power_min"
        case elevationGain = "elevation_gain", elevationLoss = "elevation_loss"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(Int.self, forKey: .id)
        carId = try c.decode(Int.self, forKey: .carId)
        startDate = try c.decode(String.self, forKey: .startDate)
        endDate = try c.decodeIfPresent(String.self, forKey: .endDate)

        let odo = try c.decodeIfPresent(DriveOdometerDetails.self, forKey: .odometerDetails)
        let bat = try c.decodeIfPresent(DriveBatteryDetails.self, forKey: .batteryDetails)
        let rRated = try c.decodeIfPresent(DriveRangeDetails.self, forKey: .rangeRated)
        let rIdeal = try c.decodeIfPresent(DriveRangeDetails.self, forKey: .rangeIdeal)

        distanceKm = odo?.distance ?? 0
        durationMin = try c.decodeIfPresent(Int.self, forKey: .durationMin) ?? 0
        efficiency = try c.decodeIfPresent(Double.self, forKey: .efficiency) ?? 0
        startAddress = try c.decodeIfPresent(String.self, forKey: .startAddress)
        endAddress = try c.decodeIfPresent(String.self, forKey: .endAddress)
        startLatitude = try c.decodeIfPresent(Double.self, forKey: .startLatitude)
        startLongitude = try c.decodeIfPresent(Double.self, forKey: .startLongitude)
        endLatitude = try c.decodeIfPresent(Double.self, forKey: .endLatitude)
        endLongitude = try c.decodeIfPresent(Double.self, forKey: .endLongitude)
        startBatteryLevel = bat?.startBatteryLevel ?? 0
        endBatteryLevel = bat?.endBatteryLevel ?? 0
        startIdealRangeKm = rRated?.startRange ?? 0
        endIdealRangeKm = rRated?.endRange ?? 0
        outsideTempAvg = try c.decodeIfPresent(Double.self, forKey: .outsideTempAvg)
        speedMax = try c.decodeIfPresent(Double.self, forKey: .speedMax) ?? 0
        powerMax = try c.decodeIfPresent(Double.self, forKey: .powerMax) ?? 0
        powerMin = try c.decodeIfPresent(Double.self, forKey: .powerMin) ?? 0
        elevationGain = try c.decodeIfPresent(Double.self, forKey: .elevationGain)
        elevationLoss = try c.decodeIfPresent(Double.self, forKey: .elevationLoss)
    }
}
```

- [ ] **Step 3: 重写 Charge struct**

替换现有 `Charge` struct，保留 extension 兼容别名：

```swift
struct Charge: Codable, Identifiable {
    let id: Int
    let carId: Int
    let startDate: String
    let endDate: String?
    let chargeEnergyAdded: Double
    let startBatteryLevel: Int
    let endBatteryLevel: Int?
    let startIdealRangeKm: Double
    let endIdealRangeKm: Double?
    let startRatedRangeKm: Double
    let endRatedRangeKm: Double?
    let durationMin: Int
    let cost: Double?
    let address: String?
    let latitude: Double?
    let longitude: Double?
    let chargingType: String
    let powerMax: Double?
    let powerMin: Double?
    let outsideTempAvg: Double?

    enum CodingKeys: String, CodingKey {
        case id, carId = "car_id"
        case startDate = "start_date", endDate = "end_date"
        case chargeEnergyAdded = "charge_energy_added"
        case batteryDetails = "battery_details"
        case rangeRated = "range_rated"
        case rangeIdeal = "range_ideal"
        case durationMin = "duration_min"
        case cost, address, latitude, longitude
        case chargerPhases = "charger_phases"
        case powerMax = "power_max", powerMin = "power_min"
        case outsideTempAvg = "outside_temp_avg"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(Int.self, forKey: .id)
        carId = try c.decode(Int.self, forKey: .carId)
        startDate = try c.decode(String.self, forKey: .startDate)
        endDate = try c.decodeIfPresent(String.self, forKey: .endDate)
        chargeEnergyAdded = try c.decodeIfPresent(Double.self, forKey: .chargeEnergyAdded) ?? 0

        let bat = try c.decodeIfPresent(DriveBatteryDetails.self, forKey: .batteryDetails)
        let rRated = try c.decodeIfPresent(DriveRangeDetails.self, forKey: .rangeRated)
        let rIdeal = try c.decodeIfPresent(DriveRangeDetails.self, forKey: .rangeIdeal)

        startBatteryLevel = bat?.startBatteryLevel ?? 0
        endBatteryLevel = bat?.endBatteryLevel
        startIdealRangeKm = rRated?.startRange ?? 0
        endIdealRangeKm = rRated?.endRange
        startRatedRangeKm = rIdeal?.startRange ?? 0
        endRatedRangeKm = rIdeal?.endRange
        durationMin = try c.decodeIfPresent(Int.self, forKey: .durationMin) ?? 0
        cost = try c.decodeIfPresent(Double.self, forKey: .cost)
        address = try c.decodeIfPresent(String.self, forKey: .address)
        latitude = try c.decodeIfPresent(Double.self, forKey: .latitude)
        longitude = try c.decodeIfPresent(Double.self, forKey: .longitude)

        // Derive chargingType from charger_phases: 0=DC, 1/3=AC
        let phases = try c.decodeIfPresent(Int.self, forKey: .chargerPhases)
        chargingType = (phases == 0) ? "DC" : "AC"

        powerMax = try c.decodeIfPresent(Double.self, forKey: .powerMax)
        powerMin = try c.decodeIfPresent(Double.self, forKey: .powerMin)
        outsideTempAvg = try c.decodeIfPresent(Double.self, forKey: .outsideTempAvg)
    }

    // Memberwise init for Previews
    init(id: Int, carId: Int, startDate: String, endDate: String?, chargeEnergyAdded: Double,
         startBatteryLevel: Int, endBatteryLevel: Int?, startIdealRangeKm: Double, endIdealRangeKm: Double?,
         startRatedRangeKm: Double, endRatedRangeKm: Double?, durationMin: Int, cost: Double?,
         address: String?, latitude: Double, longitude: Double, chargingType: String,
         powerMax: Double?, powerMin: Double?, outsideTempAvg: Double?) {
        self.id = id; self.carId = carId; self.startDate = startDate; self.endDate = endDate
        self.chargeEnergyAdded = chargeEnergyAdded; self.startBatteryLevel = startBatteryLevel
        self.endBatteryLevel = endBatteryLevel; self.startIdealRangeKm = startIdealRangeKm
        self.endIdealRangeKm = endIdealRangeKm; self.startRatedRangeKm = startRatedRangeKm
        self.endRatedRangeKm = endRatedRangeKm; self.durationMin = durationMin; self.cost = cost
        self.address = address; self.latitude = latitude; self.longitude = longitude
        self.chargingType = chargingType; self.powerMax = powerMax; self.powerMin = powerMin
        self.outsideTempAvg = outsideTempAvg
    }
}
```

- [ ] **Step 4: 重写 BatteryHealth struct**

```swift
struct BatteryHealth: Codable {
    let carId: Int
    let date: String?
    let batteryLevel: Int?
    let ratedRangeKm: Double
    let idealRangeKm: Double
    let odometer: Double?
    let outsideTemp: Double?
    let usableBatteryLevel: Int?
    let healthPercentage: Double?
    let capacityDegradationPercent: Double?
    let originalCapacityKwh: Double?
    let currentCapacityKwh: Double?
    let history: [BatteryHealthPoint]?

    var mileageKm: Double { odometer ?? 0 }

    enum CodingKeys: String, CodingKey {
        case carId = "car_id", date
        case batteryLevel = "battery_level"
        case ratedRangeKm = "max_range"
        case idealRangeKm = "current_range"
        case odometer, outsideTemp = "outside_temp"
        case usableBatteryLevel = "usable_battery_level"
        case healthPercentage = "battery_health_percentage"
        case capacityDegradationPercent = "capacity_degradation_percent"
        case originalCapacityKwh = "original_capacity_kwh"
        case currentCapacityKwh = "current_capacity_kwh"
        case history
    }
}
```

- [ ] **Step 5: 更新 Charge extension 兼容别名**

保留现有 extension，确认 `chargeType` 等别名仍能编译。

- [ ] **Step 6: Commit**

```bash
git add app_mimo/ios/MateLink/Core/Models/CarStatus.swift
git commit -m "fix(ios): nest Drive/Charge/BatteryHealth models for TeslaMate API v1.24+"
```

---

### Task 3: iOS ApiClient 补全 + URL 安全

**Files:**
- Modify: `app_mimo/ios/MateLink/Core/API/ApiClient.swift`
- Create: `app_mimo/ios/MateLink/Core/API/UrlSecurity.swift`

- [ ] **Step 1: 创建 UrlSecurity.swift**

```swift
import Foundation

enum UrlSecurity {
    /// Returns nil if the URL is safe; returns an error message if it's not.
    static func validate(_ urlString: String, token: String?) -> String? {
        guard let url = URL(string: urlString), let host = url.host else {
            return "Invalid URL format"
        }
        // Only enforce HTTPS when sending a token to a non-local host
        if token != nil && !token!.isEmpty && url.scheme == "http" {
            let isLocal = host == "localhost"
                || host.hasPrefix("127.")
                || host.hasPrefix("192.168.")
                || host.hasPrefix("10.")
                || host.hasSuffix(".local")
            if !isLocal {
                return "HTTP is not allowed for remote servers. Use HTTPS to protect your token."
            }
        }
        return nil
    }
}
```

- [ ] **Step 2: 在 TeslaMateAPI.init 中添加 URL 校验**

在 `TeslaMateAPI` 的 `init` 中、设置 `session` 之前添加：

```swift
if let error = UrlSecurity.validate(baseURL, token: token) {
    print("[UrlSecurity] \(error)")
}
```

- [ ] **Step 3: 补齐 ApiClient 缺失端点**

在 `TeslaMateAPI` class 中添加以下方法（与 Android `TeslamateApi.kt` 对齐）：

```swift
func getCar(_ carId: Int) async throws -> Car {
    let resp: CarApiResponse = try await fetch("/api/v1/cars")
    guard let car = resp.data.cars.first(where: { $0.id == carId }) ?? resp.data.cars.first else {
        throw ApiError.invalidResponse
    }
    return car
}

func getCurrentCharge(_ carId: Int) async throws -> Charge? {
    return try? await fetch("/api/v1/cars/\(carId)/charges/current")
}

func getChargeDetail(_ carId: Int, chargeId: Int) async throws -> Charge {
    return try await fetch("/api/v1/cars/\(carId)/charges/\(chargeId)")
}

func getDriveDetail(_ carId: Int, driveId: Int) async throws -> Drive {
    return try await fetch("/api/v1/cars/\(carId)/drives/\(driveId)")
}

func getBatteryHealth(_ carId: Int) async throws -> BatteryHealth {
    return try await fetch("/api/v1/cars/\(carId)/battery")
}

func getUpdates(_ carId: Int) async throws -> [UpdateItem] {
    return try await fetch("/api/v1/cars/\(carId)/updates")
}

struct GlobalSettings: Codable {
    let unitOfLength: String?
    let unitOfPressure: String?
    let unitOfTemperature: String?

    enum CodingKeys: String, CodingKey {
        case unitOfLength = "unit_of_length"
        case unitOfPressure = "unit_of_pressure"
        case unitOfTemperature = "unit_of_temperature"
    }
}

func getGlobalSettings() async throws -> GlobalSettings {
    return try await fetch("/api/v1/settings")
}
```

- [ ] **Step 4: Commit**

```bash
git add app_mimo/ios/MateLink/Core/API/ApiClient.swift app_mimo/ios/MateLink/Core/API/UrlSecurity.swift
git commit -m "feat(ios): add UrlSecurity +补齐 7 API endpoints matching Android"
```

---

### Task 4: Android DashboardViewModel 空安全修复

**Files:**
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/dashboard/DashboardViewModel.kt:40-43`

- [ ] **Step 1: 修复空安全链**

将：
```kotlin
val cars = apiClient.api.getCars().data.cars
val status = apiClient.api.getCarStatus(carId).data
```

改为：
```kotlin
val carsResponse = apiClient.api.getCars()
val cars = carsResponse.body()?.data?.cars ?: emptyList()
val statusResponse = apiClient.api.getCarStatus(carId)
val status = statusResponse.body()?.data?.status
```

- [ ] **Step 2: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/dashboard/DashboardViewModel.kt
git commit -m "fix(android): null-safe API response chains in DashboardViewModel"
```

---

### Task 5: Android NetworkModule Auth 冲突修复

**Files:**
- Modify: `app_mimo/android/app/src/main/java/com/matelink/di/NetworkModule.kt:173-183`

- [ ] **Step 1: 改为 if/else 互斥**

将两个独立 `if` 改为 Bearer 优先的 `if/else`：

```kotlin
if (apiToken.isNotBlank()) {
    requestBuilder.addHeader("Authorization", "Bearer $apiToken")
} else if (basicAuthUsername.isNotBlank() && basicAuthPassword.isNotBlank()) {
    requestBuilder.addHeader("Authorization", okhttp3.Credentials.basic(basicAuthUsername, basicAuthPassword))
}
```

- [ ] **Step 2: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/di/NetworkModule.kt
git commit -m "fix(android): resolve dual Authorization header conflict"
```

---

## Phase 2: 页面补全

### Task 6: Android 效率分析页面

**Files:**
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/efficiency/EfficiencyScreen.kt`
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/efficiency/EfficiencyViewModel.kt`

- [ ] **Step 1: 创建 EfficiencyViewModel**

从 `Drive` 历史计算效率分布：

```kotlin
@HiltViewModel
class EfficiencyViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {
    data class UiState(
        val loading: Boolean = true,
        val avgEfficiency: Double = 0.0,
        val efficiencyBySpeed: List<Pair<Int, Double>> = emptyList(), // speed_bin → avg_whkm
        val drives: List<Drive> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load(carId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val drives = repository.getDrives(carId)
            val avg = if (drives.isNotEmpty()) drives.map { it.efficiency }.average() else 0.0
            val bySpeed = drives
                .filter { it.speedMax > 0 }
                .groupBy { (it.speedMax / 10).toInt() * 10 }
                .map { (bin, list) -> bin to list.map { it.efficiency }.average() }
                .sortedBy { it.first }
            _uiState.value = UiState(false, avg, bySpeed, drives)
        }
    }
}
```

- [ ] **Step 2: 创建 EfficiencyScreen**

使用 Compose Canvas 绘制散点图（速度 vs 能耗），参考 `git_ref` 的图表实现。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/efficiency/
git commit -m "feat(android): add EfficiencyScreen with speed-efficiency scatter chart"
```

---

### Task 7: Android 成本分析页面

**Files:**
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/cost/CostScreen.kt`
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/cost/CostViewModel.kt`

- [ ] **Step 1: 创建 CostViewModel**

从 `Charge` 历史聚合月度 AC/DC 成本：

```kotlin
@HiltViewModel
class CostViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {
    data class MonthlyCost(val month: String, val acCost: Double, val dcCost: Double)
    data class LocationCost(val address: String, val totalCost: Double, val count: Int)

    data class UiState(
        val loading: Boolean = true,
        val totalCost: Double = 0.0,
        val monthlyCosts: List<MonthlyCost> = emptyList(),
        val topLocations: List<LocationCost> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load(carId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val charges = repository.getCharges(carId)
            // Group by month, split AC/DC
            val monthly = charges.filter { it.cost != null && it.cost > 0 }
                .groupBy { it.startDate.take(7) }
                .map { (month, list) ->
                    val ac = list.filter { it.chargingType != "DC" }.sumOf { it.cost ?: 0.0 }
                    val dc = list.filter { it.chargingType == "DC" }.sumOf { it.cost ?: 0.0 }
                    MonthlyCost(month, ac, dc)
                }.sortedBy { it.month }
            // Top locations
            val locations = charges.filter { it.address != null && it.cost != null }
                .groupBy { it.address!! }
                .map { (addr, list) -> LocationCost(addr, list.sumOf { it.cost ?: 0.0 }, list.size) }
                .sortedByDescending { it.totalCost }.take(5)
            val total = charges.sumOf { it.cost ?: 0.0 }
            _uiState.value = UiState(false, total, monthly, locations)
        }
    }
}
```

- [ ] **Step 2: 创建 CostScreen**

月度堆叠柱状图 (AC/DC) + 总费用卡片 + 位置排行列表。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/cost/
git commit -m "feat(android): add CostScreen with monthly AC/DC stacked bar chart"
```

---

### Task 8: Android 续航分析页面

**Files:**
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/range/RangeScreen.kt`
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/range/RangeViewModel.kt`

- [ ] **Step 1: 创建 RangeViewModel**

比较 rated range vs 实际里程：

```kotlin
@HiltViewModel
class RangeViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {
    data class RangeTrip(
        val drive: Drive,
        val estimatedRange: Double, // from rated range delta
        val actualRange: Double,    // distance driven
        val diff: Double            // estimated - actual
    )

    data class UiState(
        val loading: Boolean = true,
        val avgAccuracy: Double = 0.0,
        val trips: List<RangeTrip> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load(carId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val drives = repository.getDrives(carId)
            val trips = drives.map { d ->
                val estimated = d.startRatedRangeKm - d.endRatedRangeKm
                RangeTrip(d, estimated, d.diff)
            }
            val avgAcc = if (trips.isNotEmpty()) {
                trips.map { if (it.estimatedRange > 0) (1 - abs(it.diff) / it.estimatedRange) * 100 else 100.0 }.average()
            } else 100.0
            _uiState.value = UiState(false, avgAcc, trips)
        }
    }
}
```

- [ ] **Step 2: 创建 RangeScreen**

预估 vs 实际偏差折线图 + 准确率卡片 + 影响因素。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/range/
git commit -m "feat(android): add RangeScreen with estimated vs actual comparison"
```

---

### Task 9: Android 待机耗电页面

**Files:**
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/vampire/VampireScreen.kt`
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/vampire/VampireViewModel.kt`

- [ ] **Step 1: 创建 VampireViewModel**

从 `Charge` 记录中计算充电间隔期间的电量损耗：

```kotlin
@HiltViewModel
class VampireViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {
    data class IdleDrain(
        val date: String,
        val drainPercent: Double,
        val hours: Double,
        val avgPowerW: Double
    )

    data class UiState(
        val loading: Boolean = true,
        val totalDrainPercent: Double = 0.0,
        val avgPowerW: Double = 0.0,
        val dailyDrains: List<IdleDrain> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load(carId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            // Calculate idle drain from consecutive charge/drive records
            val charges = repository.getCharges(carId)
            val drives = repository.getDrives(carId)
            // Sort all events by date, find gaps where battery dropped
            // ... implementation from git_ref
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }
}
```

- [ ] **Step 2: 创建 VampireScreen**

空闲损耗功率卡片 + 每日柱状图 + 优化建议。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/vampire/
git commit -m "feat(android): add VampireScreen with idle drain analysis"
```

---

### Task 10: Android 时间线页面

**Files:**
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/timeline/TimelineScreen.kt`
- Create: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/timeline/TimelineViewModel.kt`

- [ ] **Step 1: 创建 TimelineViewModel**

合并 Drive + Charge 事件为时间线：

```kotlin
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {
    sealed class TimelineEvent {
        abstract val timestamp: String
        data class DriveEvent(val drive: Drive) : TimelineEvent() { override val timestamp get() = drive.startDate }
        data class ChargeEvent(val charge: Charge) : TimelineEvent() { override val timestamp get() = charge.startDate }
    }

    data class UiState(
        val loading: Boolean = true,
        val events: List<TimelineEvent> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load(carId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val drives = repository.getDrives(carId).map { TimelineEvent.DriveEvent(it) }
            val charges = repository.getCharges(carId).map { TimelineEvent.ChargeEvent(it) }
            val events = (drives + charges).sortedByDescending { it.timestamp }
            _uiState.value = UiState(false, events)
        }
    }
}
```

- [ ] **Step 2: 创建 TimelineScreen**

24h 横向时间轴 + 事件类型图标 (驾驶=绿, 充电=橙)。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/screens/timeline/
git commit -m "feat(android): add TimelineScreen with drive/charge event timeline"
```

---

### Task 11: Android NavGraph 路由注册

**Files:**
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/navigation/NavGraph.kt`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/navigation/MateLinkNavHost.kt`

- [ ] **Step 1: 在 NavGraph 中添加 5 个新路由**

```kotlin
composable("efficiency/{carId}") { backStackEntry ->
    val carId = backStackEntry.arguments?.getString("carId")?.toIntOrNull() ?: 1
    EfficiencyScreen(carId = carId, onBack = { navController.popBackStack() })
}
composable("cost/{carId}") { ... }
composable("range/{carId}") { ... }
composable("vampire/{carId}") { ... }
composable("timeline/{carId}") { ... }
```

- [ ] **Step 2: 在 MateLinkNavHost 的 MoreScreen 导航中注册入口**

确保 MoreScreen 的导航链接能到达这 5 个新路由。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/android/app/src/main/java/com/matelink/ui/navigation/
git commit -m "feat(android): register 5 new analysis routes in NavGraph"
```

---

### Task 12: 中文化 — Android

**Files:**
- Modify: `app_mimo/android/app/src/main/res/values-zh/strings.xml`

- [ ] **Step 1: 添加新页面中文字符串**

```xml
<!-- Efficiency -->
<string name="efficiency_title">效率分析</string>
<string name="efficiency_avg">平均能耗</string>
<string name="efficiency_by_speed">速度区间效率</string>

<!-- Cost -->
<string name="cost_title">成本分析</string>
<string name="cost_total">总费用</string>
<string name="cost_monthly">月度费用</string>
<string name="cost_top_locations">费用最高地点</string>

<!-- Range -->
<string name="range_title">续航分析</string>
<string name="range_accuracy">续航准确率</string>
<string name="range_estimated">预估续航</string>
<string name="range_actual">实际续航</string>

<!-- Vampire -->
<string name="vampire_title">待机耗电</string>
<string name="vampire_total_drain">总待机损耗</string>
<string name="vampire_avg_power">平均待机功率</string>

<!-- Timeline -->
<string name="timeline_title">时间线</string>
<string name="timeline_drive">驾驶</string>
<string name="timeline_charge">充电</string>

<!-- About -->
<string name="about_title">关于</string>
<string name="about_version">版本</string>
<string name="about_tech_stack">技术栈</string>
```

- [ ] **Step 2: Commit**

```bash
git add app_mimo/android/app/src/main/res/values-zh/strings.xml
git commit -m "feat(android): add Chinese strings for new analysis pages"
```

---

### Task 13: 中文化 — iOS

**Files:**
- Create: `app_mimo/ios/MateLink/Resources/zh-Hans.lproj/Localizable.strings`
- Modify: `app_mimo/ios/MateLink/Core/Utils/Localization.swift`

- [ ] **Step 1: 创建 Localizable.strings**

```
/* Tab labels */
"nav.dashboard" = "仪表盘";
"nav.drives" = "行程";
"nav.charges" = "充电";
"nav.more" = "更多";

/* Page titles */
"title.drive_detail" = "行程详情";
"title.charge_detail" = "充电详情";
"title.current_charge" = "当前充电";
"title.statistics" = "统计";
"title.battery_health" = "电池健康";
"title.efficiency" = "效率分析";
"title.cost" = "成本分析";
"title.range" = "续航分析";
"title.vampire" = "待机耗电";
"title.timeline" = "时间线";
"title.mileage" = "里程钻取";
"title.updates" = "固件版本";
"title.sentry" = "哨兵历史";
"title.settings" = "设置";
"title.about" = "关于";
"title.more" = "更多";

/* Status */
"status.online" = "在线";
"status.offline" = "离线";
"status.charging" = "充电中";
"status.driving" = "驾驶中";
"status.asleep" = "休眠";
"status.locked" = "已锁车";
"status.unlocked" = "未锁车";

/* Common */
"common.loading" = "加载中...";
"common.no_data" = "暂无数据";
"common.distance" = "距离";
"common.duration" = "时长";
"common.energy" = "能耗";
"common.cost" = "费用";
"common.free" = "免费";
```

- [ ] **Step 2: 更新 Localization.swift 确保加载 zh-Hans**

确认 `L10n.string()` 从 `zh-Hans.lproj/Localizable.strings` 加载。

- [ ] **Step 3: Commit**

```bash
git add app_mimo/ios/MateLink/Resources/zh-Hans.lproj/ app_mimo/ios/MateLink/Core/Utils/Localization.swift
git commit -m "feat(ios): add Chinese localization strings"
```

---

## Phase 3: Bug 收尾

### Task 14: iOS Bug 修复组

**Files:**
- Modify: `app_mimo/ios/MateLink/Features/Drives/DriveDetailView.swift:131`
- Modify: `app_mimo/ios/MateLink/Features/Range/RangeView.swift:27-31`
- Modify: `app_mimo/ios/MateLink/Features/Dashboard/DashboardView.swift`

- [ ] **Step 1: 修复 DriveDetailView 假 maxSpeed**

将 L131 的：
```swift
let maxSpeed: Int = Int(Double(avgSpeed) * 1.5)
```
改为：
```swift
let maxSpeed: Int = Int(drive.speedMax)
```

- [ ] **Step 2: 重写 RangeView accuracy 计算**

将 L27-31 的恒为 0% 逻辑改为：
```swift
var accuracy: Double {
    let validTrips = trips.filter { $0.estimated > 0 }
    guard !validTrips.isEmpty else { return 100 }
    return validTrips.map { trip in
        let error = abs(Double(trip.actual - trip.estimated)) / Double(trip.estimated)
        return (1 - error) * 100
    }.average
}

extension Array where Element == Double {
    var average: Double { isEmpty ? 0 : reduce(0, +) / Double(count) }
}
```

- [ ] **Step 3: 修复 DashboardView Timer 泄漏**

将：
```swift
let timer = Timer.publish(every: 5, on: .main, in: .common).autoconnect()
```
改为：
```swift
@State private var timerCancellable: Cancellable?
// ...
.onAppear {
    timerCancellable = Timer.publish(every: 5, on: .main, in: .common)
        .autoconnect()
        .sink { _ in loadData() }
}
.onDisappear {
    timerCancellable?.cancel()
}
```

- [ ] **Step 4: 标注 BatteryTrendCard 为 Demo 数据**

将：
```swift
let data: [Int] = [75, 72, 68, 70, 73, 76, 78]
```
改为带标注：
```swift
// Demo data — real 7-day history requires /api/v1/cars/{id}/status_history
let data: [Int] = [75, 72, 68, 70, 73, 76, 78]
```
并在 UI 上显示 "Demo" 水印或文字。

- [ ] **Step 5: 修复 widget Double→Int**

将：
```swift
defaults.set(s.usableBatteryRangeKm, forKey: "widget_range")
```
改为：
```swift
defaults.set(Int(s.usableBatteryRangeKm), forKey: "widget_range")
```

- [ ] **Step 6: Commit**

```bash
git add app_mimo/ios/MateLink/Features/Drives/DriveDetailView.swift app_mimo/ios/MateLink/Features/Range/RangeView.swift app_mimo/ios/MateLink/Features/Dashboard/DashboardView.swift
git commit -m "fix(ios): maxSpeed/accuracy/timer/widget bug fixes"
```

---

### Task 15: Android Bug 修复组

**Files:**
- Modify: `app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt:30-64`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/dashboard/DashboardScreen.kt:88-152`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/data/repository/SettingsRepository.kt:33`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/data/repository/StatsRepository.kt:706`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/battery/BatteryViewModel.kt:165`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/service/ChargingMonitorService.kt:67`
- Modify: `app_mimo/android/app/src/main/java/com/matelink/ui/screens/dashboard/DashboardViewModel.kt:59-71`

- [ ] **Step 1: ApiClient 线程安全**

将 `cachedApi` 和 `cachedBaseUrl` 加 `@Volatile`：

```kotlin
@Volatile private var cachedApi: TeslaMateApi? = null
@Volatile private var cachedBaseUrl: String? = null
```

- [ ] **Step 2: DashboardScreen 可空字段处理**

在使用 `status` 属性前添加默认值映射：
```kotlin
val batteryLevel = status?.batteryLevel ?: 0
val odometer = status?.odometer ?: 0.0
// ... etc
```

- [ ] **Step 3: MockMode 默认改为 false**

```kotlin
val mockMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.MOCK_MODE] ?: false }
```

- [ ] **Step 4: StatsRepository 日期解析保护**

```kotlin
val prevDate = runCatching { java.time.LocalDate.parse(sortedDays[i - 1]) }.getOrNull() ?: continue
val currDate = runCatching { java.time.LocalDate.parse(sortedDays[i]) }.getOrNull() ?: continue
```

- [ ] **Step 5: BatteryViewModel 外推阈值**

```kotlin
val rangeAt100 = if (batteryLevel >= 10 && ratedRange > 0) {
    (ratedRange / batteryLevel) * 100
} else {
    maxRangeNow
}
```

- [ ] **Step 6: ChargingMonitorService 线程安全**

```kotlin
private val activeNotificationCarIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
```

- [ ] **Step 7: DashboardViewModel 异常处理**

```kotlin
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    // Log but don't swallow
}
```

- [ ] **Step 8: Commit**

```bash
git add app_mimo/android/
git commit -m "fix(android): thread safety, null safety, MockMode default, date parsing"
```

---

## 验证检查点

### Phase 1 完成后
- [ ] iOS `CarStatus` 能 decode 嵌套 JSON（用 mock_data.json 测试向量验证）
- [ ] iOS `ApiClient` 有 getCar/getCurrentCharge/getChargeDetail/getDriveDetail/getBatteryHealth/getUpdates/getGlobalSettings 方法
- [ ] Android DashboardViewModel 不再 NPE
- [ ] Android 只发送一个 Authorization header

### Phase 2 完成后
- [ ] Android 有 EfficiencyScreen/CostScreen/RangeScreen/VampireScreen/TimelineScreen 文件
- [ ] NavGraph 注册了 5 个新路由
- [ ] iOS 有 zh-Hans.lproj/Localizable.strings
- [ ] Tab 标签显示中文

### Phase 3 完成后
- [ ] iOS DriveDetailView 显示真实 maxSpeed
- [ ] iOS RangeView accuracy 不为 0%
- [ ] Android MockMode 默认 false
- [ ] Android StatsRepository 不因日期格式崩溃
