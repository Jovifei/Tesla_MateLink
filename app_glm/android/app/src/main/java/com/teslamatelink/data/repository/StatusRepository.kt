package com.teslamatelink.data.repository

import com.teslamatelink.data.api.model.CarStatus
import com.teslamatelink.data.api.model.TirePressure
import com.teslamatelink.mock.MockDataLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Repository interface for real-time vehicle status.
 *
 * Emits status updates as a [Flow] — the source may be
 * the mock random-walk engine or a future polling API.
 */
interface StatusRepository {
    fun observeStatus(carId: Int): Flow<CarStatus>
}

/**
 * Mock implementation that generates a random-walk sequence
 * starting from the static mock snapshot.
 *
 * Each tick perturbs battery, range, temperatures, and
 * odometer slightly so the UI gets live-feeling data.
 */
@Singleton
class MockStatusRepository @Inject constructor(
    private val mockDataLoader: MockDataLoader
) : StatusRepository {

    companion object {
        private const val UPDATE_INTERVAL_MS = 5000L
    }

    private val currentStatus = mutableMapOf<Int, CarStatus>()
    private val rng = Random(42) // Seeded for reproducibility

    override fun observeStatus(carId: Int): Flow<CarStatus> = flow {
        // Seed from mock data once
        if (currentStatus[carId] == null) {
            val mock = mockDataLoader.getStatus(carId)
                ?: CarStatus(carId = carId, state = "online")
            currentStatus[carId] = mock
        }

        while (true) {
            emit(currentStatus[carId]!!)
            delay(UPDATE_INTERVAL_MS)
            currentStatus[carId] = randomWalk(currentStatus[carId]!!)
        }
    }

    /** Apply a small Gaussian-like perturbation to numeric fields. */
    private fun randomWalk(status: CarStatus): CarStatus {
        fun Double.randWalk(scale: Double): Double =
            (this + (rng.nextGaussian() * scale)).coerceAtLeast(0.0)

        fun Int.randWalk(scale: Int): Int =
            (this + (rng.nextInt(-scale, scale + 1)).toDouble().roundToInt()).coerceAtLeast(0)

        val batt = (status.batteryLevel ?: 50).randWalk(1).coerceIn(0, 100)
        val usable = (status.usableBatteryLevel ?: 50).randWalk(1).coerceIn(0, 100)

        return status.copy(
            batteryLevel = batt,
            usableBatteryLevel = usable,
            idealBatteryRangeKm = (status.idealBatteryRangeKm ?: 400.0).randWalk(2.0),
            estBatteryRangeKm = (status.estBatteryRangeKm ?: 350.0).randWalk(3.0),
            insideTemp = (status.insideTemp ?: 22.0).randWalk(0.3),
            outsideTemp = (status.outsideTemp ?: 25.0).randWalk(0.5),
            odometer = (status.odometer ?: 0.0).randWalk(0.05),
            chargeEnergyAdded = status.chargeEnergyAdded?.let {
                if (status.isCharging) it.randWalk(0.1) else it
            },
            timeToFullCharge = status.timeToFullCharge?.let {
                if (status.isCharging && it > 0) (it - 0.01).coerceAtLeast(0.0) else it
            },
            chargerPower = status.chargerPower?.let {
                if (status.isCharging) it.randWalk(0.2) else it
            },
            tirePressure = status.tirePressure?.let { tp ->
                TirePressure(
                    frontLeft = tp.frontLeft?.randWalk(0.02),
                    frontRight = tp.frontRight?.randWalk(0.02),
                    rearLeft = tp.rearLeft?.randWalk(0.02),
                    rearRight = tp.rearRight?.randWalk(0.02)
                )
            }
        )
    }
}
