package com.teslamatelink.ui.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.teslamatelink.ui.widget.BatteryWidget.Companion.WidgetData
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Worker that fetches the latest car status and updates the home screen widget.
 * Uses mock data for now; replace with real API call when available.
 */
class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            // Mock: fetch latest car data
            val data = fetchCarStatus()
            BatteryWidget.updateWidget(applicationContext, data = data)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun fetchCarStatus(): WidgetData {
        // Mock implementation - replace with real Tesla API call
        val prefs: SharedPreferences = applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        )

        val batteryLevel = prefs.getInt(KEY_LAST_BATTERY, 80).let { last ->
            // Simulate slight battery drain or charge change
            (last + Random.nextInt(-3, 4)).coerceIn(0, 100)
        }
        val rangeKm = (batteryLevel * 4.25).toFloat() // ~425 km at 100%
        val carState = when {
            batteryLevel > 0 && Random.nextFloat() < 0.7f -> "Parked"
            else -> "Driving"
        }

        // Persist for next update
        prefs.edit().apply {
            putInt(KEY_LAST_BATTERY, batteryLevel)
            apply()
        }

        return WidgetData(
            batteryLevel = batteryLevel,
            rangeKm = rangeKm,
            carState = carState
        )
    }

    companion object {
        private const val WORK_NAME = "battery_widget_update"
        private const val PREFS_NAME = "widget_update_prefs"
        private const val KEY_LAST_BATTERY = "last_battery_level"

        /**
         * Schedule periodic updates every 15 minutes.
         * Call this once (e.g., from Application.onCreate or widget onEnabled).
         */
        fun scheduleUpdate(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancel the periodic update.
         */
        fun cancelUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
