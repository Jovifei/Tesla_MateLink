package com.teslamatelink.data.repository

/**
 * Events emitted by [SentryStateRepository] when sentry state changes.
 */
sealed class SentryEvent {
    /** A sentry alert was detected. */
    data class AlertDetected(
        val count: Int,
        val shouldNotify: Boolean
    ) : SentryEvent()

    /** The sentry session ended (sentry mode turned off). */
    data object SessionEnded : SentryEvent()
}

/**
 * Tracks sentry mode state per car and emits [SentryEvent]s on transitions.
 */
interface SentryStateRepository {
    /**
     * Process a new status snapshot and return an event if the state changed.
     *
     * @param carId car identifier
     * @param sentryMode whether sentry mode is currently armed
     * @param isSentryAlerted whether a sentry alert is active right now
     * @param latitude current car latitude (null if unknown)
     * @param longitude current car longitude (null if unknown)
     * @param geofence current geofence name (null if none)
     * @return [SentryEvent] on state transition, null if no change
     */
    fun processStatus(
        carId: Int,
        sentryMode: Boolean,
        isSentryAlerted: Boolean,
        latitude: Double?,
        longitude: Double?,
        geofence: String?
    ): SentryEvent?
}
