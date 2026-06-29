package com.teslamatelink.domain

import com.teslamatelink.data.local.entity.ChargeEntity
import com.teslamatelink.data.local.entity.DriveEntity
import com.teslamatelink.data.local.entity.toSummary
import com.teslamatelink.domain.model.Trip
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Assembles a Trip domain object from its constituent drive and charge legs.
 *
 * Shared by TripDetector (for newly detected trips) and TripRepository (for
 * rebuilding persisted SavedTrips from their leg references). The aggregator
 * itself enforces no detection rules -- callers decide whether a given leg set
 * is eligible (the detector applies the 300 km / 2-drive / 1-charge thresholds).
 */
object TripAggregator {

    /**
     * Build a Trip from chronologically-ordered legs.
     * Returns null if [drives] is empty (a Trip's start/end metadata comes from the first/last drive).
     * [name] is carried through to [Trip.name] so saved trips' custom names reach the UI.
     */
    fun buildTrip(
        drives: List<DriveEntity>,
        charges: List<ChargeEntity>,
        name: String? = null
    ): Trip? {
        if (drives.isEmpty()) return null

        val totalDistance = drives.sumOf { it.distanceKm ?: 0.0 }
        val totalDrivingMin = drives.sumOf { it.durationMin ?: 0 }
        val firstStart = parseIsoDateTime(drives.first().startDate)
        val lastEnd = parseIsoDateTime(drives.last().endDate)
        val totalMin = if (firstStart != null && lastEnd != null) {
            ChronoUnit.MINUTES.between(firstStart, lastEnd).toInt()
        } else totalDrivingMin
        val totalEnergyConsumed = drives.mapNotNull { it.consumptionKwh }.sum()
        val totalEnergyCharged = charges.sumOf { it.chargeEnergyAdded ?: 0.0 }
        val costs = charges.mapNotNull { it.cost }
        val totalCost = if (costs.isNotEmpty()) costs.sum() else null
        // maxSpeed: derive from available fields — DriveEntity doesn't have speedMax directly
        // Use a default or calculate from distance/duration when possible
        val avgSpeedKmh = if (totalDistance > 0 && totalDrivingMin > 0) {
            (totalDistance / (totalDrivingMin / 60.0)).toInt()
        } else 0
        val avgEfficiency = if (totalDistance > 0) {
            (totalEnergyConsumed * 1000.0) / totalDistance
        } else null

        return Trip(
            drives = drives.map { it.toSummary() },
            charges = charges.map { it.toSummary() },
            totalDistance = totalDistance,
            totalDrivingDurationMin = totalDrivingMin,
            totalDurationMin = totalMin,
            totalEnergyConsumed = totalEnergyConsumed,
            totalEnergyCharged = totalEnergyCharged,
            totalChargeCost = totalCost,
            avgEfficiency = avgEfficiency,
            maxSpeed = avgSpeedKmh,
            startAddress = drives.first().startAddress,
            endAddress = drives.last().endAddress,
            startDate = drives.first().startDate,
            endDate = drives.last().endDate,
            startBatteryLevel = drives.first().startBatteryLevel,
            endBatteryLevel = drives.last().endBatteryLevel,
            name = name
        )
    }
}
