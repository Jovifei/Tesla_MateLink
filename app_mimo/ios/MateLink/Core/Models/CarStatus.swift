import Foundation

enum CarState: String, Codable { case online, offline, asleep, charging, driving }

// MARK: - Nested API containers (TeslaMate API v1.24+)

private struct BatteryDetails: Decodable {
    let batteryLevel: Int
    let usableBatteryLevel: Int
    let estBatteryRange: Double
    let ratedBatteryRange: Double
    let idealBatteryRange: Double

    private enum CodingKeys: String, CodingKey {
        case batteryLevel = "battery_level"
        case usableBatteryLevel = "usable_battery_level"
        case estBatteryRange = "est_battery_range"
        case ratedBatteryRange = "rated_battery_range"
        case idealBatteryRange = "ideal_battery_range"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        batteryLevel = (try? c.decode(Int.self, forKey: .batteryLevel)) ?? 0
        usableBatteryLevel = (try? c.decode(Int.self, forKey: .usableBatteryLevel)) ?? 0
        estBatteryRange = (try? c.decode(Double.self, forKey: .estBatteryRange)) ?? 0
        ratedBatteryRange = (try? c.decode(Double.self, forKey: .ratedBatteryRange)) ?? 0
        idealBatteryRange = (try? c.decode(Double.self, forKey: .idealBatteryRange)) ?? 0
    }
}

private struct ChargingDetails: Decodable {
    let pluggedIn: Bool
    let chargingState: String
    let chargeEnergyAdded: Double
    let chargeLimitSoc: Int
    let chargePortDoorOpen: Bool
    let chargerActualCurrent: Int
    let chargerPhases: Int
    let chargerPower: Double
    let chargerVoltage: Int
    let chargeCurrentRequest: Int
    let chargeCurrentRequestMax: Int
    let timeToFullCharge: Double

    /// True only while *actively* DC charging (charger_phases == 0).
    var isDcCharging: Bool { chargerPhases == 0 }

    private enum CodingKeys: String, CodingKey {
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

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        pluggedIn = (try? c.decode(Bool.self, forKey: .pluggedIn)) ?? false
        chargingState = (try? c.decode(String.self, forKey: .chargingState)) ?? "unknown"
        chargeEnergyAdded = (try? c.decode(Double.self, forKey: .chargeEnergyAdded)) ?? 0
        chargeLimitSoc = (try? c.decode(Int.self, forKey: .chargeLimitSoc)) ?? 0
        chargePortDoorOpen = (try? c.decode(Bool.self, forKey: .chargePortDoorOpen)) ?? false
        chargerActualCurrent = (try? c.decode(Int.self, forKey: .chargerActualCurrent)) ?? 0
        chargerPhases = (try? c.decode(Int.self, forKey: .chargerPhases)) ?? 0
        chargerPower = (try? c.decode(Double.self, forKey: .chargerPower)) ?? 0
        chargerVoltage = (try? c.decode(Int.self, forKey: .chargerVoltage)) ?? 0
        chargeCurrentRequest = (try? c.decode(Int.self, forKey: .chargeCurrentRequest)) ?? 0
        chargeCurrentRequestMax = (try? c.decode(Int.self, forKey: .chargeCurrentRequestMax)) ?? 0
        timeToFullCharge = (try? c.decode(Double.self, forKey: .timeToFullCharge)) ?? 0
    }
}

private struct ClimateDetails: Decodable {
    let isClimateOn: Bool
    let insideTemp: Double
    let outsideTemp: Double

    private enum CodingKeys: String, CodingKey {
        case isClimateOn = "is_climate_on"
        case insideTemp = "inside_temp"
        case outsideTemp = "outside_temp"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        isClimateOn = (try? c.decode(Bool.self, forKey: .isClimateOn)) ?? false
        insideTemp = (try? c.decode(Double.self, forKey: .insideTemp)) ?? 0
        outsideTemp = (try? c.decode(Double.self, forKey: .outsideTemp)) ?? 0
    }
}

