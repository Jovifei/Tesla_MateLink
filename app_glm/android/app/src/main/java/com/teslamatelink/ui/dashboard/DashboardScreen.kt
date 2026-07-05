package com.teslamatelink.ui.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.ui.components.ChipStatus
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.components.StitchDataRow
import com.teslamatelink.ui.components.StitchStatusChip
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

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
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showCarSwitcher = true }
                    ) {
                        Text(
                            text = state.carStatus?.displayName ?: "Tesla MateLink",
                            color = StitchColors.OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.24).sp
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Switch car",
                            tint = StitchColors.OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    val isOnline = state.carStatus?.state == "online"
                    StitchStatusChip(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        status = if (isOnline) ChipStatus.ONLINE else ChipStatus.OFFLINE
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = StitchColors.OnSurface
                        )
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
                                        Text(
                                            car.name,
                                            color = StitchColors.OnSurface,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "${car.model} · ${car.totalDrives} drives",
                                            color = StitchColors.OnSurfaceVariant,
                                            fontSize = 14.sp
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
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StitchColors.OnSurface)
            }
        } else {
            state.carStatus?.let { status ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(0.dp))

                    // ── Battery Card ─────────────────────────────────────────
                    StitchCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToBattery)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "BATTERY",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${status.batteryLevel}%",
                                    color = StitchColors.OnSurface,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Text(
                                text = "${status.estBatteryRangeKm.toInt()} km",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = JetBrainsMonoFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { status.batteryLevel / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = StitchColors.OnSurface,
                            trackColor = StitchColors.Border,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Limit: ${status.chargeLimitSoc}%",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 12.sp,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "${status.odometer.toInt()} km",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 12.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    // ── Charging Card (conditional) ──────────────────────────
                    if (status.isCharging && status.chargerPower != null) {
                        StitchCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToCharges)
                        ) {
                            Text(
                                text = "CHARGING",
                                color = StitchColors.StatusCharging,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Power",
                                        color = StitchColors.OnSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "${status.chargerPower?.toInt() ?: 0} kW",
                                        color = StitchColors.OnSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Added",
                                        color = StitchColors.OnSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "${String.format("%.1f", status.chargeEnergyAdded ?: 0.0)} kWh",
                                        color = StitchColors.OnSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Remaining",
                                        color = StitchColors.OnSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "${String.format("%.1f", status.timeToFullChargeHours ?: 0.0)}h",
                                        color = StitchColors.OnSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                            }
                        }
                    }

                    // ── Climate Card ─────────────────────────────────────────
                    StitchCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CLIMATE",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Inside",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.insideTemp?.let { "${String.format("%.1f", it)}°C" } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Outside",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.outsideTemp?.let { "${String.format("%.1f", it)}°C" } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Climate",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                StitchStatusChip(
                                    text = if (status.isClimateOn) "ON" else "OFF",
                                    status = if (status.isClimateOn) ChipStatus.ONLINE else ChipStatus.OFFLINE
                                )
                            }
                        }
                    }

                    // ── Status Card ──────────────────────────────────────────
                    StitchCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STATUS",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "LOCK",
                            value = if (status.locked) "Locked" else "Unlocked"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "SENTRY",
                            value = if (status.sentryMode) "Active" else "Off"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "PLUG",
                            value = if (status.pluggedIn) "Connected" else "Disconnected"
                        )
                    }

                    // ── Tire Pressure Card ───────────────────────────────────
                    StitchCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TIRE PRESSURE",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "FL",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.frontLeftPsi?.let { String.format("%.1f", it) } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "FR",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.frontRightPsi?.let { String.format("%.1f", it) } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "RL",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.rearLeftPsi?.let { String.format("%.1f", it) } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "RR",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "${status.rearRightPsi?.let { String.format("%.1f", it) } ?: "—"}",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily
                                )
                            }
                        }
                    }

                    // ── Quick Navigation ─────────────────────────────────────
                    StitchCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "QUICK ACCESS",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StitchDataRow(
                            label = "DRIVES",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToDrives)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "CHARGES",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToCharges)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "BATTERY",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToBattery)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "UPDATES",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToUpdates)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "STATISTICS",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToStatistics)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StitchDataRow(
                            label = "EFFICIENCY",
                            value = "→",
                            modifier = Modifier.clickable(onClick = onNavigateToEfficiency)
                        )
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
