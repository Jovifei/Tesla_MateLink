package com.teslamatelink.data.repository

import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proxy [CarRepository] that delegates to either [MockCarRepository] or
 * [RealCarRepository] based on [SettingsDataStore.useRealDataSourceSnapshot].
 *
 * Enables runtime switching without rebuilding the Hilt graph or restarting the app.
 * ViewModels are unaware of the switch (CarRepository interface unchanged).
 */
@Singleton
class DelegatingCarRepository @Inject constructor(
    private val settings: SettingsDataStore,
    @com.teslamatelink.di.MockImpl private val mock: CarRepository,
    @com.teslamatelink.di.RealImpl private val real: CarRepository
) : CarRepository {

    private val delegate: CarRepository
        get() = if (settings.useRealDataSourceSnapshot()) real else mock

    override fun getCars(): Flow<List<Car>> = delegate.getCars()

    override fun getCar(carId: Int): Flow<Car?> = delegate.getCar(carId)

    override suspend fun refreshCars(): List<Car> = delegate.refreshCars()

    override suspend fun getDrives(carId: Int): List<Drive> = delegate.getDrives(carId)

    override suspend fun getCharges(carId: Int): List<Charge> = delegate.getCharges(carId)

    override suspend fun getBatteryHealth(carId: Int): BatteryHealth? =
        delegate.getBatteryHealth(carId)

    override suspend fun getUpdates(carId: Int): List<UpdateItem> =
        delegate.getUpdates(carId)
}
