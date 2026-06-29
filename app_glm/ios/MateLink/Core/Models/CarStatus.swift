import Foundation

enum CarState: String, Codable { case online, offline, asleep, charging, driving }

struct TirePressure: Codable { let frontLeft: Double; let frontRight: Double; let rearLeft: Double; let rearRight: Double
    enum CodingKeys: String, CodingKey { case frontLeft = "front_left"; case frontRight = "front_right"; case rearLeft = "rear_left"; case rearRight = "rear_right" }
}

struct CarStatus: Codable {
    let carId: Int; let state: CarState; let since: String; let healthy: Bool
    let odometer: Int; let batteryLevel: Int; let usableBatteryLevel: Int
    let chargeEnergyAdded: Double; let chargeLimitSoc: Int
    let idealBatteryRangeKm: Int; let estBatteryRangeKm: Int
    let chargerPower: Double; let chargerActualCurrent: Int; let chargerVoltage: Int
    let chargePhases: Int?
    let chargePortDoorOpen: Bool; let timeToFullCharge: Double
    let insideTemp: Double; let outsideTemp: Double; let isClimateOn: Bool
    let latitude: Double; let longitude: Double; let heading: Int; let speed: Int; let shiftState: String
    let locked: Bool; let sentryMode: Bool; let carVersion: String
    let tirePressure: TirePressure?

    enum CodingKeys: String, CodingKey {
        case carId = "car_id"; case state, since, healthy
        case odometer; case batteryLevel = "battery_level"; case usableBatteryLevel = "usable_battery_level"
        case chargeEnergyAdded = "charge_energy_added"; case chargeLimitSoc = "charge_limit_soc"
        case idealBatteryRangeKm = "ideal_battery_range_km"; case estBatteryRangeKm = "est_battery_range_km"
        case chargerPower = "charger_power"; case chargerActualCurrent = "charger_actual_current"; case chargerVoltage = "charger_voltage"
        case chargePhases = "charge_phases"
        case chargePortDoorOpen = "charge_port_door_open"; case timeToFullCharge = "time_to_full_charge"
        case insideTemp = "inside_temp"; case outsideTemp = "outside_temp"; case isClimateOn = "is_climate_on"
        case latitude, longitude, heading, speed; case shiftState = "shift_state"
        case locked; case sentryMode = "sentry_mode"; case carVersion = "car_version"
        case tirePressure = "tire_pressure"
    }
}

struct Drive: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String
    let distanceKm: Double; let durationMin: Int; let efficiency: Int; let consumptionKwh: Double
    let startAddress: String; let endAddress: String; let outsideTempAvg: Double
    let startBatteryLevel: Int; let endBatteryLevel: Int
    enum CodingKeys: String, CodingKey {
        case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"
        case distanceKm = "distance_km"; case durationMin = "duration_min"; case efficiency
        case consumptionKwh = "consumption_kWh"; case startAddress = "start_address"; case endAddress = "end_address"
        case outsideTempAvg = "outside_temp_avg"; case startBatteryLevel = "start_battery_level"; case endBatteryLevel = "end_battery_level"
    }
}

struct Charge: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String?
    let chargeEnergyAdded: Double; let chargeEnergyUsed: Double
    let startBatteryLevel: Int; let endBatteryLevel: Int?
    let startIdealRangeKm: Int; let endIdealRangeKm: Int?
    let cost: Double; let chargeType: String; let address: String
    let fastChargerBrand: String?
    let fastChargerType: String?
    enum CodingKeys: String, CodingKey {
        case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"
        case chargeEnergyAdded = "charge_energy_added"; case chargeEnergyUsed = "charge_energy_used"
        case startBatteryLevel = "start_battery_level"; case endBatteryLevel = "end_battery_level"
        case startIdealRangeKm = "start_ideal_range_km"; case endIdealRangeKm = "end_ideal_range_km"
        case cost; case chargeType = "charge_type"; case address
        case fastChargerBrand = "fast_charger_brand"; case fastChargerType = "fast_charger_type"
    }
}

struct BatteryHealth: Codable {
    let carId: Int; let originalCapacityKwh: Double; let currentCapacityKwh: Double
    let capacityDegradationPercent: Double; let originalRangeKm: Int; let currentRangeKm: Int
    let rangeLossPercent: Double; let mileageKm: Int
    let history: [BatteryHealthPoint]
    enum CodingKeys: String, CodingKey {
        case carId = "car_id"; case originalCapacityKwh = "original_capacity_kwh"; case currentCapacityKwh = "current_capacity_kwh"
        case capacityDegradationPercent = "capacity_degradation_percent"; case originalRangeKm = "original_range_km"; case currentRangeKm = "current_range_km"
        case rangeLossPercent = "range_loss_percent"; case mileageKm = "mileage_km"; case history
    }
}

struct BatteryHealthPoint: Codable { let date: String; let capacityKwh: Double; let mileageKm: Int
    enum CodingKeys: String, CodingKey { case date; case capacityKwh = "capacity_kwh"; case mileageKm = "mileage_km" }
}

struct UpdateItem: Codable, Identifiable {
    let id: Int; let carId: Int; let startDate: String; let endDate: String; let version: String
    enum CodingKeys: String, CodingKey { case id; case carId = "car_id"; case startDate = "start_date"; case endDate = "end_date"; case version }
}
