package com.teslamatelink.data.api.model

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Whether the car is actively DC-charging.
 *
 * Only `isCharging && chargerPhases == 0` confirms DC.
 * After completion, phases is null regardless of charge type, so this
 * returns false for completed sessions.
 */
val CarStatus.isDcCharging: Boolean
    get() = isCharging && (chargerActualCurrent != null && chargerActualCurrent > 0) &&
        (chargerPower != null && chargerPower > 0)

/**
 * Whether the charge port door is open (cable plugged in).
 */
val CarStatus.pluggedIn: Boolean
    get() = chargePortDoorOpen ?: false

/**
 * Whether the car finished charging but is still plugged in.
 */
val CarStatus.isChargeCompletePluggedIn: Boolean
    get() = pluggedIn && !isCharging && (batteryLevel ?: 0) >= (chargeLimitSoc ?: 100)

/**
 * Epoch milliseconds for when the current state began.
 * Parses the ISO-8601 [since] field; returns 0 if unavailable.
 */
val CarStatus.stateSinceEpochMs: Long
    get() {
        val sinceStr = since ?: return 0L
        return try {
            Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(sinceStr))
                .toEpochMilli()
        } catch (_: Exception) {
            try {
                Instant.parse(sinceStr).toEpochMilli()
            } catch (_: Exception) {
                0L
            }
        }
    }

/**
 * Whether a sentry alert is currently active.
 */
val CarStatus.isSentryAlerted: Boolean
    get() = sentryMode == true && state.equals("alerted", ignoreCase = true)

/**
 * Current geofence name, if any.
 */
val CarStatus.geofence: String?
    get() = null // Not available in current API; placeholder for future extension
