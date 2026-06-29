package com.teslamatelink.domain

import com.teslamatelink.domain.model.Units

/**
 * Utility object for formatting values based on unit preferences.
 * Supports metric (km, °C, bar) and imperial (mi, °F, psi) units.
 *
 * All input values are expected in metric base units (km, °C, bar, m).
 * Conversion to imperial is applied internally when the user's unit
 * preference is set to imperial.
 */
object UnitFormatter {

    /**
     * Format elevation value with appropriate unit label.
     * Converts meters to feet when imperial is selected.
     */
    fun formatElevation(value: Int?, units: Units?): String {
        val v = value ?: 0
        return if (units?.isImperial == true) {
            "%,d ft".format((v * 3.28084).toInt())
        } else {
            "%,d m".format(v)
        }
    }

    fun getElevationValue(value: Float, units: Units?): Float {
        return if (units?.isImperial == true) (value * 3.28084f) else value
    }

    fun getElevationUnit(units: Units?): String {
        return if (units?.isImperial == true) "ft" else "m"
    }

    fun formatDistance(value: Double, units: Units?, decimals: Int = 1): String {
        return if (units?.isImperial == true) {
            "%,.${decimals}f mi".format(value * 0.621371)
        } else {
            "%,.${decimals}f km".format(value)
        }
    }

    fun formatDistanceValue(value: Double, units: Units?, decimals: Int = 1): Double {
        return if (units?.isImperial == true) value * 0.621371 else value
    }

    fun getDistanceUnit(units: Units?): String {
        return if (units?.isImperial == true) "mi" else "km"
    }

    fun formatTemperature(value: Double, units: Units?, decimals: Int = 0): String {
        return if (units?.unitOfTemperature == "F") {
            "%.${decimals}f°F".format(value * 9.0 / 5.0 + 32.0)
        } else {
            "%.${decimals}f°C".format(value)
        }
    }

    fun formatTemperatureValue(value: Double, units: Units?): Double {
        return if (units?.unitOfTemperature == "F") value * 9.0 / 5.0 + 32.0 else value
    }

    fun getTemperatureUnit(units: Units?): String {
        return if (units?.unitOfTemperature == "F") "°F" else "°C"
    }

    fun formatPressure(value: Double, units: Units?, decimals: Int = 1): String {
        return if (units?.unitOfPressure == "psi") {
            "%.${decimals}f psi".format(value * 14.5038)
        } else {
            "%.${decimals}f bar".format(value)
        }
    }

    fun formatPressureValue(value: Double, units: Units?): Double {
        return if (units?.unitOfPressure == "psi") value * 14.5038 else value
    }

    fun getPressureUnit(units: Units?): String {
        return if (units?.unitOfPressure == "psi") "psi" else "bar"
    }

    fun formatEfficiency(value: Double, units: Units?, decimals: Int = 1): String {
        return if (units?.isImperial == true) {
            "%.${decimals}f Wh/mi".format(value)
        } else {
            "%.${decimals}f Wh/km".format(value)
        }
    }

    fun getEfficiencyUnit(units: Units?): String {
        return if (units?.isImperial == true) "Wh/mi" else "Wh/km"
    }

    fun formatSpeed(value: Double, units: Units?, decimals: Int = 0): String {
        return if (units?.isImperial == true) {
            "%.${decimals}f mph".format(value)
        } else {
            "%.${decimals}f km/h".format(value)
        }
    }

    fun getSpeedUnit(units: Units?): String {
        return if (units?.isImperial == true) "mph" else "km/h"
    }
}
