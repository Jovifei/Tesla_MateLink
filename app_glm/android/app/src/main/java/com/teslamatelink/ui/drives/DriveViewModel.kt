package com.teslamatelink.ui.drives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teslamatelink.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriveItem(
    val id: Int,
    val startDate: String,
    val startAddress: String,
    val endAddress: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val avgSpeed: Double,
    val efficiency: Double,
    val maxPower: Double,
    val maxAltitude: Double,
    val insideTemp: Double?,
    val outsideTemp: Double?,
    val batteryStart: Int,
    val batteryEnd: Int
)

data class DriveUiState(
    val isLoading: Boolean = true,
    val drives: List<DriveItem> = emptyList(),
    val selectedDrive: DriveItem? = null,
    val error: String? = null
)

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriveUiState())
    val uiState: StateFlow<DriveUiState> = _uiState.asStateFlow()

    init { loadDrives() }

    fun loadDrives(carId: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val drives = carRepository.getDrives(carId)
                val items = drives.map { drive ->
                    DriveItem(
                        id = drive.id,
                        startDate = drive.startDate ?: "",
                        startAddress = drive.startAddress ?: "Unknown",
                        endAddress = drive.endAddress ?: "Unknown",
                        distanceKm = drive.distanceKm ?: 0.0,
                        durationMinutes = drive.durationMin ?: 0,
                        avgSpeed = if ((drive.durationMin ?: 0) > 0)
                            (drive.distanceKm ?: 0.0) / (drive.durationMin!! / 60.0) else 0.0,
                        efficiency = drive.efficiency ?: 0.0,
                        maxPower = 0.0, // TODO: extract from API when available
                        maxAltitude = 0.0, // TODO: extract from API when available
                        insideTemp = null,
                        outsideTemp = drive.outsideTempAvg,
                        batteryStart = drive.startBatteryLevel ?: 0,
                        batteryEnd = drive.endBatteryLevel ?: 0
                    )
                }
                _uiState.value = _uiState.value.copy(drives = items, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load drives"
                )
            }
        }
    }

    fun loadDriveDetail(driveId: Int) {
        viewModelScope.launch {
            val drive = _uiState.value.drives.find { it.id == driveId }
            _uiState.value = _uiState.value.copy(selectedDrive = drive)
        }
    }
}
