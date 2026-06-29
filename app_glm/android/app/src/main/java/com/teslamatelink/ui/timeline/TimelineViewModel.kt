package com.teslamatelink.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teslamatelink.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class TimelineEvent(
    val id: Int,
    val time: String,
    val title: String,
    val description: String,
    val type: String, // "drive" or "charge"
    val metrics: String
)

data class TimelineUiState(
    val isLoading: Boolean = true,
    val events: List<TimelineEvent> = emptyList()
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    fun loadTimeline(carId: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val drives = carRepository.getDrives(carId)
                val charges = carRepository.getCharges(carId)

                val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFmt = SimpleDateFormat("HH:mm", Locale.US)

                val driveEvents = drives.mapIndexed { index, drive ->
                    val time = drive.startDate?.let {
                        try { outputFmt.format(inputFmt.parse(it)!!) } catch (_: Exception) { "" }
                    } ?: ""
                    val desc = buildString {
                        append(drive.startAddress ?: "Unknown")
                        append(" → ")
                        append(drive.endAddress ?: "Unknown")
                    }
                    val metrics = buildString {
                        drive.distanceKm?.let { append("%.1f km".format(it)) }
                        drive.efficiency?.let {
                            if (isNotEmpty()) append(" · ")
                            append("%.0f Wh/km".format(it))
                        }
                        drive.durationMin?.let {
                            if (isNotEmpty()) append(" · ")
                            append("$it min")
                        }
                    }
                    TimelineEvent(
                        id = drive.id,
                        time = time,
                        title = "Drive",
                        description = desc,
                        type = "drive",
                        metrics = metrics
                    )
                }

                val chargeEvents = charges.mapIndexed { index, charge ->
                    val time = charge.startDate?.let {
                        try { outputFmt.format(inputFmt.parse(it)!!) } catch (_: Exception) { "" }
                    } ?: ""
                    val desc = buildString {
                        append(charge.chargeType ?: "Charging")
                        charge.address?.let { append(" · $it") }
                    }
                    val metrics = buildString {
                        charge.chargeEnergyAdded?.let { append("+%.1f kWh".format(it)) }
                        charge.cost?.let {
                            if (isNotEmpty()) append(" · ")
                            append("$%.2f".format(it))
                        }
                    }
                    TimelineEvent(
                        id = charge.id + 10000,
                        time = time,
                        title = "Charging",
                        description = desc,
                        type = "charge",
                        metrics = metrics
                    )
                }

                val allEvents = (driveEvents + chargeEvents)
                    .sortedByDescending { it.time }
                    .take(50)

                _uiState.value = TimelineUiState(isLoading = false, events = allEvents)
            } catch (e: Exception) {
                _uiState.value = TimelineUiState(isLoading = false, events = emptyList())
            }
        }
    }
}
