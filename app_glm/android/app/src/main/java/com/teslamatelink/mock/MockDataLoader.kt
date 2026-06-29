package com.teslamatelink.mock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.api.model.CarRaw
import com.teslamatelink.data.api.model.CarStatus
import com.teslamatelink.data.api.model.ChargeRaw
import com.teslamatelink.data.api.model.DriveRaw
import com.teslamatelink.data.api.model.UpdateRaw
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and caches the mock data from assets/mock_data.json.
 *
 * The JSON structure mirrors the TeslaMate API responses so
 * this same loader can be adapted to real API parsing later.
 */
@Singleton
class MockDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private var cached: MockDataContainer? = null

    /** Top-level schema of mock_data.json */
    data class MockDataContainer(
        @SerializedName("cars") val cars: List<CarRaw>? = null,
        @SerializedName("status") val status: Map<String, CarStatus>? = null,
        @SerializedName("drives") val drives: List<DriveRaw>? = null,
        @SerializedName("charges") val charges: List<ChargeRaw>? = null,
        @SerializedName("batteryHealth") val batteryHealth: Map<String, BatteryHealth>? = null,
        @SerializedName("updates") val updates: Map<String, List<UpdateRaw>>? = null
    )

    /** Ensure the JSON is loaded once and cached. */
    private fun load(): MockDataContainer {
        cached?.let { return it }
        val json = context.assets.open("mock_data.json")
            .bufferedReader()
            .use { it.readText() }
        val container: MockDataContainer = gson.fromJson(json, MockDataContainer::class.java)
        cached = container
        return container
    }

    // ── Public accessors ─────────────────────────────────

    fun getCars(): List<CarRaw> = load().cars ?: emptyList()

    fun getCar(carId: Int): CarRaw? = getCars().find { it.carId == carId }

    fun getStatus(carId: Int): CarStatus? = load().status?.get(carId.toString())

    fun getAllStatus(): Map<String, CarStatus> = load().status ?: emptyMap()

    fun getDrives(): List<DriveRaw> = load().drives ?: emptyList()

    fun getDrivesForCar(carId: Int): List<DriveRaw> = getDrives().filter { it.carId == carId }

    fun getCharges(): List<ChargeRaw> = load().charges ?: emptyList()

    fun getChargesForCar(carId: Int): List<ChargeRaw> = getCharges().filter { it.carId == carId }

    fun getBatteryHealth(carId: Int): BatteryHealth? = load().batteryHealth?.get(carId.toString())

    fun getUpdates(carId: Int): List<UpdateRaw> = load().updates?.get(carId.toString()) ?: emptyList()
}
