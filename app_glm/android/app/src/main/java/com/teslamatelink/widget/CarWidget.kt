package com.teslamatelink.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.currentState
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.teslamatelink.MainActivity
import com.teslamatelink.ui.theme.CarColorPalette
import com.teslamatelink.ui.theme.CarColorPalettes
import java.io.IOException
import kotlin.math.roundToInt

// TODO: Implement GlowBitmapRenderer — needed for widget background glow effects.
// Currently used in buildBackgroundBitmap() for charging glow layers.
// Once implemented, place in com.teslamatelink.ui.util.GlowBitmapRenderer.
// For now, charging glow effects in the widget are disabled.

// Compose Color constants matching StatusSuccess / StatusError
private val STATUS_SUCCESS = Color(0xE64CAF50)
private val STATUS_ERROR = Color(0xFFF44336)
private val STATUS_ERROR_DIM = Color(0xB2F44336)

/** Fixed reference width for progress bar bitmap (px). */
private const val PROGRESS_BAR_W = 1000
/** Fixed reference height for progress bar bitmap (px). */
private const val PROGRESS_BAR_H = 16
/** Fallback bitmap width when no car image is available. */
private const val FALLBACK_BG_W = 720
/** Fallback bitmap height when no car image is available. */
private const val FALLBACK_BG_H = 405

/**
 * Home screen widget displaying real-time battery info for a configured car.
 *
 * All display data is persisted in Glance preferences so that [provideGlance]
 * can render real content without needing to inject a repository.
 * [updateWidget] writes every field from [CarWidgetDisplayData] into preferences
 * and then calls [update] to trigger a redraw.
 *
 * The background bitmap (car image + glow + scrim) is generated at the car
 * image's native resolution and displayed with [ContentScale.Crop], decoupling
 * it from the launcher-reported widget size.  All functional UI (status bar
 * icons, temperatures, progress bar, text) is rendered as Glance composables
 * that are positioned by the layout engine using actual rendered dimensions.
 * This avoids aspect-ratio distortion on third-party launchers (e.g. Nova)
 * that report widget sizes inconsistently.
 */
class CarWidget : GlanceAppWidget() {

    companion object {
        // Glance preference keys — one per CarWidgetDisplayData field
        val CAR_ID_KEY = intPreferencesKey("car_id")
        val HAS_DATA_KEY = booleanPreferencesKey("has_data")
        val CAR_NAME_KEY = stringPreferencesKey("car_name")
        val EXTERIOR_COLOR_KEY = stringPreferencesKey("exterior_color")
        val MODEL_KEY = stringPreferencesKey("model")
        val TRIM_BADGING_KEY = stringPreferencesKey("trim_badging")
        val WHEEL_TYPE_KEY = stringPreferencesKey("wheel_type")
        val STATE_KEY = stringPreferencesKey("state")
        val IS_LOCKED_KEY = booleanPreferencesKey("is_locked")
        val SENTRY_MODE_KEY = booleanPreferencesKey("sentry_mode")
        val PLUGGED_IN_KEY = booleanPreferencesKey("plugged_in")
        val OUTSIDE_TEMP_KEY = floatPreferencesKey("outside_temp")   // Float.NaN if null
        val INSIDE_TEMP_KEY = floatPreferencesKey("inside_temp")     // Float.NaN if null
        val IS_CLIMATE_ON_KEY = booleanPreferencesKey("is_climate_on")
        val BATTERY_LEVEL_KEY = intPreferencesKey("battery_level")
        val RATED_RANGE_KEY = floatPreferencesKey("rated_range_km")  // -1 if null
        val CHARGE_LIMIT_KEY = intPreferencesKey("charge_limit_soc") // -1 if null
        val IS_CHARGING_KEY = booleanPreferencesKey("is_charging")
        val IS_DC_CHARGING_KEY = booleanPreferencesKey("is_dc_charging")
        val CHARGER_POWER_KEY = intPreferencesKey("charger_power")           // -1 if null
        val CHARGE_ENERGY_ADDED_KEY = floatPreferencesKey("charge_energy_added") // -1 if null
        val TIME_TO_FULL_KEY = floatPreferencesKey("time_to_full")           // -1 if null
        val CHARGER_VOLTAGE_KEY = intPreferencesKey("charger_voltage")       // -1 if null
        val CHARGER_CURRENT_KEY = intPreferencesKey("charger_current")       // -1 if null
        val AC_PHASES_KEY = intPreferencesKey("ac_phases")                   // -1 if null
        val SENTRY_EVENT_COUNT_KEY = intPreferencesKey("sentry_event_count")   // 0 = none
        val IMAGE_OVERRIDE_VARIANT_KEY = stringPreferencesKey("image_override_variant")
        val IMAGE_OVERRIDE_WHEEL_KEY = stringPreferencesKey("image_override_wheel")
        val LOCATION_TEXT_KEY = stringPreferencesKey("location_text")
        val IS_IMPERIAL_KEY = booleanPreferencesKey("is_imperial")
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isOnLockScreen = isWidgetOnLockScreen(context, id)

        provideContent { WidgetContent(isOnLockScreen) }
    }

