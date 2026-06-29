package com.teslamatelink.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.teslamatelink.data.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

enum class StepStatus { PENDING, ACTIVE, OK, FAIL }

data class StepState(
    val label: String,
    val status: StepStatus = StepStatus.PENDING
)

data class OnboardingUiState(
    val serverUrl: String = "",
    val apiToken: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isConnected: Boolean = false,
    val currentStep: String? = null,
    val steps: List<StepState> = listOf(
        StepState("Ping server"),
        StepState("Check readiness"),
        StepState("Fetch cars")
    )
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    companion object {
        private const val STEP_PING = "Pinging server..."
        private const val STEP_READYZ = "Checking server readiness..."
        private const val STEP_CARS = "Fetching cars..."
    }

    private fun updateStep(index: Int, status: StepStatus) {
        _uiState.value = _uiState.value.copy(
            steps = _uiState.value.steps.mapIndexed { i, step ->
                if (i == index) step.copy(status = status) else step
            }
        )
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url, errorMessage = null)
    }

    fun updateApiToken(token: String) {
        _uiState.value = _uiState.value.copy(apiToken = token, errorMessage = null)
    }

    fun testConnection() {
        val current = _uiState.value
        if (current.serverUrl.isBlank() || current.apiToken.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Server URL and API Token are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, errorMessage = null, isConnected = false,
                steps = listOf(
                    StepState("Ping server"),
                    StepState("Check readiness"),
                    StepState("Fetch cars")
                )
            )
            try {
                val baseUrl = current.serverUrl.trimEnd('/')

                // Step 1: ping
                _uiState.value = _uiState.value.copy(currentStep = STEP_PING)
                updateStep(0, StepStatus.ACTIVE)
                val pingOk = withContext(Dispatchers.IO) {
                    val req = Request.Builder().url("$baseUrl/api/ping").build()
                    okHttpClient.newCall(req).execute().isSuccessful
                }
                if (!pingOk) {
                    updateStep(0, StepStatus.FAIL)
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = "Cannot reach server. Check URL")
                    return@launch
                }
                updateStep(0, StepStatus.OK)

                // Step 2: readyz
                _uiState.value = _uiState.value.copy(currentStep = STEP_READYZ)
                updateStep(1, StepStatus.ACTIVE)
                val readyzOk = withContext(Dispatchers.IO) {
                    val req = Request.Builder().url("$baseUrl/api/readyz").build()
                    okHttpClient.newCall(req).execute().isSuccessful
                }
                if (!readyzOk) {
                    updateStep(1, StepStatus.FAIL)
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = "Server not ready (DB/MQTT down)")
                    return@launch
                }
                updateStep(1, StepStatus.OK)

                // Step 3: cars (validates token)
                _uiState.value = _uiState.value.copy(currentStep = STEP_CARS)
                updateStep(2, StepStatus.ACTIVE)
                val carsResponse = withContext(Dispatchers.IO) {
                    val req = Request.Builder().url("$baseUrl/api/v1/cars")
                        .header("Authorization", "Bearer ${current.apiToken}")
                        .build()
                    okHttpClient.newCall(req).execute()
                }
                if (carsResponse.code == 401) {
                    updateStep(2, StepStatus.FAIL)
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = "Invalid token (401)")
                    return@launch
                }
                if (!carsResponse.isSuccessful) {
                    updateStep(2, StepStatus.FAIL)
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = "Server error: ${carsResponse.code}")
                    return@launch
                }
                val bodyStr = carsResponse.body?.string()
                if (bodyStr.isNullOrBlank() || bodyStr.contains("\"cars\":[]") || bodyStr.contains("\"cars\":null")) {
                    updateStep(2, StepStatus.FAIL)
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = "No cars found on this server")
                    return@launch
                }
                updateStep(2, StepStatus.OK)

                _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, isConnected = true, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, currentStep = null, errorMessage = e.message ?: "Connection failed")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /** Returns true if we've already asked for POST_NOTIFICATIONS. */
    suspend fun hasAskedNotificationPermission(): Boolean {
        return settingsDataStore.notificationPermissionAsked.first()
    }

    /** Persist that we've asked for POST_NOTIFICATIONS. */
    suspend fun markNotificationPermissionAsked() {
        settingsDataStore.saveNotificationPermissionAsked()
    }
}
