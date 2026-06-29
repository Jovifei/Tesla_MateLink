package com.teslamatelink.domain.model

import com.teslamatelink.data.api.model.CarRaw
import com.teslamatelink.data.api.model.ChargeRaw
import com.teslamatelink.data.api.model.DriveRaw
import com.teslamatelink.data.api.model.UpdateRaw
import com.teslamatelink.data.local.entity.ChargeEntity
import com.teslamatelink.data.local.entity.DriveEntity

// ──────────────────────────────────────────────
// Domain models (clean, flattened representations)
// ──────────────────────────────────────────────

data class Car(
    val carId: Int,
    val name: String,
    val vin: String? = null,
    val model: String? = null,
    val trimBadging: String? = null,
    val efficiency: Double? = null,
    val exteriorColor: String? = null,
    val spoilerType: String? = null,
    val wheelType: String? = null,
    val freeSupercharging: Boolean? = null,
    val totalCharges: Int? = null,
    val totalDrives: Int? = null,
    val totalUpdates: Int? = null
)

data class Drive(
    val id: Int,
    val carId: Int,
    val startDate: String? = null,
    val endDate: String? = null,
    val distanceKm: Double? = null,
    val durationMin: Int? = null,
    val efficiency: Double? = null,
    val consumptionKwh: Double? = null,
    val startAddress: String? = null,
    val endAddress: String? = null,
    val outsideTempAvg: Double? = null,
    val startBatteryLevel: Int? = null,
    val endBatteryLevel: Int? = null
)

data class Charge(
    val id: Int,
    val carId: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val chargeEnergyAdded: Double? = null,
    val chargeEnergyUsed: Double? = null,
    val startBatteryLevel: Int? = null,
    val endBatteryLevel: Int? = null,
    val startIdealRangeKm: Double? = null,
    val endIdealRangeKm: Double? = null,
    val cost: Double? = null,
    val chargeType: String? = null,
    val address: String? = null
)

data class UpdateItem(
    val id: Int,
    val carId: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val version: String? = null
)

// ──────────────────────────────────────────────
// Extension functions: Raw → Domain
// ──────────────────────────────────────────────

fun CarRaw.toDomain(): Car = Car(
    carId = carId,
    name = name ?: carDetails?.model?.let { "Model $it" } ?: "Tesla",
    vin = carDetails?.vin,
    model = carDetails?.model,
    trimBadging = carDetails?.trimBadging,
    efficiency = carDetails?.efficiency,
    exteriorColor = carExterior?.exteriorColor,
    spoilerType = carExterior?.spoilerType,
    wheelType = carExterior?.wheelType,
    freeSupercharging = carSettings?.freeSupercharging,
    totalCharges = teslamateStats?.totalCharges,
    totalDrives = teslamateStats?.totalDrives,
    totalUpdates = teslamateStats?.totalUpdates
)

fun DriveRaw.toDomain(): Drive = Drive(
    id = id,
    carId = carId ?: 0,
    startDate = startDate,
    endDate = endDate,
    distanceKm = distanceKm,
    durationMin = durationMin,
    efficiency = efficiency,
    consumptionKwh = consumptionKwh,
    startAddress = startAddress,
    endAddress = endAddress,
    outsideTempAvg = outsideTempAvg,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel
)

fun ChargeRaw.toDomain(): Charge = Charge(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    chargeEnergyAdded = chargeEnergyAdded,
    chargeEnergyUsed = chargeEnergyUsed,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    startIdealRangeKm = startIdealRangeKm,
    endIdealRangeKm = endIdealRangeKm,
    cost = cost,
    chargeType = chargeType,
    address = address
)

fun UpdateRaw.toDomain(): UpdateItem = UpdateItem(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    version = version
)

// ──────────────────────────────────────────────
// Extension functions: Raw → Entity (Room, DAO type)
// ──────────────────────────────────────────────

fun DriveRaw.toEntity(): DriveEntity = DriveEntity(
    id = id,
    carId = carId ?: 0,
    startDate = startDate,
    endDate = endDate,
    distanceKm = distanceKm,
    durationMin = durationMin,
    efficiency = efficiency,
    consumptionKwh = consumptionKwh,
    startAddress = startAddress,
    endAddress = endAddress,
    outsideTempAvg = outsideTempAvg,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel
)

fun ChargeRaw.toEntity(): ChargeEntity = ChargeEntity(
    id = id,
    carId = carId ?: 0,
    startDate = startDate,
    endDate = endDate,
    chargeEnergyAdded = chargeEnergyAdded,
    chargeEnergyUsed = chargeEnergyUsed,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    cost = cost,
    chargeType = chargeType,
    address = address
)

// ──────────────────────────────────────────────
// Extension functions: Entity → Domain (cache fallback)
// ──────────────────────────────────────────────

fun DriveEntity.toDomain(): Drive = Drive(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    distanceKm = distanceKm,
    durationMin = durationMin,
    efficiency = efficiency,
    consumptionKwh = consumptionKwh,
    startAddress = startAddress,
    endAddress = endAddress,
    outsideTempAvg = outsideTempAvg,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel
)

fun ChargeEntity.toDomain(): Charge = Charge(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    chargeEnergyAdded = chargeEnergyAdded,
    chargeEnergyUsed = chargeEnergyUsed,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    // ChargeEntity does not store idealRange; null on cache fallback
    startIdealRangeKm = null,
    endIdealRangeKm = null,
    cost = cost,
    chargeType = chargeType,
    address = address
)
