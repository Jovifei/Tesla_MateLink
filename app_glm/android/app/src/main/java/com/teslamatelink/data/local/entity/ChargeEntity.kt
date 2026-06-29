package com.teslamatelink.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a charge session record.
 *
 * Mirrors the charge summary fields from the TeslaMate API
 * and stores them locally for offline access and fast queries.
 */
@Entity(
    tableName = "charges",
    indices = [
        Index(value = ["car_id"]),
        Index(value = ["car_id", "start_date"])
    ]
)
data class ChargeEntity(
    @PrimaryKey val id: Int,

    @ColumnInfo(name = "car_id") val carId: Int,

    @ColumnInfo(name = "start_date") val startDate: String?,

    @ColumnInfo(name = "end_date") val endDate: String?,

    @ColumnInfo(name = "charge_energy_added") val chargeEnergyAdded: Double?,

    @ColumnInfo(name = "charge_energy_used") val chargeEnergyUsed: Double?,

    @ColumnInfo(name = "start_battery_level") val startBatteryLevel: Int?,

    @ColumnInfo(name = "end_battery_level") val endBatteryLevel: Int?,

    @ColumnInfo(name = "cost") val cost: Double?,

    @ColumnInfo(name = "charge_type") val chargeType: String?,

    @ColumnInfo(name = "address") val address: String?
)

fun ChargeEntity.toSummary(): ChargeSummary = ChargeSummary(
    chargeId = id,
    carId = carId,
    startDate = startDate ?: "",
    endDate = endDate ?: "",
    durationMin = 0,
    address = address ?: "",
    latitude = 0.0,
    longitude = 0.0,
    energyAdded = chargeEnergyAdded ?: 0.0,
    energyUsed = chargeEnergyUsed,
    cost = cost,
    startBatteryLevel = startBatteryLevel ?: 0,
    endBatteryLevel = endBatteryLevel ?: 0,
    outsideTempAvg = null,
    odometer = 0.0
)
