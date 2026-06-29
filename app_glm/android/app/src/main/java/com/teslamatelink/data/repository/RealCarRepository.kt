package com.teslamatelink.data.repository

import android.util.Log
import com.teslamatelink.data.api.TeslaMateApi
import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.dao.ChargeDao
import com.teslamatelink.data.local.dao.DriveDao
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import com.teslamatelink.domain.model.toDomain
import com.teslamatelink.domain.model.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RealCarRepository"

/**
 * Network-first [CarRepository] backed by [TeslaMateApi] with Room cache
 * fallback for drive and charge history.
 */
@Singleton
class RealCarRepository @Inject constructor(
    private val api: TeslaMateApi,
    private val driveDao: DriveDao,
    private val chargeDao: ChargeDao
) : CarRepository {

    // ── Cars (no cache) ────────────────────────────────────

    override fun getCars(): Flow<List<Car>> = flow {
        emit(fetchCarsFromApi())
    }

    override fun getCar(carId: Int): Flow<Car?> = flow {
        emit(fetchCarsFromApi().find { it.carId == carId })
    }

    override suspend fun refreshCars(): List<Car> = fetchCarsFromApi()

    private suspend fun fetchCarsFromApi(): List<Car> {
        return try {
            val response = api.getCars()
            if (response.isSuccessful) {
                response.body()?.data?.cars.orEmpty().map { it.toDomain() }
            } else {
                Log.w(TAG, "getCars HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getCars network error", e)
            emptyList()
        }
    }

    // ── Drives (network-first + cache fallback) ────────────

    override suspend fun getDrives(carId: Int): List<Drive> {
        return try {
            val response = api.getDrives(carId)
            if (response.isSuccessful) {
                val entities = response.body()?.data?.drives.orEmpty().map { it.toEntity() }
                if (entities.isNotEmpty()) driveDao.upsertAll(entities)
                entities.map { it.toDomain() }
            } else {
                Log.w(TAG, "getDrives HTTP ${response.code()}, fallback to cache")
                driveDao.getAllChronological(carId).map { it.toDomain() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getDrives network error, fallback to cache", e)
            driveDao.getAllChronological(carId).map { it.toDomain() }
        }
    }

    // ── Charges (network-first + cache fallback) ───────────

    override suspend fun getCharges(carId: Int): List<Charge> {
        return try {
            val response = api.getCharges(carId)
            if (response.isSuccessful) {
                val entities = response.body()?.data?.charges.orEmpty().map { it.toEntity() }
                if (entities.isNotEmpty()) chargeDao.upsertAll(entities)
                entities.map { it.toDomain() }
            } else {
                Log.w(TAG, "getCharges HTTP ${response.code()}, fallback to cache")
                chargeDao.getAllChronological(carId).map { it.toDomain() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getCharges network error, fallback to cache", e)
            chargeDao.getAllChronological(carId).map { it.toDomain() }
        }
    }

    // ── Battery Health (no cache) ──────────────────────────

    override suspend fun getBatteryHealth(carId: Int): BatteryHealth? {
        return try {
            val response = api.getBatteryHealth(carId)
            if (response.isSuccessful) {
                response.body()?.data?.batteryHealth
            } else {
                Log.w(TAG, "getBatteryHealth HTTP ${response.code()}")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getBatteryHealth network error", e)
            null
        }
    }

    // ── Updates (no cache) ─────────────────────────────────

    override suspend fun getUpdates(carId: Int): List<UpdateItem> {
        return try {
            val response = api.getUpdates(carId)
            if (response.isSuccessful) {
                response.body()?.data?.updates.orEmpty().map { it.toDomain() }
            } else {
                Log.w(TAG, "getUpdates HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getUpdates network error", e)
            emptyList()
        }
    }
}
