package com.teslamatelink.data.repository

import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import com.teslamatelink.domain.model.toDomain
import com.teslamatelink.mock.MockDataLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for car data.
 *
 * Abstracts the data source (mock vs. real API) behind
 * a clean domain-oriented API.
 */
interface CarRepository {
    fun getCars(): Flow<List<Car>>
    fun getCar(carId: Int): Flow<Car?>
    suspend fun refreshCars(): List<Car>
    suspend fun getDrives(carId: Int): List<Drive>
    suspend fun getCharges(carId: Int): List<Charge>
    suspend fun getBatteryHealth(carId: Int): BatteryHealth?
    suspend fun getUpdates(carId: Int): List<UpdateItem>
}

/**
 * Mock implementation that reads from mock_data.json loaded
 * from assets via [MockDataLoader].
 */
@Singleton
class MockCarRepository @Inject constructor(
    private val mockDataLoader: MockDataLoader
) : CarRepository {

    private val carsFlow = MutableStateFlow<List<Car>>(emptyList())

    override fun getCars(): Flow<List<Car>> {
        if (carsFlow.value.isEmpty()) {
            carsFlow.value = mockDataLoader.getCars().map { it.toDomain() }
        }
        return carsFlow.asStateFlow()
    }

    override fun getCar(carId: Int): Flow<Car?> {
        val car = mockDataLoader.getCar(carId)?.toDomain()
        return MutableStateFlow(car).asStateFlow()
    }

    override suspend fun refreshCars(): List<Car> {
        val cars = mockDataLoader.getCars().map { it.toDomain() }
        carsFlow.value = cars
        return cars
    }

    override suspend fun getDrives(carId: Int): List<Drive> {
        return mockDataLoader.getDrivesForCar(carId).map { it.toDomain() }
    }

    override suspend fun getCharges(carId: Int): List<Charge> {
        return mockDataLoader.getChargesForCar(carId).map { it.toDomain() }
    }

    override suspend fun getBatteryHealth(carId: Int): BatteryHealth? {
        return mockDataLoader.getBatteryHealth(carId)
    }

    override suspend fun getUpdates(carId: Int): List<UpdateItem> {
        return mockDataLoader.getUpdates(carId).map { it.toDomain() }
    }
}
