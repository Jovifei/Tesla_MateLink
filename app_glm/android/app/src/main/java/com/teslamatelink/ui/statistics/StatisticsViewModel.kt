package com.teslamatelink.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teslamatelink.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MonthSummary(
    val name: String,
    val year: Int,
    val month: Int,
    val km: Int,
    val kwh: Double,
    val drives: Int,
    val eff: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val months: List<MonthSummary> = emptyList(),
    val error: String? = null
) {
    val totalKm: Int get() = months.sumOf { it.km }
    val totalKwh: Double get() = months.sumOf { it.kwh }
    val totalDrives: Int get() = months.sumOf { it.drives }
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init { loadStatistics() }

    fun loadStatistics(carId: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val drives = carRepository.getDrives(carId)

                val monthGroups = drives
                    .filter { it.startDate != null }
                    .groupBy { drive ->
                        val date = parseDate(drive.startDate!!)
                        YearMonth.of(date.year, date.monthValue)
                    }

                val dateFormat = DateTimeFormatter.ofPattern("MMM")
                val monthSummaries = monthGroups.entries
                    .sortedByDescending { it.key }
                    .map { (yearMonth, monthDrives) ->
                        val totalDist = monthDrives.sumOf { it.distanceKm ?: 0.0 }
                        val totalEnergy = monthDrives.sumOf { it.consumptionKwh ?: 0.0 }
                        val avgEff = if (totalDist > 0) ((totalEnergy * 1000.0) / totalDist).toInt() else 0

                        MonthSummary(
                            name = yearMonth.format(dateFormat),
                            year = yearMonth.year,
                            month = yearMonth.monthValue,
                            km = totalDist.toInt(),
                            kwh = totalEnergy,
                            drives = monthDrives.size,
                            eff = avgEff
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    months = monthSummaries,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (_: Exception) {
            try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                LocalDate.now()
            }
        }
    }
}