    @androidx.compose.runtime.Composable
    internal fun WidgetContent(isOnLockScreen: Boolean) {
        val prefs = currentState<Preferences>()
        val carId = prefs[CAR_ID_KEY]
        val hasData = prefs[HAS_DATA_KEY] ?: false
        val ctx0 = LocalContext.current

        GlanceTheme {
                val cornerMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
                } else {
                    GlanceModifier
                }
                val openAppIntent = Intent(ctx0, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .apply { if (carId != null) putExtra("EXTRA_CAR_ID", carId) }
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .then(cornerMod)
                        .clickable(actionStartActivity(openAppIntent))
                ) {
                    when {
                        carId == null -> {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .background(ColorProvider(Color(0xFF1E2530)))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LocalContext.current.getString(com.teslamatelink.R.string.widget_error_configure),
                                    style = TextStyle(color = ColorProvider(Color.White))
                                )
                            }
                        }

                        !hasData -> {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .background(ColorProvider(Color(0xFF1E2530)))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LocalContext.current.getString(com.teslamatelink.R.string.widget_loading),
                                    style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)))
                                )
                            }
                        }

                        else -> {
                            val ctx = LocalContext.current
                            val size = LocalSize.current

                            val batteryLevel = prefs[BATTERY_LEVEL_KEY] ?: 0
                            val isCharging = prefs[IS_CHARGING_KEY] ?: false
                            val isDcCharging = prefs[IS_DC_CHARGING_KEY] ?: false
                            val carName = prefs[CAR_NAME_KEY] ?: ""
                            val ratedRange = prefs[RATED_RANGE_KEY]?.takeIf { it >= 0f }
                            val isImperial = prefs[IS_IMPERIAL_KEY] ?: false
                            val chargeLimit = prefs[CHARGE_LIMIT_KEY]?.takeIf { it >= 0 }
                            val locationText = prefs[LOCATION_TEXT_KEY]
                            val chargeEnergyAdded = prefs[CHARGE_ENERGY_ADDED_KEY]?.takeIf { it >= 0f }
                            val timeToFull = prefs[TIME_TO_FULL_KEY]?.takeIf { it >= 0f }
                            val chargerPower = prefs[CHARGER_POWER_KEY]?.takeIf { it >= 0 }
                            val chargerVoltage = prefs[CHARGER_VOLTAGE_KEY]?.takeIf { it >= 0 }
                            val chargerCurrent = prefs[CHARGER_CURRENT_KEY]?.takeIf { it >= 0 }
                            val acPhases = prefs[AC_PHASES_KEY]?.takeIf { it >= 0 }

                            val isCompact = size.height.value < COMPACT_HEIGHT_DP
                            val layout = computeWidgetLayout(size.width.value, size.height.value, isCharging)

                            val useHomeLayout = !isOnLockScreen && layout.showLocation

                            // -- Status bar data --
                            val exteriorColor = prefs[EXTERIOR_COLOR_KEY]
                            val state = prefs[STATE_KEY]
                            val isLocked = prefs[IS_LOCKED_KEY] ?: false
                            val sentryMode = prefs[SENTRY_MODE_KEY] ?: false
                            val sentryEventCount = prefs[SENTRY_EVENT_COUNT_KEY] ?: 0
                            val pluggedIn = prefs[PLUGGED_IN_KEY] ?: false
                            val isClimateOn = prefs[IS_CLIMATE_ON_KEY] ?: false
                            val outsideTemp = if (layout.showTemperatures) prefs[OUTSIDE_TEMP_KEY]?.takeIf { !it.isNaN() } else null
                            val insideTemp = if (layout.showTemperatures) prefs[INSIDE_TEMP_KEY]?.takeIf { !it.isNaN() } else null

                            val palette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme = true)

                            val stateLower = state?.lowercase()
                            val isAwake = stateLower in listOf("online", "charging", "driving", "updating")
                            val isAsleep = stateLower in listOf("asleep", "suspended")
                            val isDriving = stateLower == "driving"
                            val stateIsCharging = stateLower == "charging"

                            // -- Background bitmap (car + scrim only; glow disabled until GlowBitmapRenderer is implemented) --
                            val bgKey = WidgetBackgroundCache.Key(
                                exteriorColor = exteriorColor,
                                model = prefs[MODEL_KEY],
                                trimBadging = prefs[TRIM_BADGING_KEY],
                                wheelType = prefs[WHEEL_TYPE_KEY],
                                overrideVariant = prefs[IMAGE_OVERRIDE_VARIANT_KEY],
                                overrideWheel = prefs[IMAGE_OVERRIDE_WHEEL_KEY],
                                isCharging = isCharging,
                                isDcCharging = isDcCharging
                            )
                            val bgBitmap = WidgetBackgroundCache.getOrCreate(ctx, bgKey) {
                                buildBackgroundBitmap(ctx, prefs)
                            }
                            Image(
                                provider = ImageProvider(bgBitmap),
                                contentDescription = null,
                                modifier = GlanceModifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // -- Status bar + text overlays --
                            val iconSize = if (isCompact) 14.dp else 16.dp
                            val iconGap = if (isCompact) 4.dp else 6.dp
                            val stateColor = if (isAwake) STATUS_SUCCESS
                                             else palette.onSurfaceVariant.copy(alpha = 0.8f)
                            val lockColor = if (isLocked) palette.onSurfaceVariant.copy(alpha = 0.85f)
                                            else STATUS_ERROR_DIM
                            val variantColor = palette.onSurfaceVariant.copy(alpha = 0.85f)

                            Column(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = if (isCompact) 10.dp else 14.dp,
                                        vertical = if (isCompact) 5.dp else 8.dp
                                    )
                            ) {
                                // STATUS BAR ROW
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Charging power + state icon
                                    if (stateIsCharging && chargerPower != null && chargerPower > 0) {
                                        Text(
                                            text = "${chargerPower} kW",
                                            style = TextStyle(
                                                color = ColorProvider(stateColor),
                                                fontSize = if (isCompact) 9.sp else 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = GlanceModifier.width(3.dp))
                                    }
                                    val stateIconRes = when {
                                        stateIsCharging -> com.teslamatelink.R.drawable.ic_bolt
                                        isAsleep -> com.teslamatelink.R.drawable.ic_bedtime
                                        isDriving -> com.teslamatelink.R.drawable.ic_steering_wheel
                                        else -> com.teslamatelink.R.drawable.ic_power_settings_new
                                    }
                                    Image(
                                        provider = ImageProvider(stateIconRes),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(iconSize),
                                        colorFilter = ColorFilter.tint(ColorProvider(stateColor))
                                    )
                                    Spacer(modifier = GlanceModifier.width(iconGap))

                                    // Lock icon
                                    val lockIconRes = if (isLocked) com.teslamatelink.R.drawable.ic_lock
                                                      else com.teslamatelink.R.drawable.ic_lock_open
                                    Image(
                                        provider = ImageProvider(lockIconRes),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(iconSize),
                                        colorFilter = ColorFilter.tint(ColorProvider(lockColor))
                                    )

                                    // Sentry dot + event count — tapping opens sentry history
                                    if (sentryMode) {
                                        Spacer(modifier = GlanceModifier.width(iconGap))
                                        val ringSize = if (isCompact) 11.dp else 14.dp
                                        val dotSize = if (isCompact) 5.dp else 7.dp
                                        val sentryIntent = Intent(ctx, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            .putExtra("EXTRA_CAR_ID", carId)
                                            .putExtra("EXTRA_NAVIGATE_TO", "sentry_history")
                                            .apply {
                                                if (exteriorColor != null) putExtra("EXTRA_EXTERIOR_COLOR", exteriorColor)
                                            }
                                        Row(
                                            modifier = GlanceModifier.clickable(
                                                actionStartActivity(sentryIntent)
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Grey ring + red dot (Tesla-style sentry indicator)
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(ringSize)
                                                    .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
                                                    .cornerRadius(7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = GlanceModifier
                                                        .size(dotSize)
                                                        .background(ColorProvider(STATUS_ERROR))
                                                        .cornerRadius(4.dp)
                                                ) {}
                                            }
                                            if (sentryEventCount > 0) {
                                                Spacer(modifier = GlanceModifier.width(2.dp))
                                                Text(
                                                    text = "$sentryEventCount",
                                                    style = TextStyle(
                                                        color = ColorProvider(STATUS_ERROR),
                                                        fontSize = if (isCompact) 9.sp else 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Plug icon (plugged in but not actively charging)
                                    if (pluggedIn && !stateIsCharging) {
                                        Spacer(modifier = GlanceModifier.width(iconGap))
                                        Image(
                                            provider = ImageProvider(com.teslamatelink.R.drawable.ic_plug),
                                            contentDescription = null,
                                            modifier = GlanceModifier.size(iconSize),
                                            colorFilter = ColorFilter.tint(ColorProvider(variantColor))
                                        )
                                    }

                                    Spacer(modifier = GlanceModifier.defaultWeight())

                                    // Temperatures (right-aligned)
                                    val tempParts = buildList<String> {
                                        if (outsideTemp != null) add("Ext: %.0f°".format(outsideTemp))
                                        if (insideTemp != null) add("Int: %.0f°".format(insideTemp))
                                    }
                                    if (tempParts.isNotEmpty()) {
                                        Text(
                                            text = tempParts.joinToString("  "),
                                            style = TextStyle(
                                                color = ColorProvider(
                                                    if (isClimateOn) STATUS_SUCCESS else variantColor
                                                ),
                                                fontSize = if (isCompact) 9.sp else 11.sp,
                                                fontWeight = if (isClimateOn) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = GlanceModifier.defaultWeight())

                                // Car name
                                if (carName.isNotEmpty()) {
                                    Text(
                                        text = carName,
                                        style = TextStyle(
                                            color = ColorProvider(Color.White.copy(alpha = 0.65f)),
                                            fontSize = if (isCompact) 9.sp else 10.sp
                                        )
                                    )
                                }

                                // Battery % + AC/DC badge | range + charge limit
                                val batteryColor = when {
                                    batteryLevel < 20 -> Color(0xFFEF5350)
                                    batteryLevel < 40 -> Color(0xFFFF9800)
                                    else -> Color.White
                                }
                                val batteryFontSize = when {
                                    isCompact && !layout.showTemperatures -> 16.sp
                                    isCompact -> 20.sp
                                    else -> 24.sp
                                }

                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$batteryLevel%",
                                        style = TextStyle(
                                            color = ColorProvider(batteryColor),
                                            fontSize = batteryFontSize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (isCharging) {
                                        Text(
                                            text = if (isDcCharging) "  DC" else "  AC",
                                            style = TextStyle(
                                                color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                                                fontSize = if (isCompact) 10.sp else 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    if (useHomeLayout && !isCharging && !locationText.isNullOrBlank()) {
                                        Spacer(modifier = GlanceModifier.defaultWeight())
                                        Text(
                                            text = locationText,
                                            style = TextStyle(
                                                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1
                                        )
                                    } else if (!useHomeLayout && (layout.showMileage || layout.showChargeLimit)) {
                                        Spacer(modifier = GlanceModifier.defaultWeight())
                                        val rightParts = buildList<String> {
                                            if (layout.showMileage && ratedRange != null) {
                                                val rangeUnit = if (isImperial) "mi" else "km"
                                                add("${ratedRange.roundToInt()} $rangeUnit")
                                            }
                                            if (layout.showChargeLimit && chargeLimit != null)
                                                add("Limit: $chargeLimit%")
                                        }.joinToString("  ")
                                        if (rightParts.isNotEmpty()) {
                                            Text(
                                                text = rightParts,
                                                style = TextStyle(
                                                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                                                    fontSize = if (isCompact) 10.sp else 12.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Charging details text
                                val chargingText = if (isCharging) {
                                    val kwhTimePart = buildString {
                                        if (chargeEnergyAdded != null)
                                            append("+%.1f kWh".format(chargeEnergyAdded))
                                        if (timeToFull != null) {
                                            val h = timeToFull.toInt()
                                            val m = ((timeToFull - h) * 60).roundToInt()
                                            append(if (h > 0) " ${h}h ${m}m" else " ${m}m")
                                        }
                                    }.trim()
                                    if (layout.showVoltageCurrentPhases) {
                                        val voltPart = buildString {
                                            if (chargerVoltage != null) append("${chargerVoltage}V")
                                            if (chargerCurrent != null) append(" ${chargerCurrent}A")
                                            if (!isDcCharging && acPhases != null) append(" ${acPhases}φ")
                                        }.trim()
                                        listOf(voltPart, kwhTimePart)
                                            .filter { it.isNotEmpty() }
                                            .joinToString("  ")
                                    } else {
                                        kwhTimePart
                                    }
                                } else ""

                                if (isCharging) {
                                    if (useHomeLayout) {
                                        if (chargingText.isNotEmpty() || !locationText.isNullOrBlank()) {
                                            Row(
                                                modifier = GlanceModifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (chargingText.isNotEmpty()) {
                                                    Text(
                                                        text = chargingText,
                                                        style = TextStyle(
                                                            color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                                                            fontSize = if (isCompact) 9.sp else 11.sp
                                                        )
                                                    )
                                                }
                                                if (!locationText.isNullOrBlank()) {
                                                    Spacer(modifier = GlanceModifier.defaultWeight())
                                                    Text(
                                                        text = locationText,
                                                        style = TextStyle(
                                                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                                            fontSize = 10.sp
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    } else if (chargingText.isNotEmpty()) {
                                        Text(
                                            text = chargingText,
                                            style = TextStyle(
                                                color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                                                fontSize = if (isCompact) 9.sp else 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Range at top center (home screen, 2x2+)
                            if (useHomeLayout && layout.showMileage && ratedRange != null) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .padding(top = 8.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = "${ratedRange.roundToInt()} ${if (isImperial) "mi" else "km"}",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            // Progress bar at the very bottom
                            val barHeight = if (isCompact) 4.dp else 6.dp
                            val progressBitmap = buildProgressBarBitmap(
                                batteryLevel, chargeLimit, isCharging, isDcCharging, palette
                            )
                            Box(
                                modifier = GlanceModifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Image(
                                    provider = ImageProvider(progressBitmap),
                                    contentDescription = null,
                                    modifier = GlanceModifier.fillMaxWidth().height(barHeight),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }
                }
            }
        }

    /**
     * Persists all [CarWidgetDisplayData] fields to Glance preferences and triggers
     * a redraw.
     */
    suspend fun updateWidget(context: Context, glanceId: GlanceId, data: CarWidgetDisplayData) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[CAR_ID_KEY] = data.carId
                this[HAS_DATA_KEY] = true
                this[CAR_NAME_KEY] = data.carName
                data.exteriorColor?.let { this[EXTERIOR_COLOR_KEY] = it }
                data.model?.let { this[MODEL_KEY] = it }
                data.trimBadging?.let { this[TRIM_BADGING_KEY] = it }
                data.wheelType?.let { this[WHEEL_TYPE_KEY] = it }
                data.state?.let { this[STATE_KEY] = it }
                this[IS_LOCKED_KEY] = data.isLocked
                this[SENTRY_MODE_KEY] = data.sentryModeActive
                this[PLUGGED_IN_KEY] = data.pluggedIn
                this[OUTSIDE_TEMP_KEY] = data.outsideTemp?.toFloat() ?: Float.NaN
                this[INSIDE_TEMP_KEY] = data.insideTemp?.toFloat() ?: Float.NaN
                this[IS_CLIMATE_ON_KEY] = data.isClimateOn
                this[BATTERY_LEVEL_KEY] = data.batteryLevel
                this[RATED_RANGE_KEY] = data.ratedBatteryRangeKm?.toFloat() ?: -1f
                this[CHARGE_LIMIT_KEY] = data.chargeLimitSoc ?: -1
                this[IS_CHARGING_KEY] = data.isCharging
                this[IS_DC_CHARGING_KEY] = data.isDcCharging
                this[CHARGER_POWER_KEY] = data.chargerPower ?: -1
                this[CHARGE_ENERGY_ADDED_KEY] = data.chargeEnergyAdded?.toFloat() ?: -1f
                this[TIME_TO_FULL_KEY] = data.timeToFullCharge?.toFloat() ?: -1f
                this[CHARGER_VOLTAGE_KEY] = data.chargerVoltage ?: -1
                this[CHARGER_CURRENT_KEY] = data.chargerActualCurrent ?: -1
                this[AC_PHASES_KEY] = data.acPhases ?: -1
                this[SENTRY_EVENT_COUNT_KEY] = data.sentryEventCount
                this[IS_IMPERIAL_KEY] = data.isImperial
                if (data.imageOverride != null) {
                    this[IMAGE_OVERRIDE_VARIANT_KEY] = data.imageOverride.variant
                    this[IMAGE_OVERRIDE_WHEEL_KEY] = data.imageOverride.wheelCode
                } else {
                    remove(IMAGE_OVERRIDE_VARIANT_KEY)
                    remove(IMAGE_OVERRIDE_WHEEL_KEY)
                }
                if (data.locationText != null) {
                    this[LOCATION_TEXT_KEY] = data.locationText
                } else {
                    remove(LOCATION_TEXT_KEY)
                }
            }
        }
        update(context, glanceId)
    }

    // -------------------------------------------------------------------------
    // Background bitmap — decorative only (car + scrim; glow disabled)
    // -------------------------------------------------------------------------

    /**
     * Generates the background bitmap at the car image's native resolution.
     * Layers (bottom to top):
     *  1. Palette surface color
     *  2. Dimmed car image (cover-scaled to fill)
     *  3. Gradient scrim (dark at top and bottom)
     *
     * No status bar or progress bar — those are rendered as Glance composables.
     * The bitmap is displayed with [ContentScale.Crop] so its dimensions do not
     * need to match the launcher-reported widget size.
     *
     * TODO: Add charging glow layers once GlowBitmapRenderer is implemented.
     */
    private fun buildBackgroundBitmap(
        context: Context,
        prefs: Preferences,
    ): Bitmap {
        val exteriorColor = prefs[EXTERIOR_COLOR_KEY]
        val model = prefs[MODEL_KEY]
        val trimBadging = prefs[TRIM_BADGING_KEY]
        val wheelType = prefs[WHEEL_TYPE_KEY]
        val overrideVariant = prefs[IMAGE_OVERRIDE_VARIANT_KEY]
        val overrideWheel = prefs[IMAGE_OVERRIDE_WHEEL_KEY]

        val palette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme = true)

        val carBitmap = loadCarBitmap(context, model, exteriorColor, wheelType, trimBadging, overrideVariant, overrideWheel)
        val width = carBitmap?.width ?: FALLBACK_BG_W
        val height = carBitmap?.height ?: FALLBACK_BG_H

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 1. Solid background
        canvas.drawColor(colorToAndroidArgb(palette.surface))

        // 2. Car image (dimmed car on top)
        // TODO: Add glow layers using GlowBitmapRenderer once implemented
        if (carBitmap != null) {
            val scaleByWidth = width.toFloat() / carBitmap.width
            val scaleByHeight = height.toFloat() / carBitmap.height
            val coverScale = maxOf(scaleByWidth, scaleByHeight)
            val scaledW = (carBitmap.width * coverScale).roundToInt().coerceAtLeast(1)
            val scaledH = (carBitmap.height * coverScale).roundToInt().coerceAtLeast(1)

            val carLeft = (width - scaledW) / 2f
            val carTop = (height - scaledH) / 2f

            // Apply dimming (20% alpha car image)
            val dimPaint = Paint().apply { alpha = 51 } // ~0.20 * 255
            val scaled = Bitmap.createScaledBitmap(carBitmap, scaledW, scaledH, true)
            canvas.drawBitmap(scaled, carLeft, carTop, dimPaint)
            scaled.recycle()
            carBitmap.recycle()
        }

        // 3. Gradient scrim
        val scrimPaint = Paint()
        scrimPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                android.graphics.Color.argb(210, 0, 0, 0),
                android.graphics.Color.argb(0, 0, 0, 0),
                android.graphics.Color.argb(210, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        return result
    }

    // -------------------------------------------------------------------------
    // Progress bar bitmap — rendered independently at a fixed reference size
    // -------------------------------------------------------------------------

    /**
     * Generates a progress bar bitmap at a fixed [PROGRESS_BAR_W] x [PROGRESS_BAR_H]
     * resolution.  Displayed with [ContentScale.FillBounds] in a fixed-height box.
     * Since the content is horizontal solid-colour fills, stretching is invisible.
     */
    private fun buildProgressBarBitmap(
        batteryLevel: Int,
        chargeLimit: Int?,
        isCharging: Boolean,
        isDcCharging: Boolean,
        palette: CarColorPalette
    ): Bitmap {
        val w = PROGRESS_BAR_W
        val h = PROGRESS_BAR_H
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val r = h / 2f

        // Track
        paint.color = android.graphics.Color.argb(80, 255, 255, 255)
        canvas.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, paint)

        // Charge limit zone
        if (chargeLimit != null && chargeLimit > batteryLevel) {
            val dimColor = if (isDcCharging) palette.dcColor else if (isCharging) palette.acColor else palette.accent
            paint.color = android.graphics.Color.argb(
                60,
                (dimColor.red * 255).toInt(),
                (dimColor.green * 255).toInt(),
                (dimColor.blue * 255).toInt()
            )
            canvas.drawRect(
                w * batteryLevel / 100f, 0f,
                w * chargeLimit / 100f, h.toFloat(),
                paint
            )
        }

        // Battery fill
        val fillColor = when {
            isCharging && isDcCharging -> palette.dcColor
            isCharging -> palette.acColor
            batteryLevel < 20 -> Color(0xFFEF5350)
            batteryLevel < 40 -> Color(0xFFFF9800)
            else -> palette.accent
        }
        paint.color = android.graphics.Color.argb(
            230,
            (fillColor.red * 255).toInt(),
            (fillColor.green * 255).toInt(),
            (fillColor.blue * 255).toInt()
        )
        val fillW = w * batteryLevel / 100f
        if (fillW > 0) {
            canvas.drawRoundRect(RectF(0f, 0f, fillW, h.toFloat()), r, r, paint)
        }

        return result
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * TODO: Replace with CarImageResolver once moved to a shared package.
     * For now, loads the car PNG from assets using a simple path builder.
     */
    private fun loadCarBitmap(
        context: Context,
        model: String?,
        exteriorColor: String?,
        wheelType: String?,
        trimBadging: String?,
        overrideVariant: String? = null,
        overrideWheel: String? = null
    ): Bitmap? {
        // Build a simple asset path from model + exteriorColor
        val colorCode = mapColor(exteriorColor)
        val modelName = model?.lowercase()?.replace(" ", "_") ?: "model_3"
        val assetPath = "cars/${modelName}_${colorCode}.png"
        return try {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
        } catch (_: IOException) {
            // Fallback: try a generic car image
            try {
                context.assets.open("cars/model_3_default.png").use { BitmapFactory.decodeStream(it) }
            } catch (_: IOException) {
                null
            }
        }
    }

    /** Simple color name → code mapping (subset). Expand as needed. */
    private fun mapColor(exteriorColor: String?): String {
        return when (exteriorColor?.lowercase()?.replace(" ", "")) {
            "deepblue", "ppsb" -> "deep_blue"
            "ultrared", "pr01" -> "ultra_red"
            "red", "ppmr" -> "red"
            "midnightcherry", "pr00" -> "midnight_cherry"
            "white", "ppsw" -> "pearl_white"
            "black", "pbsb" -> "solid_black"
            "quicksilver", "pn00" -> "quicksilver"
            "stealthgrey", "pn01" -> "stealth_grey"
            "midnightsilver", "pmng" -> "midnight_silver"
            else -> "pearl_white"
        }
    }

    private fun colorToAndroidArgb(color: Color): Int = android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )

    private suspend fun isWidgetOnLockScreen(context: Context, glanceId: GlanceId): Boolean {
        return try {
            val glanceManager = GlanceAppWidgetManager(context)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CarWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                if (glanceManager.getGlanceIdBy(appWidgetId) == glanceId) {
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    return options.getInt(
                        AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
                        AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN
                    ) == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