private struct TpmsDetails: Decodable {
    let tpmsPressureFl: Double
    let tpmsPressureFr: Double
    let tpmsPressureRl: Double
    let tpmsPressureRr: Double

    private enum CodingKeys: String, CodingKey {
        case tpmsPressureFl = "tpms_pressure_fl"
        case tpmsPressureFr = "tpms_pressure_fr"
        case tpmsPressureRl = "tpms_pressure_rl"
        case tpmsPressureRr = "tpms_pressure_rr"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        tpmsPressureFl = (try? c.decode(Double.self, forKey: .tpmsPressureFl)) ?? 0
        tpmsPressureFr = (try? c.decode(Double.self, forKey: .tpmsPressureFr)) ?? 0
        tpmsPressureRl = (try? c.decode(Double.self, forKey: .tpmsPressureRl)) ?? 0
        tpmsPressureRr = (try? c.decode(Double.self, forKey: .tpmsPressureRr)) ?? 0
    }
}

private struct CarStatusContainer: Decodable {
    let healthy: Bool
    let locked: Bool
    let sentryMode: Bool
    let windowsOpen: Bool
    let doorsOpen: Bool
    let centerDisplayState: String

    private enum CodingKeys: String, CodingKey {
        case healthy
        case locked
        case sentryMode = "sentry_mode"
        case windowsOpen = "windows_open"
        case doorsOpen = "doors_open"
        case centerDisplayState = "center_display_state"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        healthy = (try? c.decode(Bool.self, forKey: .healthy)) ?? false
        locked = (try? c.decode(Bool.self, forKey: .locked)) ?? false
        sentryMode = (try? c.decode(Bool.self, forKey: .sentryMode)) ?? false
        windowsOpen = (try? c.decode(Bool.self, forKey: .windowsOpen)) ?? false
        doorsOpen = (try? c.decode(Bool.self, forKey: .doorsOpen)) ?? false
        centerDisplayState = (try? c.decode(String.self, forKey: .centerDisplayState)) ?? ""
    }
}

private struct CarGeodata: Decodable {
    let geofence: String
    let latitude: Double
    let longitude: Double

    private enum CodingKeys: String, CodingKey {
        case geofence, latitude, longitude
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        geofence = (try? c.decode(String.self, forKey: .geofence)) ?? ""
        latitude = (try? c.decode(Double.self, forKey: .latitude)) ?? 0
        longitude = (try? c.decode(Double.self, forKey: .longitude)) ?? 0
    }
}

private struct DrivingDetails: Decodable {
    let shiftState: String
    let power: Double
    let speed: Int
    let heading: Int
    let elevation: Double

    private enum CodingKeys: String, CodingKey {
        case shiftState = "shift_state"
        case power, speed, heading, elevation
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        shiftState = (try? c.decode(String.self, forKey: .shiftState)) ?? ""
        power = (try? c.decode(Double.self, forKey: .power)) ?? 0
        speed = (try? c.decode(Int.self, forKey: .speed)) ?? 0
        heading = (try? c.decode(Int.self, forKey: .heading)) ?? 0
        elevation = (try? c.decode(Double.self, forKey: .elevation)) ?? 0
    }
}

private struct OdometerDetails: Decodable {
    let odometer: Double

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        odometer = (try? c.decode(Double.self, forKey: .odometer)) ?? 0
    }

    private enum CodingKeys: String, CodingKey { case odometer }
}

private struct CarVersions: Decodable {
    let version: String
    let updateAvailable: Bool
    let updateVersion: String

    private enum CodingKeys: String, CodingKey {
        case version
        case updateAvailable = "update_available"
        case updateVersion = "update_version"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        version = (try? c.decode(String.self, forKey: .version)) ?? ""
        updateAvailable = (try? c.decode(Bool.self, forKey: .updateAvailable)) ?? false
        updateVersion = (try? c.decode(String.self, forKey: .updateVersion)) ?? ""
    }
}

// MARK: - CarStatus

