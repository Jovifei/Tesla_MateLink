package com.teslamatelink.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.teslamatelink.data.local.entity.ChargeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the charges table.
 *
 * Provides CRUD operations and aggregate queries used
 * by the stats and history screens.
 */
@Dao
interface ChargeDao {

    // ── Write ────────────────────────────────────────────

    @Upsert
    suspend fun upsertAll(charges: List<ChargeEntity>)

    @Upsert
    suspend fun upsert(charge: ChargeEntity)

    // ── Read ─────────────────────────────────────────────

    @Query("SELECT * FROM charges WHERE car_id = :carId ORDER BY start_date DESC")
    fun observeAll(carId: Int): Flow<List<ChargeEntity>>

    @Query("SELECT * FROM charges WHERE car_id = :carId AND id = :chargeId")
    suspend fun get(carId: Int, chargeId: Int): ChargeEntity?

    @Query("SELECT COALESCE(MAX(id), 0) FROM charges WHERE car_id = :carId")
    suspend fun getMaxId(carId: Int): Int

    @Query("SELECT * FROM charges WHERE car_id = :carId ORDER BY start_date ASC")
    suspend fun getAllChronological(carId: Int): List<ChargeEntity>

    // ── Delete ───────────────────────────────────────────

    @Query("DELETE FROM charges WHERE car_id = :carId")
    suspend fun deleteAllForCar(carId: Int)

    // ── Stats ────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM charges WHERE car_id = :carId")
    suspend fun count(carId: Int): Int

    @Query("SELECT COUNT(*) FROM charges WHERE car_id = :carId")
    fun observeCount(carId: Int): Flow<Int>

    @Query("SELECT COALESCE(SUM(charge_energy_added), 0) FROM charges WHERE car_id = :carId")
    suspend fun sumEnergyAdded(carId: Int): Double

    @Query("SELECT COALESCE(SUM(cost), 0) FROM charges WHERE car_id = :carId")
    suspend fun sumCost(carId: Int): Double

    @Query("SELECT COALESCE(SUM(cost) / NULLIF(SUM(charge_energy_added), 0), 0) FROM charges WHERE car_id = :carId AND cost IS NOT NULL")
    suspend fun avgCostPerKwh(carId: Int): Double

    @Query("SELECT MAX(charge_energy_added) FROM charges WHERE car_id = :carId")
    suspend fun maxEnergyAdded(carId: Int): Double?

    @Query("SELECT MIN(start_date) FROM charges WHERE car_id = :carId")
    suspend fun firstChargeDate(carId: Int): String?

    @Query("SELECT MAX(start_date) FROM charges WHERE car_id = :carId")
    suspend fun lastChargeDate(carId: Int): String?
}
