package com.teslamatelink.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.teslamatelink.data.local.entity.SavedTrip
import com.teslamatelink.data.local.entity.SavedTripConsumedFingerprint
import com.teslamatelink.data.local.entity.SavedTripLeg
import com.teslamatelink.data.local.entity.SavedTripWithLegs

@Dao
interface SavedTripDao {

    @Transaction
    @Query("SELECT * FROM saved_trips WHERE carId = :carId ORDER BY id ASC")
    suspend fun getAllWithLegs(carId: Int): List<SavedTripWithLegs>

    @Transaction
    @Query("SELECT * FROM saved_trips WHERE id = :tripId")
    suspend fun getWithLegs(tripId: Long): SavedTripWithLegs?

    @Query("""
        SELECT fp.fingerprint FROM saved_trip_consumed_fingerprints fp
        INNER JOIN saved_trips st ON fp.savedTripId = st.id
        WHERE st.carId = :carId
    """)
    suspend fun getAllConsumedFingerprintsForCar(carId: Int): List<String>

    @Insert
    suspend fun insertTrip(trip: SavedTrip): Long

    @Insert
    suspend fun insertLegs(legs: List<SavedTripLeg>)

    @Insert
    suspend fun insertConsumedFingerprints(rows: List<SavedTripConsumedFingerprint>)

    /** Atomically insert a trip and its legs, returning the generated trip id. */
    @Transaction
    suspend fun insertTripWithLegs(trip: SavedTrip, legs: (Long) -> List<SavedTripLeg>): Long {
        val tripId = insertTrip(trip)
        val resolved = legs(tripId)
        if (resolved.isNotEmpty()) insertLegs(resolved)
        return tripId
    }

    @Query("DELETE FROM saved_trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    // === Edit/merge helpers (PR 2) ===

    @Query("DELETE FROM saved_trip_legs WHERE tripId = :tripId")
    suspend fun deleteLegs(tripId: Long)

    @Query("UPDATE saved_trips SET source = :source, updatedAt = :updatedAt WHERE id = :tripId")
    suspend fun updateSource(tripId: Long, source: String, updatedAt: Long)

    @Query("SELECT name FROM saved_trips WHERE id = :tripId")
    suspend fun getName(tripId: Long): String?

    @Query("UPDATE saved_trips SET name = :name, updatedAt = :updatedAt WHERE id = :tripId")
    suspend fun updateName(tripId: Long, name: String?, updatedAt: Long)

    @Query("SELECT fingerprint FROM saved_trip_consumed_fingerprints WHERE savedTripId = :tripId")
    suspend fun getConsumedFingerprints(tripId: Long): List<String>

    /** Drive/charge IDs already referenced by any saved trip belonging to [carId]. */
    @Query("""
        SELECT DISTINCT legId FROM saved_trip_legs
        WHERE legType = :legType
          AND tripId IN (SELECT id FROM saved_trips WHERE carId = :carId)
    """)
    suspend fun getUsedLegIds(carId: Int, legType: String): List<Int>

    /** Atomically replace all legs of a trip. Used when adding legs (re-numbers positions). */
    @Transaction
    suspend fun replaceLegs(tripId: Long, newLegs: List<SavedTripLeg>) {
        deleteLegs(tripId)
        if (newLegs.isNotEmpty()) insertLegs(newLegs)
    }
}
