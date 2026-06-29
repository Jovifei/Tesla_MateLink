package com.teslamatelink.data.api.model

import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────
// Cars
// ──────────────────────────────────────────────

data class CarsResponse(
    @SerializedName("data") val data: CarsData? = null
)

data class CarsData(
    @SerializedName("cars") val cars: List<CarRaw>? = null
)

data class CarResponse(
    @SerializedName("data") val data: CarData? = null
)

data class CarData(
    @SerializedName("cars") val cars: List<CarRaw>? = null
)

data class CarRaw(
    @SerializedName("car_id") val carId: Int,
    @SerializedName("name") val name: String? = null,
    @SerializedName("car_details") val carDetails: CarDetails? = null,
    @SerializedName("car_exterior") val carExterior: CarExterior? = null,
    @SerializedName("car_settings") val carSettings: CarSettings? = null,
    @SerializedName("teslamate_details") val teslamateDetails: CarTeslaMateDetails? = null,
    @SerializedName("teslamate_stats") val teslamateStats: CarTeslaMateStats? = null
)

data class CarDetails(
    @SerializedName("eid") val eid: Long? = null,
    @SerializedName("vid") val vid: Long? = null,
    @SerializedName("vin") val vin: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("trim_badging") val trimBadging: String? = null,
    @SerializedName("efficiency") val efficiency: Double? = null
)

data class CarExterior(
    @SerializedName("exterior_color") val exteriorColor: String? = null,
    @SerializedName("spoiler_type") val spoilerType: String? = null,
    @SerializedName("wheel_type") val wheelType: String? = null
)

data class CarSettings(
    @SerializedName("suspend_min") val suspendMin: Int? = null,
    @SerializedName("suspend_after_idle_min") val suspendAfterIdleMin: Int? = null,
    @SerializedName("req_not_unlocked") val reqNotUnlocked: Boolean? = null,
    @SerializedName("free_supercharging") val freeSupercharging: Boolean? = null,
    @SerializedName("use_streaming_api") val useStreamingApi: Boolean? = null
)