struct CarStatus: Codable {
    let carId: Int
    let state: CarState
    let since: String
    let healthy: Bool
    let odometer: Int
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
    let isDcCharging: Bool
    let version: String?

    /// Explicit memberwise init for mock/preview use (suppressed by custom init(from:)).
    init(
        carId: Int, state: CarState, since: String, healthy: Bool,
        odometer: Int, batteryLevel: Int, usableBatteryLevel: Int,
        usableBatteryRangeKm: Double, idealBatteryRangeKm: Double,
        chargeEnergyAdded: Double, chargeLimitSoc: Int,
        chargerPower: Double, chargerActualCurrent: Int, chargerVoltage: Int,
        chargePortDoorOpen: Bool, timeToFullCharge: Double,
        insideTemp: Double, outsideTemp: Double, isClimateOn: Bool,
        locked: Bool, sentryMode: Bool, pluggedIn: Bool,
        tirePressureFrontLeft: Double, tirePressureFrontRight: Double,
        tirePressureRearLeft: Double, tirePressureRearRight: Double,
        latitude: Double, longitude: Double, elevation: Double,
        speed: Int, power: Double, heading: Int,
        shiftState: String?, isDcCharging: Bool = false, version: String? = nil
    ) {
        self.carId = carId; self.state = state; self.since = since; self.healthy = healthy
        self.odometer = odometer; self.batteryLevel = batteryLevel; self.usableBatteryLevel = usableBatteryLevel
        self.usableBatteryRangeKm = usableBatteryRangeKm; self.idealBatteryRangeKm = idealBatteryRangeKm
        self.chargeEnergyAdded = chargeEnergyAdded; self.chargeLimitSoc = chargeLimitSoc
        self.chargerPower = chargerPower; self.chargerActualCurrent = chargerActualCurrent; self.chargerVoltage = chargerVoltage
        self.chargePortDoorOpen = chargePortDoorOpen; self.timeToFullCharge = timeToFullCharge
        self.insideTemp = insideTemp; self.outsideTemp = outsideTemp; self.isClimateOn = isClimateOn
        self.locked = locked; self.sentryMode = sentryMode; self.pluggedIn = pluggedIn
        self.tirePressureFrontLeft = tirePressureFrontLeft; self.tirePressureFrontRight = tirePressureFrontRight
        self.tirePressureRearLeft = tirePressureRearLeft; self.tirePressureRearRight = tirePressureRearRight
        self.latitude = latitude; self.longitude = longitude; self.elevation = elevation
        self.speed = speed; self.power = power; self.heading = heading
        self.shiftState = shiftState; self.isDcCharging = isDcCharging; self.version = version
    }

    private enum TopKeys: String, CodingKey {
        case carId = "car_id"
        case state, since
        case batteryDetails = "battery_details"
        case chargingDetails = "charging_details"
        case climateDetails = "climate_details"
        case tpmsDetails = "tpms_details"
        case carStatus = "car_status"
        case carGeodata = "car_geodata"
        case drivingDetails = "driving_details"
        case odometer
        case carVersions = "car_versions"
    }

