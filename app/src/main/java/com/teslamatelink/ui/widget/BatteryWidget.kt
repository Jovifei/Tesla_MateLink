package com.teslamatelink.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.teslamatelink.R

class BatteryWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId, null)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.scheduleUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateWorker.cancelUpdate(context)
    }

    companion object {
        private const val PREFS_NAME = "battery_widget_prefs"
        private const val KEY_BATTERY = "battery_level"
        private const val KEY_RANGE = "range_km"
        private const val KEY_STATE = "car_state"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager? = null,
            widgetId: Int? = null,
            data: WidgetData?
        ) {
            val manager = appWidgetManager ?: AppWidgetManager.getInstance(context)
            val ids = widgetId?.let { intArrayOf(it) }
                ?: manager.getAppWidgetIds(ComponentName(context, BatteryWidget::class.java))

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val batteryLevel = data?.batteryLevel ?: prefs.getInt(KEY_BATTERY, 80)
            val rangeKm = data?.rangeKm ?: prefs.getFloat(KEY_RANGE, 350f)
            val carState = data?.carState ?: prefs.getString(KEY_STATE, "Parked") ?: "Parked"

            prefs.edit().apply {
                putInt(KEY_BATTERY, batteryLevel)
                putFloat(KEY_RANGE, rangeKm)
                putString(KEY_STATE, carState)
                apply()
            }

            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            views.setTextViewText(R.id.widget_battery_text, "$batteryLevel%")
            views.setTextViewText(R.id.widget_range_text, "%.0f km".format(rangeKm))
            views.setTextViewText(R.id.widget_state_text, carState)

            // Set progress bar
            views.setInt(R.id.widget_battery_progress, "setMax", 100)
            views.setInt(R.id.widget_battery_progress, "progress", batteryLevel)

            // Open app on tap
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            ids.forEach { id ->
                manager.updateAppWidget(id, views)
            }
        }
    }

    data class WidgetData(
        val batteryLevel: Int,
        val rangeKm: Float,
        val carState: String
    )
}
