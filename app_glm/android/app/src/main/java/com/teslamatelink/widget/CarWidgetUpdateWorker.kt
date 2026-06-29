package com.teslamatelink.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.teslamatelink.BuildConfig
import com.teslamatelink.data.repository.CarRepository
import com.teslamatelink.data.repository.StatusRepository
import kotlinx.coroutines.flow.firstOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker that fetches car status and updates all active home screen widgets.
 *
 * Uses two strategies:
 * 1. Self-rescheduling OneTimeWorkRequest: 1 min (debug) / 3 min (release)
 * 2. PeriodicWorkRequest (15 min) as a reliable fallback when the app is killed
 *
 * TODO: Add SettingsDataStore integration for image overrides and unit preferences.
 * TODO: Add GeocodingRepository for reverse geocoding location text.
 * TODO: Add SentryStateRepository for sentry event counts.
 */
@HiltWorker
class CarWidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val carRepository: CarRepository,
    private val statusRepository: StatusRepository,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CarWidgetUpdateWorker"
        const val WORK_NAME = "car_widget_update"
        const val PERIODIC_WORK_NAME = "car_widget_update_periodic"

        // Debug: 1 minute, Release: 3 minutes
        private val INTERVAL_MINUTES = if (BuildConfig.DEBUG) 1L else 3L

        /**
         * Enqueues an immediate (no-delay) update — used when a widget is first configured
         * so it shows real data right away instead of waiting for the next scheduled poll.
         */
        fun scheduleImmediateUpdate(context: Context) {
            val immediateRequest = OneTimeWorkRequestBuilder<CarWidgetUpdateWorker>()
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
            Log.d(TAG, "Scheduled immediate widget update")
        }

        fun scheduleWork(context: Context) {
            // Strategy 1: self-rescheduling OneTimeWorkRequest for frequent updates
            val oneTimeRequest = OneTimeWorkRequestBuilder<CarWidgetUpdateWorker>()
                .setInitialDelay(INTERVAL_MINUTES, TimeUnit.MINUTES)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )

            // Strategy 2: PeriodicWorkRequest as reliable backup (survives app death)
            val periodicRequest = PeriodicWorkRequestBuilder<CarWidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .addTag("$TAG-periodic")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )

            Log.d(TAG, "Scheduled widget update (${INTERVAL_MINUTES}min + 15min backup)")
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Log.d(TAG, "Cancelled widget update work")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting widget update")

        val manager = GlanceAppWidgetManager(appContext)
        val glanceIds = manager.getGlanceIds(CarWidget::class.java)

        if (glanceIds.isEmpty()) {
            Log.d(TAG, "No active widgets, skipping update")
            scheduleNextUpdate()
            return Result.success()
        }

        // TODO: Read image overrides from SettingsDataStore once integrated

        // Fetch all cars once
        val cars = try {
            carRepository.getCars().firstOrNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch cars", e)
            scheduleNextUpdate()
            return Result.retry()
        }

        for (glanceId in glanceIds) {
            try {
                val prefs = getAppWidgetState(appContext, PreferencesGlanceStateDefinition, glanceId)
                val carId = prefs[CarWidget.CAR_ID_KEY] ?: continue

                val car = carRepository.getCar(carId).firstOrNull() ?: continue
                if (car == null) continue

                val status = statusRepository.observeStatus(carId).firstOrNull()

                val displayData = if (status != null) {
                    CarWidgetDisplayData(
                        carId = car.carId,
                        carName = car.name,
                        exteriorColor = car.exteriorColor,
                        model = car.model,
                        trimBadging = car.trimBadging,
                        wheelType = car.wheelType,
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
                        acPhases = null,
                        sentryEventCount = 0,
                    )
                } else {
                    // Fallback: show car info with unavailable status
                    CarWidgetDisplayData(
                        carId = car.carId,
                        carName = car.name,
                        exteriorColor = car.exteriorColor,
                        model = car.model,
                        trimBadging = car.trimBadging,
                        wheelType = car.wheelType,
                        state = "unavailable",
                        stateSince = null,
                        isLocked = false,
                        sentryModeActive = false,
                        pluggedIn = false,
                        outsideTemp = null,
                        insideTemp = null,
                        isClimateOn = false,
                        batteryLevel = 0,
                        ratedBatteryRangeKm = null,
                        chargeLimitSoc = null,
                        isCharging = false,
                        isDcCharging = false,
                        chargerPower = null,
                        chargeEnergyAdded = null,
                        timeToFullCharge = null,
                        chargerVoltage = null,
                        chargerActualCurrent = null,
                        acPhases = null,
                        sentryEventCount = 0,
                    )
                }
                CarWidget().updateWidget(appContext, glanceId, displayData)
                Log.d(TAG, "Updated widget for car $carId (${car.name})")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget $glanceId", e)
            }
        }

        scheduleNextUpdate()
        return Result.success()
    }

    private fun scheduleNextUpdate() {
        scheduleWork(appContext)
    }
}
