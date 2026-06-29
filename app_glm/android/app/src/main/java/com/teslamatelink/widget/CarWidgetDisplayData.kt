package com.teslamatelink.widget

import com.teslamatelink.data.api.model.CarRaw
import com.teslamatelink.data.api.model.CarStatus
import com.teslamatelink.domain.model.CarImageOverride

/**
 * Data class encapsulating all fields shown on the dashboard battery card.
 * This is the single source of truth for what the home screen widget displays.
 */
data class CarWidgetDisplayData(
    val carId: Int,
    val carName: String,
    val exteriorColor: String?,
    val model: String?,
    val trimBadging: String?,
    val wheelType: String?,
    // --- Status indicators ---
    val state: String?,
    val stateSince: String?,
    val isLocked: Boolean,
    val sentryModeActive: Boolean,
    val pluggedIn: Boolean,
    val outsideTemp: Double?,
    val insideTemp: Double?,
    val isClimateOn: Boolean,
    // --- Battery info ---
    val batteryLevel: Int,
    val ratedBatteryRangeKm: Double?,
    val chargeLimitSoc: Int?,
    // --- Charging state ---
    val isCharging: Boolean,
    val isDcCharging: Boolean,
    val chargerPower: Int?,
    val chargeEnergyAdded: Double?,
    val timeToFullCharge: Double?,
    val chargerVoltage: Int?,
    val chargerActualCurrent: Int?,
    val acPhases: Int?,
    val sentryEventCount: Int = 0,
    // --- Image override (from car image picker) ---
    val imageOverride: CarImageOverride? = null,
    // --- Location (pre-resolved for widget display) ---
    val locationText: String? = null,
    // --- Unit preference ---
    val isImperial: Boolean = false,
) {
    companion object {
        fun from(carRaw: CarRaw, status: CarStatus): CarWidgetDisplayData {
            return CarWidgetDisplayData(
                carId = carRaw.carId,
                carName = carRaw.name ?: carRaw.carDetails?.model?.let { "Model $it" } ?: "Tesla",
                exteriorColor = carRaw.carExterior?.exteriorColor,
                model = carRaw.carDetails?.model,
                trimBadging = carRaw.carDetails?.trimBadging,
                wheelType = carRaw.carExterior?.wheelType,
                state = status.state,
                stateSince = status.since,
                isLocked = status.locked ?: false,
                sentryModeActive = status.sentryMode ?: false,
                pluggedIn = status.chargePortDoorOpen ?: false,
                outsideTemp = status.outsideTemp,
                insideTemp = status.insideTemp,
                isClimateOn = status.isClimateOn ?: false,
                batteryLevel = status.batteryLevel ?: 0,
                ratedBatteryRangeKm = status.estBatteryRangeKm,
                chargeLimitSoc = status.chargeLimitSoc,
                isCharging = status.isCharging,
                isDcCharging = status.state.equals("dc_charging", ignoreCase = true),
                chargerPower = status.chargerPower?.toInt(),
                chargeEnergyAdded = status.chargeEnergyAdded,
                timeToFullCharge = status.timeToFullCharge,
                chargerVoltage = status.chargerVoltage,
                chargerActualCurrent = status.chargerActualCurrent,
                acPhases = null, // TODO: extract from status when available
                sentryEventCount = 0,  // Populated separately by worker
            )
        }
    }
}
