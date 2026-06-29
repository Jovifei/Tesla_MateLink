package com.teslamatelink.data.repository

import com.google.common.truth.Truth.assertThat
import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DelegatingCarRepositoryTest {

    private val mockRepo: CarRepository = mockk(relaxed = true)
    private val realRepo: CarRepository = mockk(relaxed = true)
    private val settings: SettingsDataStore = mockk(relaxed = true)

    private val delegating = DelegatingCarRepository(settings, mockRepo, realRepo)

    @Test
    fun getCars_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expected = listOf(
            Car(
                carId = 1,
                name = "Mock Car",
                vin = "VIN001",
                exteriorColor = "Red",
                wheelType = "19in"
            )
        )
        every { mockRepo.getCars() } returns flowOf(expected)

        val result = delegating.getCars()
        result.collect { assertThat(it).isEqualTo(expected) }
    }

    @Test
    fun getCars_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(
            Car(
                carId = 2,
                name = "Real Car",
                vin = "VIN002",
                exteriorColor = "Blue",
                wheelType = "21in"
            )
        )
        every { realRepo.getCars() } returns flowOf(expected)

        val result = delegating.getCars()
        result.collect { assertThat(it).isEqualTo(expected) }
    }

    @Test
    fun getDrives_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(
            Drive(
                id = 1,
                carId = 5,
                distanceKm = 100.0,
                startBatteryLevel = 80,
                endBatteryLevel = 60,
                startAddress = "Home",
                endAddress = "Work"
            )
        )
        coEvery { realRepo.getDrives(5) } returns expected

        assertThat(delegating.getDrives(5)).isEqualTo(expected)
    }

    @Test
    fun getDrives_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expected = listOf(
            Drive(
                id = 2,
                carId = 5,
                distanceKm = 50.0,
                startBatteryLevel = 90,
                endBatteryLevel = 75,
                startAddress = "Work",
                endAddress = "Home"
            )
        )
        coEvery { mockRepo.getDrives(5) } returns expected

        assertThat(delegating.getDrives(5)).isEqualTo(expected)
    }

    @Test
    fun getCharges_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(
            Charge(
                id = 1,
                carId = 5,
                chargeEnergyAdded = 30.0,
                startBatteryLevel = 20,
                endBatteryLevel = 80,
                address = "Home"
            )
        )
        coEvery { realRepo.getCharges(5) } returns expected

        assertThat(delegating.getCharges(5)).isEqualTo(expected)
    }

    @Test
    fun getBatteryHealth_delegatesCorrectly() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = BatteryHealth(currentCapacityKwh = 72.0)
        coEvery { realRepo.getBatteryHealth(1) } returns expected

        assertThat(delegating.getBatteryHealth(1)).isEqualTo(expected)
    }

    @Test
    fun getUpdates_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expected = listOf(
            UpdateItem(id = 1, carId = 5, version = "2026.1")
        )
        coEvery { mockRepo.getUpdates(5) } returns expected

        assertThat(delegating.getUpdates(5)).isEqualTo(expected)
    }

    @Test
    fun refreshCars_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(
            Car(
                carId = 1,
                name = "Real Car",
                vin = "VIN003",
                exteriorColor = "White",
                wheelType = "20in"
            )
        )
        coEvery { realRepo.refreshCars() } returns expected

        assertThat(delegating.refreshCars()).isEqualTo(expected)
    }
}