    init(from decoder: Decoder) throws {
        let top = try decoder.container(keyedBy: TopKeys.self)
        carId = try top.decode(Int.self, forKey: .carId)
        state = try top.decode(CarState.self, forKey: .state)
        since = (try? top.decode(String.self, forKey: .since)) ?? ""

        let battery = try top.decode(BatteryDetails.self, forKey: .batteryDetails)
        let charging = try top.decode(ChargingDetails.self, forKey: .chargingDetails)
        let climate = try top.decode(ClimateDetails.self, forKey: .climateDetails)
        let tpms = try top.decode(TpmsDetails.self, forKey: .tpmsDetails)
        let statusContainer = try top.decode(CarStatusContainer.self, forKey: .carStatus)
        let geodata = try top.decode(CarGeodata.self, forKey: .carGeodata)
        let driving = try top.decode(DrivingDetails.self, forKey: .drivingDetails)

        // Odometer is a top-level value in the API
        let odometerValue: Double = (try? top.decode(Double.self, forKey: .odometer)) ?? 0
        odometer = Int(odometerValue)

        // Battery
        batteryLevel = battery.batteryLevel
        usableBatteryLevel = battery.usableBatteryLevel
        usableBatteryRangeKm = battery.ratedBatteryRange
        idealBatteryRangeKm = battery.idealBatteryRange

        // Charging
        pluggedIn = charging.pluggedIn
        chargeEnergyAdded = charging.chargeEnergyAdded
        chargeLimitSoc = charging.chargeLimitSoc
        chargePortDoorOpen = charging.chargePortDoorOpen
        chargerActualCurrent = charging.chargerActualCurrent
        chargerPower = charging.chargerPower
        chargerVoltage = charging.chargerVoltage
        timeToFullCharge = charging.timeToFullCharge
        isDcCharging = charging.isDcCharging

        // Climate
        insideTemp = climate.insideTemp
        outsideTemp = climate.outsideTemp
        isClimateOn = climate.isClimateOn

        // Status container
        healthy = statusContainer.healthy
        locked = statusContainer.locked
        sentryMode = statusContainer.sentryMode

        // TPMS
        tirePressureFrontLeft = tpms.tpmsPressureFl
        tirePressureFrontRight = tpms.tpmsPressureFr
        tirePressureRearLeft = tpms.tpmsPressureRl
        tirePressureRearRight = tpms.tpmsPressureRr

        // Geodata
        latitude = geodata.latitude
        longitude = geodata.longitude

        // Driving
        shiftState = driving.shiftState.isEmpty ? nil : driving.shiftState
        speed = driving.speed
        power = driving.power
        heading = driving.heading
        elevation = driving.elevation

        // Versions (optional — may be absent)
        if let versions = try? top.decode(CarVersions.self, forKey: .carVersions) {
            version = versions.version.isEmpty ? nil : versions.version
        } else {
            version = nil
        }
    }

    func encode(to encoder: Encoder) throws {
        var top = encoder.container(keyedBy: TopKeys.self)
        try top.encode(carId, forKey: .carId)
        try top.encode(state, forKey: .state)
        try top.encode(since, forKey: .since)
    }
}

struct Drive: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String
    let distanceKm: Double; let durationMin: Int; let efficiency: Double
    let startAddress: String; let endAddress: String
    let startLatitude: Double; let startLongitude: Double
    let endLatitude: Double; let endLongitude: Double
    let startBatteryLevel: Int; let endBatteryLevel: Int
    let startIdealRangeKm: Double; let endIdealRangeKm: Double
    let outsideTempAvg: Double; let speedMax: Double; let powerMax: Double; let powerMin: Double
    let elevationGain: Double; let elevationLoss: Double

    /// 行程能耗 (kWh) = 距离(km) × 效率(Wh/km) / 1000
    var consumptionKwh: Double { distanceKm * efficiency / 1000.0 }

    enum CodingKeys: String, CodingKey {
        case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"
        case distanceKm = "distance_km"; case durationMin = "duration_min"; case efficiency
        case startAddress = "start_address"; case endAddress = "end_address"
        case startLatitude = "start_latitude"; case startLongitude = "start_longitude"
        case endLatitude = "end_latitude"; case endLongitude = "end_longitude"
        case startBatteryLevel = "start_battery_level"; case endBatteryLevel = "end_battery_level"
        case startIdealRangeKm = "start_ideal_range_km"; case endIdealRangeKm = "end_ideal_range_km"
        case outsideTempAvg = "outside_temp_avg"; case speedMax = "speed_max"
        case powerMax = "power_max"; case powerMin = "power_min"
        case elevationGain = "elevation_gain"; case elevationLoss = "elevation_loss"
    }
}

