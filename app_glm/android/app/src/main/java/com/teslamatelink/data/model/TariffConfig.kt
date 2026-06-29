package com.teslamatelink.data.model

import com.google.gson.annotations.SerializedName

data class TariffConfig(
    @SerializedName("is_enabled") val isEnabled: Boolean = true,
    @SerializedName("peak_price") val peakPrice: Double = 1.0,
    @SerializedName("flat_price") val flatPrice: Double = 0.7,
    @SerializedName("valley_price") val valleyPrice: Double = 0.3,
    @SerializedName("currency_code") val currencyCode: String = "CNY"
) {
    fun priceForHour(hour: Int): Double = when (hour) {
        in 0..6 -> valleyPrice
        in 9..11, in 17..21 -> peakPrice
        else -> flatPrice
    }

    companion object {
        val DEFAULT = TariffConfig()
    }
}
