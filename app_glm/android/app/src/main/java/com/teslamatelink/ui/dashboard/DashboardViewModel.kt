package com.teslamatelink.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teslamatelink.data.repository.CarRepository
import com.teslamatelink.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarStatus(
    val displayName: String = "My Tesla",
    val state: String = "online",
    val batteryLevel: Int = 0,
    val estBatteryRangeKm: Double = 0.0,
    val odometer: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val insideTemp: Double? = null,
    val outsideTemp: Double? = null,
    val isClimateOn: Boolean = false,
    val sentryMode: Boolean = false,
    val locked: Boolean = true,
    val frontLeftPsi: Double? = null,
    val frontRightPsi: Double? = null,
    val rearLeftPsi: Double? = null,
    val rearRightPsi: Double? = null,
    val pluggedIn: Boolean = false,
    val isCharging: Boolean = false,
    val isDcCharging: Boolean = false,
    val chargerPower: Double? = null,
    val chargeEnergyAdded: Double? = null,
    val timeToFullChargeHours: Double? = null,
    val chargeLimitSoc: Int = 80
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val carStatus: CarStatus? = null,
    val cars: List<CarInfo> = emptyList(),
    val selectedCarId: Int = 1,
    val error: String? = null
)

data class CarInfo(
    val id: Int,
    val name: String,
    val model: String,
    val exteriorColor: String? = null,
    val totalDrives: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val statusRepository: StatusRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var statusJob: Job? = null

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val cars = carRepository.getCars().firstOrNull() ?: emptyList()
                val carInfos = cars.map { car ->
                    CarInfo(
                        id = car.carId,
                        name = car.name,
                        model = car.model ?: "",
                        exteriorColor = car.exteriorColor,
                        totalDrives = car.totalDrives ?: 0
                    )
                }
                val selectedId = carInfos.firstOrNull()?.id ?: 1
                _uiState.value = _uiState.value.copy(
                    cars = carInfos,
                    selectedCarId = selectedId,
                    isLoading = false
                )
                loadCarStatus()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadCarStatus() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            val carId = _uiState.value.selectedCarId
            try {
                // Observe real-time status via StatusRepository Flow
                statusRepository.observeStatus(carId).collect { apiStatus ->
                    val status = CarStatus(
                        displayName = _uiState.value.cars.find { it.id == carId }?.name ?: "Tesla",
                        state = apiStatus.state ?: "unknown",
                        batteryLevel = apiStatus.batteryLevel ?: 0,
                        estBatteryRangeKm = apiStatus.estBatteryRangeKm ?: 0.0,
                        odometer = apiStatus.odometer ?: 0.0,
                        latitude = apiStatus.latitude,
                        longitude = apiStatus.longitude,
                        insideTemp = apiStatus.insideTemp,
                        outsideTemp = apiStatus.outsideTemp,
                        isClimateOn = apiStatus.isClimateOn ?: false,
                        sentryMode = apiStatus.sentryMode ?: false,
                        locked = apiStatus.locked ?: true,
                        frontLeftPsi = apiStatus.tirePressure?.frontLeft,
                        frontRightPsi = apiStatus.tirePressure?.frontRight,
                        rearLeftPsi = apiStatus.tirePressure?.rearLeft,
                        rearRightPsi = apiStatus.tirePressure?.rearRight,
                        pluggedIn = apiStatus.chargePortDoorOpen ?: false,
                        isCharging = apiStatus.isCharging,
                        isDcCharging = false, // TODO: detect DC from status
                        chargerPower = apiStatus.chargerPower,
                        chargeEnergyAdded = apiStatus.chargeEnergyAdded,
                        timeToFullChargeHours = apiStatus.timeToFullCharge,
                        chargeLimitSoc = apiStatus.chargeLimitSoc ?: 80
                    )
                    _uiState.value = _uiState.value.copy(carStatus = status, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectCar(carId: Int) {
        autoRefreshJob?.cancel()
        _uiState.value = _uiState.value.copy(selectedCarId = carId, carStatus = null)
        loadCarStatus()
    }

    override fun onCleared() {
        super.onCleared()
        statusJob?.cancel()
    }
}
