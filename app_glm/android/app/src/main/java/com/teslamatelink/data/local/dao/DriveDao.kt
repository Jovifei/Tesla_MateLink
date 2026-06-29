package com.teslamatelink.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.teslamatelink.data.local.entity.DriveEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the drives table.
 *
 * Provides CRUD operations and aggregate queries used
 * by the stats and history screens.
 */
@Dao
interface DriveDao {

    // ── Write ────────────────────────────────────────────

    @Upsert
    suspend fun upsertAll(drives: List<DriveEntity>)

    @Upsert
    suspend fun upsert(drive: DriveEntity)

    // ── Read ─────────────────────────────────────────────

    @Query("SELECT * FROM drives WHERE car_id = :carId ORDER BY start_date DESC")
    fun observeAll(carId: Int): Flow<List<DriveEntity>>

    @Query("SELECT * FROM drives WHERE car_id = :carId AND id = :driveId")
    suspend fun get(carId: Int, driveId: Int): DriveEntity?

    @Query("SELECT COALESCE(MAX(id), 0) FROM drives WHERE car_id = :carId")
    suspend fun getMaxId(carId: Int): Int

    @Query("SELECT * FROM drives WHERE car_id = :carId ORDER BY start_date ASC")
    suspend fun getAllChronological(carId: Int): List<DriveEntity>

    // ── Delete ───────────────────────────────────────────

    @Query("DELETE FROM drives WHERE car_id = :carId")
    suspend fun deleteAllForCar(carId: Int)

    // ── Stats ────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM drives WHERE car_id = :carId")
    suspend fun count(carId: Int): Int

    @Query("SELECT COUNT(*) FROM drives WHERE car_id = :carId")
    fun observeCount(carId: Int): Flow<Int>

    @Query("SELECT COALESCE(SUM(distance_km), 0) FROM drives WHERE car_id = :carId")
    suspend fun sumDistance(carId: Int): Double

    @Query("SELECT COALESCE(SUM(consumption_kwh), 0) FROM drives WHERE car_id = :carId")
    suspend fun sumEnergyConsumed(carId: Int): Double

    @Query("SELECT MAX(distance_km) FROM drives WHERE car_id = :carId")
    suspend fun maxDistance(carId: Int): Double?

    @Query("SELECT AVG(efficiency) FROM drives WHERE car_id = :carId AND efficiency > 0")
    suspend fun avgEfficiency(carId: Int): Double?

    @Query("SELECT MIN(start_date) FROM drives WHERE car_id = :carId")
    suspend fun firstDriveDate(carId: Int): String?

    @Query("SELECT MAX(start_date) FROM drives WHERE car_id = :carId")
    suspend fun lastDriveDate(carId: Int): String?
}
