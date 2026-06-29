package com.teslamatelink.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.domain.GCJ02Converter
import com.teslamatelink.ui.components.AmapComposeView
import com.teslamatelink.ui.components.CarImage
import com.teslamatelink.ui.components.MapMarker
import com.teslamatelink.util.MapUtils
import com.teslamatelink.ui.dashboard.components.BatteryCard
import com.teslamatelink.ui.dashboard.components.ChargingCard
import com.teslamatelink.ui.dashboard.components.InfoGrid
import com.teslamatelink.R
import com.teslamatelink.ui.dashboard.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDrives: () -> Unit,
    onNavigateToCharges: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToUpdates: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToHeatmap: () -> Unit = {},
    onNavigateToEfficiency: () -> Unit = {},
    onNavigateToVampire: () -> Unit = {},
    onNavigateToRange: () -> Unit = {},
    onNavigateToCost: () -> Unit = {},
    onNavigateToDestinations: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCarSwitcher by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showCarSwitcher = true }
                    ) {
                        Text(
                            text = state.carStatus?.displayName ?: "Tesla MateLink",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cd_switch_car),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                    // Car switcher dropdown
                    DropdownMenu(
                        expanded = showCarSwitcher,
                        onDismissRequest = { showCarSwitcher = false }
                    ) {
                        state.cars.forEach { car ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(car.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${car.model} · ${car.totalDrives} drives",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectCar(car.id)
                                    showCarSwitcher = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.carStatus?.let { status ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = status.displayName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        StatusBadge(state = status.state)
                    }

                    // Car image
                    CarImage(
                        exteriorColor = state.cars.find { it.id == state.selectedCarId }?.exteriorColor,
                        isDark = isSystemInDarkTheme()
                    )

                    // Location (TG-06)
                    LocationCard(
                        latitude = status.latitude,
                        longitude = status.longitude,
                        address = null
                    )

                    // Battery
                    BatteryCard(
                        batteryLevel = status.batteryLevel,
                        rangeKm = status.estBatteryRangeKm,
                        chargeLimit = status.chargeLimitSoc,
                        isCharging = status.isCharging,
                        onClick = onNavigateToBattery
                    )

                    // Charging
                    if (status.isCharging && status.chargerPower != null) {
                        ChargingCard(
                            powerKw = status.chargerPower,
                            addedKwh = status.chargeEnergyAdded ?: 0.0,
                            remainingHours = status.timeToFullChargeHours ?: 0.0
                        )
                    }

                    // Info grid
                    InfoGrid(
                        odometer = status.odometer,
                        location = stringResource(R.string.location_home),
                        insideTemp = status.insideTemp,
                        outsideTemp = status.outsideTemp,
                        frontLeftPsi = status.frontLeftPsi,
                        frontRightPsi = status.frontRightPsi,
                        rearLeftPsi = status.rearLeftPsi,
                        rearRightPsi = status.rearRightPsi
                    )

                    // Quick navigation chips — row 1 (core)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.nav_quick_access),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavChip("Drives", onClick = onNavigateToDrives)
                        QuickNavChip("Charges", onClick = onNavigateToCharges)
                        QuickNavChip("Battery", onClick = onNavigateToBattery)
                        QuickNavChip("Updates", onClick = onNavigateToUpdates)
                    }

                    // Quick navigation chips — row 2 (analytics)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavChip("Stats", onClick = onNavigateToStatistics)
                        QuickNavChip("Heatmap", onClick = onNavigateToHeatmap)
                        QuickNavChip("Efficiency", onClick = onNavigateToEfficiency)
                        QuickNavChip("Vampire", onClick = onNavigateToVampire)
                    }

                    // Quick navigation chips — row 3 (more analytics)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavChip("Range", onClick = onNavigateToRange)
                        QuickNavChip("Cost", onClick = onNavigateToCost)
                        QuickNavChip("Places", onClick = onNavigateToDestinations)
                        QuickNavChip("Timeline", onClick = onNavigateToTimeline)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationCard(latitude: Double?, longitude: Double?, address: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.location_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (latitude != null && longitude != null) {
                if (MapUtils.isChineseLocale()) {
                    val context = LocalContext.current
                    if (MapUtils.isAmapKeyConfigured(context)) {
                        AmapComposeView(
                            lat = latitude,
                            lng = longitude,
                            markers = listOf(
                                MapMarker(
                                    lat = latitude, lng = longitude, title = "Tesla", snippet = address
                                )
                            ),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        Text(
                            stringResource(R.string.map_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    val (gcjLat, gcjLng) = GCJ02Converter.wgs84ToGcj02(latitude, longitude)
                    Text(
                        "WGS-84: %.6f, %.6f".format(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "GCJ-02: %.6f, %.6f".format(gcjLat, gcjLng),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!address.isNullOrBlank()) {
                        Text(
                            address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    stringResource(R.string.no_location_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickNavChip(
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) }
    )
}
