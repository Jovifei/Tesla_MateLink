package com.teslamatelink.data.repository

import com.google.common.truth.Truth.assertThat
import com.teslamatelink.MainDispatcherRule
import com.teslamatelink.buildInMemoryDb
import com.teslamatelink.data.api.TeslaMateApi
import com.teslamatelink.data.local.AppDatabase
import com.teslamatelink.data.local.dao.ChargeDao
import com.teslamatelink.data.local.dao.DriveDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class RealCarRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var server: MockWebServer
    private lateinit var api: TeslaMateApi
    private lateinit var db: AppDatabase
    private lateinit var driveDao: DriveDao
    private lateinit var chargeDao: ChargeDao
    private lateinit var repository: RealCarRepository

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TeslaMateApi::class.java)
        db = buildInMemoryDb()
        driveDao = db.driveDao()
        chargeDao = db.chargeDao()
        repository = RealCarRepository(api, driveDao, chargeDao)
    }

    @After
    fun tearDown() {
        server.shutdown()
        db.close()
    }

    // ── T-009: API success + cache write ────────────────────────────────

    @Test
    fun getDrives_success_writesToRoomCache() = runTest {
        val json = """{"data":{"drives":[{"id":1,"car_id":5,"start_date":"2026-01-01T00:00:00Z","end_date":"2026-01-01T01:00:00Z","distance_km":42.5,"duration_min":60,"consumption_kWh":6.4,"start_battery_level":80,"end_battery_level":50}]}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val drives = repository.getDrives(5)

        assertThat(drives).hasSize(1)
        assertThat(drives[0].id).isEqualTo(1)
        assertThat(drives[0].distanceKm).isEqualTo(42.5)
        // Cache was written
        val cached = driveDao.getAllChronological(5)
        assertThat(cached).hasSize(1)
        assertThat(cached[0].id).isEqualTo(1)
    }

    @Test
    fun getCharges_success_writesToRoomCache() = runTest {
        val json = """{"data":{"charges":[{"id":10,"car_id":5,"start_date":"2026-01-01T00:00:00Z","end_date":"2026-01-01T02:00:00Z","charge_energy_added":30.0,"charge_energy_used":32.0,"start_battery_level":20,"end_battery_level":90,"cost":12.5,"charge_type":"ac","address":"Supercharger"}]}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val charges = repository.getCharges(5)

        assertThat(charges).hasSize(1)
        assertThat(charges[0].id).isEqualTo(10)
        assertThat(charges[0].chargeEnergyAdded).isEqualTo(30.0)
        val cached = chargeDao.getAllChronological(5)
        assertThat(cached).hasSize(1)
    }

    @Test
    fun refreshCars_success_returnsDomainCars() = runTest {
        val json = """{"data":{"cars":[{"car_id":1,"name":"My Tesla","car_details":{"vin":"5YJ...","model":"3"},"teslamate_stats":{"total_charges":10,"total_drives":50,"total_updates":3}}]}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val cars = repository.refreshCars()

        assertThat(cars).hasSize(1)
        assertThat(cars[0].carId).isEqualTo(1)
        assertThat(cars[0].name).isEqualTo("My Tesla")
        assertThat(cars[0].totalDrives).isEqualTo(50)
    }

    // ── T-010: API failure → cache fallback ─────────────────────────────

    @Test
    fun getDrives_apiError_fallsBackToRoomCache() = runTest {
        val json = """{"data":{"drives":[{"id":1,"car_id":5,"distance_km":42.5}]}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
        repository.getDrives(5)

        server.enqueue(MockResponse().setResponseCode(500))
        val drives = repository.getDrives(5)

        assertThat(drives).hasSize(1)
        assertThat(drives[0].id).isEqualTo(1)
    }

    @Test
    fun getDrives_emptyCache_onFirstFailure_returnsEmpty() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val drives = repository.getDrives(999)
        assertThat(drives).isEmpty()
    }

    @Test
    fun getBatteryHealth_success_returnsHealth() = runTest {
        val json = """{"data":{"battery_health":{"car_id":1,"current_capacity_kwh":72.0}}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val health = repository.getBatteryHealth(1)
        assertThat(health).isNotNull()
        assertThat(health!!.currentCapacityKwh).isEqualTo(72.0)
    }

    @Test
    fun getBatteryHealth_failure_returnsNull() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertThat(repository.getBatteryHealth(1)).isNull()
    }

    @Test
    fun getUpdates_success_returnsUpdateItems() = runTest {
        val json = """{"data":{"updates":[{"id":1,"car_id":5,"version":"2026.1.2","start_date":"2026-01-01"}]}}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val updates = repository.getUpdates(5)
        assertThat(updates).hasSize(1)
        assertThat(updates[0].version).isEqualTo("2026.1.2")
    }
}
