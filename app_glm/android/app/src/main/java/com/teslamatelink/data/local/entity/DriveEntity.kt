package com.teslamatelink.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a drive record.
 *
 * Mirrors the drive summary fields from the TeslaMate API
 * and stores them locally for offline access and fast queries.
 */
@Entity(
    tableName = "drives",
    indices = [
        Index(value = ["car_id"]),
        Index(value = ["car_id", "start_date"])
    ]
)
data class DriveEntity(
    @PrimaryKey val id: Int,

    @ColumnInfo(name = "car_id") val carId: Int,

    @ColumnInfo(name = "start_date") val startDate: String?,

    @ColumnInfo(name = "end_date") val endDate: String?,

    @ColumnInfo(name = "distance_km") val distanceKm: Double?,

    @ColumnInfo(name = "duration_min") val durationMin: Int?,

    @ColumnInfo(name = "efficiency") val efficiency: Double?,

    @ColumnInfo(name = "consumption_kwh") val consumptionKwh: Double?,

    @ColumnInfo(name = "start_address") val startAddress: String?,

    @ColumnInfo(name = "end_address") val endAddress: String?,

    @ColumnInfo(name = "outside_temp_avg") val outsideTempAvg: Double?,

    @ColumnInfo(name = "start_battery_level") val startBatteryLevel: Int?,

    @ColumnInfo(name = "end_battery_level") val endBatteryLevel: Int?
)

fun DriveEntity.toSummary(): DriveSummary = DriveSummary(
    driveId = id,
    carId = carId,
    startDate = startDate ?: "",
    endDate = endDate ?: "",
    durationMin = durationMin ?: 0,
    startAddress = startAddress ?: "",
    endAddress = endAddress ?: "",
    distance = distanceKm ?: 0.0,
    speedMax = 0,
    speedAvg = 0,
    powerMax = 0,
    powerMin = 0,
    startBatteryLevel = startBatteryLevel ?: 0,
    endBatteryLevel = endBatteryLevel ?: 0,
    outsideTempAvg = outsideTempAvg,
    insideTempAvg = null,
    energyConsumed = consumptionKwh,
    efficiency = efficiency
)