data class CarTeslaMateDetails(
    @SerializedName("inserted_at") val insertedAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class CarTeslaMateStats(
    @SerializedName("total_charges") val totalCharges: Int? = null,
    @SerializedName("total_drives") val totalDrives: Int? = null,
    @SerializedName("total_updates") val totalUpdates: Int? = null
)

// ──────────────────────────────────────────────
// Status
// ──────────────────────────────────────────────

data class CarStatusResponse(
    @SerializedName("data") val data: CarStatusData? = null
)

data class CarStatusData(
    @SerializedName("status") val status: CarStatus? = null
)

data class CarStatus(
    @SerializedName("car_id") val carId: Int? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("since") val since: String? = null,
    @SerializedName("healthy") val healthy: Boolean? = null,
    @SerializedName("odometer") val odometer: Double? = null,
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("usable_battery_level") val usableBatteryLevel: Int? = null,
    @SerializedName("charge_energy_added") val chargeEnergyAdded: Double? = null,
    @SerializedName("charge_limit_soc") val chargeLimitSoc: Int? = null,
    @SerializedName("ideal_battery_range_km") val idealBatteryRangeKm: Double? = null,
    @SerializedName("est_battery_range_km") val estBatteryRangeKm: Double? = null,
    @SerializedName("charger_power") val chargerPower: Double? = null,
    @SerializedName("charger_actual_current") val chargerActualCurrent: Int? = null,
    @SerializedName("charger_voltage") val chargerVoltage: Int? = null,
    @SerializedName("charge_port_door_open") val chargePortDoorOpen: Boolean? = null,
    @SerializedName("time_to_full_charge") val timeToFullCharge: Double? = null,
    @SerializedName("inside_temp") val insideTemp: Double? = null,
    @SerializedName("outside_temp") val outsideTemp: Double? = null,
    @SerializedName("is_climate_on") val isClimateOn: Boolean? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("heading") val heading: Int? = null,
    @SerializedName("speed") val speed: Int? = null,
    @SerializedName("shift_state") val shiftState: String? = null,
    @SerializedName("locked") val locked: Boolean? = null,
    @SerializedName("sentry_mode") val sentryMode: Boolean? = null,
    @SerializedName("car_version") val carVersion: String? = null,
    @SerializedName("tire_pressure") val tirePressure: TirePressure? = null
) {
    val isCharging: Boolean get() = state.equals("charging", ignoreCase = true)
    val isOnline: Boolean get() = state.equals("online", ignoreCase = true)
    val isAsleep: Boolean get() = state.equals("asleep", ignoreCase = true)
    val isSuspended: Boolean get() = state.equals("suspended", ignoreCase = true)
}

data class TirePressure(
    @SerializedName("front_left") val frontLeft: Double? = null,
    @SerializedName("front_right") val frontRight: Double? = null,
    @SerializedName("rear_left") val rearLeft: Double? = null,
    @SerializedName("rear_right") val rearRight: Double? = null
)

// ──────────────────────────────────────────────
// Drives
// ──────────────────────────────────────────────

data class DrivesResponse(
    @SerializedName("data") val data: DrivesData? = null
)

data class DrivesData(
    @SerializedName("drives") val drives: List<DriveRaw>? = null
)

data class DriveRaw(
    @SerializedName("id") val id: Int,
    @SerializedName("car_id") val carId: Int? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("distance_km") val distanceKm: Double? = null,
    @SerializedName("duration_min") val durationMin: Int? = null,
    @SerializedName("efficiency") val efficiency: Double? = null,
    @SerializedName("consumption_kWh") val consumptionKwh: Double? = null,
    @SerializedName("start_address") val startAddress: String? = null,
    @SerializedName("end_address") val endAddress: String? = null,
    @SerializedName("outside_temp_avg") val outsideTempAvg: Double? = null,
    @SerializedName("start_battery_level") val startBatteryLevel: Int? = null,
    @SerializedName("end_battery_level") val endBatteryLevel: Int? = null
)

data class DriveDetailResponse(
    @SerializedName("data") val data: DriveDetailData? = null
)

data class DriveDetailData(
    @SerializedName("drive") val drive: DriveRaw? = null
)

// ──────────────────────────────────────────────
// Charges
// ──────────────────────────────────────────────

data class ChargesResponse(
    @SerializedName("data") val data: ChargesData? = null
)

data class ChargesData(
    @SerializedName("charges") val charges: List<ChargeRaw>? = null
)

data class ChargeRaw(
    @SerializedName("id") val id: Int,
    @SerializedName("car_id") val carId: Int? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("charge_energy_added") val chargeEnergyAdded: Double? = null,
    @SerializedName("charge_energy_used") val chargeEnergyUsed: Double? = null,
    @SerializedName("start_battery_level") val startBatteryLevel: Int? = null,
    @SerializedName("end_battery_level") val endBatteryLevel: Int? = null,
    @SerializedName("start_ideal_range_km") val startIdealRangeKm: Double? = null,
    @SerializedName("end_ideal_range_km") val endIdealRangeKm: Double? = null,
    @SerializedName("cost") val cost: Double? = null,
    @SerializedName("charge_type") val chargeType: String? = null,
    @SerializedName("address") val address: String? = null
)

data class CurrentChargeResponse(
    @SerializedName("data") val data: CurrentChargeData? = null,
    @SerializedName("error") val error: String? = null
)

data class CurrentChargeData(
    @SerializedName("charge") val charge: ChargeRaw? = null
)

data class ChargeDetailResponse(
    @SerializedName("data") val data: ChargeDetailData? = null
)

data class ChargeDetailData(
    @SerializedName("charge") val charge: ChargeRaw? = null
)

// ──────────────────────────────────────────────
// Battery Health
// ──────────────────────────────────────────────

data class BatteryHealthResponse(
    @SerializedName("data") val data: BatteryHealthData? = null
)

data class BatteryHealthData(
    @SerializedName("battery_health") val batteryHealth: BatteryHealth? = null
)

data class BatteryHealth(
    @SerializedName("car_id") val carId: Int? = null,
    @SerializedName("original_capacity_kwh") val originalCapacityKwh: Double? = null,
    @SerializedName("current_capacity_kwh") val currentCapacityKwh: Double? = null,
    @SerializedName("capacity_degradation_percent") val capacityDegradationPercent: Double? = null,
    @SerializedName("original_range_km") val originalRangeKm: Double? = null,
    @SerializedName("current_range_km") val currentRangeKm: Double? = null,
    @SerializedName("range_loss_percent") val rangeLossPercent: Double? = null,
    @SerializedName("mileage_km") val mileageKm: Double? = null,
    @SerializedName("history") val history: List<BatteryHealthPoint>? = null
)

data class BatteryHealthPoint(
    @SerializedName("date") val date: String? = null,
    @SerializedName("capacity_kwh") val capacityKwh: Double? = null,
    @SerializedName("mileage_km") val mileageKm: Double? = null
)

data class BatteryHistoryResponse(
    @SerializedName("data") val data: BatteryHistoryData? = null
)

data class BatteryHistoryData(
    @SerializedName("history") val history: List<BatteryHealthPoint>? = null
)

// ──────────────────────────────────────────────
// Updates
// ──────────────────────────────────────────────

data class UpdatesResponse(
    @SerializedName("data") val data: UpdatesData? = null
)

data class UpdatesData(
    @SerializedName("updates") val updates: List<UpdateRaw>? = null
)

data class UpdateRaw(
    @SerializedName("id") val id: Int,
    @SerializedName("car_id") val carId: Int? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("version") val version: String? = null
)

data class UpdateDetailResponse(
    @SerializedName("data") val data: UpdateDetailData? = null
)

data class UpdateDetailData(
    @SerializedName("update") val update: UpdateRaw? = null
)

// ──────────────────────────────────────────────
// Commands, Health, Readiness
// ──────────────────────────────────────────────

data class CommandResponse(
    @SerializedName("data") val data: CommandResult? = null,
    @SerializedName("error") val error: String? = null
)

data class CommandResult(
    @SerializedName("result") val result: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class PingResponse(
    @SerializedName("ping") val ping: String? = null
)

data class HealthzResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("version") val version: String? = null
)

data class ReadyzResponse(
    @SerializedName("status") val status: String? = null
)

