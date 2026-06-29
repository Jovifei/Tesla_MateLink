package com.teslamatelink.ui.charges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teslamatelink.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChargeItem(
    val id: Int,
    val date: String,
    val address: String,
    val isDc: Boolean,
    val energyKwh: Double,
    val cost: Double,
    val durationMinutes: Int,
    val startBattery: Int,
    val endBattery: Int,
    val maxPower: Double,
    val avgPower: Double,
    val efficiency: Double
)

data class ChargeUiState(
    val isLoading: Boolean = true,
    val charges: List<ChargeItem> = emptyList(),
    val isDcFilter: Boolean? = null, // null = all, true = DC, false = AC
    val selectedCharge: ChargeItem? = null,
    val error: String? = null
)

@HiltViewModel
class ChargeViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChargeUiState())
    val uiState: StateFlow<ChargeUiState> = _uiState.asStateFlow()

    init { loadCharges() }

    fun loadCharges(carId: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val charges = carRepository.getCharges(carId)
                val items = charges.map { charge ->
                    ChargeItem(
                        id = charge.id,
                        date = charge.startDate ?: "",
                        address = charge.address ?: "Unknown",
                        isDc = charge.chargeType.equals("dc", ignoreCase = true),
                        energyKwh = charge.chargeEnergyAdded ?: 0.0,
                        cost = charge.cost ?: 0.0,
                        durationMinutes = 0, // TODO: calculate from start/end date
                        startBattery = charge.startBatteryLevel ?: 0,
                        endBattery = charge.endBatteryLevel ?: 0,
                        maxPower = 0.0, // TODO: extract from API
                        avgPower = 0.0, // TODO: extract from API
                        efficiency = 0.0 // TODO: calculate
                    )
                }
                _uiState.value = _uiState.value.copy(charges = items, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load charges"
                )
            }
        }
    }

    fun setDcFilter(isDc: Boolean?) {
        _uiState.value = _uiState.value.copy(isDcFilter = isDc)
    }

    fun loadChargeDetail(chargeId: Int) {
        viewModelScope.launch {
            val charge = _uiState.value.charges.find { it.id == chargeId }
            _uiState.value = _uiState.value.copy(selectedCharge = charge)
        }
    }
}