struct Charge: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String?
    let chargeEnergyAdded: Double; let startBatteryLevel: Int; let endBatteryLevel: Int?
    let startIdealRangeKm: Double; let endIdealRangeKm: Double?
    let startRatedRangeKm: Double; let endRatedRangeKm: Double?
    let durationMin: Int; let cost: Double?; let address: String?
    let latitude: Double; let longitude: Double
    let chargingType: String; let powerMax: Double; let powerMin: Double
    let outsideTempAvg: Double

    enum CodingKeys: String, CodingKey {
        case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"
        case chargeEnergyAdded = "charge_energy_added"
        case startBatteryLevel = "start_battery_level"; case endBatteryLevel = "end_battery_level"
        case startIdealRangeKm = "start_ideal_range_km"; case endIdealRangeKm = "end_ideal_range_km"
        case startRatedRangeKm = "start_rated_range_km"; case endRatedRangeKm = "end_rated_range_km"
        case durationMin = "duration_min"; case cost; case address
        case latitude, longitude; case chargingType = "charging_type"
        case powerMax = "power_max"; case powerMin = "power_min"
        case outsideTempAvg = "outside_temp_avg"
    }
}

struct BatteryHealthPoint: Codable, Identifiable {
    var id: String { date }
    let date: String
    let capacityKwh: Double
}

struct BatteryHealth: Codable {
    let carId: Int; let date: String; let batteryLevel: Int
    let ratedRangeKm: Double; let idealRangeKm: Double
    let odometer: Double; let outsideTemp: Double; let usableBatteryLevel: Int
    // 可选扩展字段：API/mock 未提供时为 nil，View 用默认值兜底
    let capacityDegradationPercent: Double?
    let originalCapacityKwh: Double?
    let currentCapacityKwh: Double?
    let history: [BatteryHealthPoint]?

    /// 里程 (km)，复用 odometer
    var mileageKm: Double { odometer }

    enum CodingKeys: String, CodingKey {
        case carId = "car_id"; case date; case batteryLevel = "battery_level"
        case ratedRangeKm = "rated_range_km"; case idealRangeKm = "ideal_range_km"
        case odometer; case outsideTemp = "outside_temp"; case usableBatteryLevel = "usable_battery_level"
        case capacityDegradationPercent = "capacity_degradation_percent"
        case originalCapacityKwh = "original_capacity_kwh"
        case currentCapacityKwh = "current_capacity_kwh"
        case history
    }
}

struct UpdateItem: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String; let version: String
    enum CodingKeys: String, CodingKey { case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"; case version }
}

/// Sentry mode alert event (mock `sentry_events` array).
/// iOS API does not yet expose a real sentry endpoint; this models the mock payload
/// so SentryHistoryView can render a list/detail shell.
struct SentryEvent: Codable, Identifiable {
    let id: Int
    let startDate: String
    let endDate: String?
    let latitude: Double
    let longitude: Double
    let address: String?

    enum CodingKeys: String, CodingKey {
        case id
        case startDate = "start_date"
        case endDate = "end_date"
        case latitude, longitude, address
    }
}

// MARK: - Charge compat aliases
//
// Several consumers (ChargeListView / ChargeDetailView / CostView / TimelineView) were
// written against an older/Android-leaning field set that the iOS `Charge` model does
// not currently store: `chargeType`, `chargeEnergyUsed`, `fastChargerBrand`,
// `fastChargerType`. Rather than touch every call site, expose read-only compat props
// here so the consumers compile against the real `chargingType`/optional `cost`/
// `address` storage. When the iOS API gains the real fields, swap these out.
extension Charge {
    /// Alias for the stored `chargingType` (JSON `charging_type`).
    var chargeType: String { chargingType }

    /// Real `charge_energy_used` is not yet exposed by the iOS API/mock.
    /// Returns 0 to signal "unknown"; views render "—" when 0.
    var chargeEnergyUsed: Double { 0 }

    /// Fast-charger metadata is not yet modeled on iOS; nil-safe stubs for view compat.
    var fastChargerBrand: String? { nil }
    var fastChargerType: String? { nil }
}
