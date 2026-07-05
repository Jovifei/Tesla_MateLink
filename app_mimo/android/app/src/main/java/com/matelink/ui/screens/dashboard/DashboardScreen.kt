package com.matelink.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelink.ui.components.AmapPointView
import com.matelink.ui.theme.StatusSuccess
import com.matelink.ui.theme.StatusWarning
import com.matelink.ui.theme.SwissInk
import com.matelink.ui.theme.SwissMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    intent: android.content.Intent? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCharges: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToDrives: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToBattery: (carId: Int, efficiency: Double?, exteriorColor: String?) -> Unit = { _, _, _ -> },
    onNavigateToMileage: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToUpdates: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToStats: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToCurrentCharge: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToWhereWasI: (carId: Int, timestamp: String, exteriorColor: String?) -> Unit = { _, _, _ -> },
    onNavigateToSentryHistory: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToTrips: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val status = uiState.status
    val car = uiState.car

    if (status == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data available")
        }
        return
    }

    val carId = car?.carId ?: 1
    val exteriorColor = car?.carExterior?.exteriorColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = car?.displayName ?: "My Tesla",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StateBadge(status.state ?: "offline")
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }

        // Battery card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigateToBattery(carId, car?.carDetails?.efficiency, exteriorColor)
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Battery", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${status.batteryLevel ?: 0}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("${(status.ratedBatteryRangeKm ?: status.idealBatteryRangeKm ?: status.estBatteryRangeKm ?: 0.0).toInt()} km range", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (status.batteryLevel ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
                if ((status.chargeLimitSoc ?: 0) > 0) {
                    Text("Limit: ${status.chargeLimitSoc ?: 0}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if ((status.chargeLimitSoc ?: 0) > 90) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ High charge level - consider reducing to 80-90% for daily use",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFB8C00)
                    )
                }
            }
        }

        // Info cards row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "Odometer",
                value = "${String.format("%,.0f", status.odometer ?: 0.0)} km",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToMileage(carId, exteriorColor) }
            )
            InfoCard(
                title = "Location",
                value = "${String.format("%.4f", status.latitude ?: 0.0)}, ${String.format("%.4f", status.longitude ?: 0.0)}\nElevation: ${status.elevation ?: 0}m",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToDrives(carId, exteriorColor) }
            )
        }

        // Location Map
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val ts = status.stateSince
                    if (!ts.isNullOrBlank()) {
                        onNavigateToWhereWasI(carId, ts, exteriorColor)
                    }
                }
        ) {
            AmapPointView(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                latitude = status.latitude ?: 0.0,
                longitude = status.longitude ?: 0.0,
                title = car?.displayName ?: "Vehicle"
            )
        }

        // Temperature + Status cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard("Inside", "${status.insideTemp ?: 0.0}°C", Modifier.weight(1f))
            InfoCard("Outside", "${status.outsideTemp ?: 0.0}°C", Modifier.weight(1f))
            InfoCard("Lock", if (status.locked == true) "🔒 Locked" else "🔓 Unlocked", Modifier.weight(1f))
            InfoCard("Plug", if (status.pluggedIn == true) "⚡ Plugged" else "Not Plugged", Modifier.weight(1f))
        }

        // Status row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("🔒", if (status.locked == true) "Locked" else "Unlocked", status.locked == true)
            StatusChip("⚡", if (status.pluggedIn == true) "Plugged" else "Unplugged", status.pluggedIn == true)
            StatusChip("💨", if (status.isClimateOn == true) "Climate ON" else "Climate OFF", status.isClimateOn == true)
            StatusChip("🛡️", if (status.sentryMode == true) "Sentry" else "Sentry OFF", status.sentryMode == true)
        }

        // Tire pressure
        Text("Tire Pressure", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard("FL", "${status.tpmsDetails?.pressureFl ?: 0.0} bar", Modifier.weight(1f))
            InfoCard("FR", "${status.tpmsDetails?.pressureFr ?: 0.0} bar", Modifier.weight(1f))
            InfoCard("RL", "${status.tpmsDetails?.pressureRl ?: 0.0} bar", Modifier.weight(1f))
            InfoCard("RR", "${status.tpmsDetails?.pressureRr ?: 0.0} bar", Modifier.weight(1f))
        }

        // 7-Day Battery Trend
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigateToStats(carId, exteriorColor)
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7-Day Battery Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BatteryTrendChart(currentBatteryLevel = status.batteryLevel ?: 0)
            }
        }

        // Charging card
        if (status.isCharging) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToCurrentCharge(carId, exteriorColor)
                    },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ Charging", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Power"); Text("${status.chargerPower ?: 0} kW", fontWeight = FontWeight.Bold) }
                        Column { Text("Added"); Text("${status.chargeEnergyAdded ?: 0.0} kWh", fontWeight = FontWeight.Bold) }
                        Column { Text("Remaining"); Text("${status.timeToFullCharge ?: 0.0}h", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StateBadge(state: String) {
    val (color, label) = when (state) {
        "online" -> StatusSuccess to "Online"
        "driving" -> SwissInk to "Driving"
        "charging" -> StatusWarning to "Charging"
        "asleep" -> SwissMuted to "Asleep"
        else -> SwissMuted to "Offline"
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, color = Color.White) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = color)
    )
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatusChip(icon: String, label: String, active: Boolean) {
    SuggestionChip(
        onClick = {},
        label = { Text("$icon $label", style = MaterialTheme.typography.bodySmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun BatteryTrendChart(currentBatteryLevel: Int) {
    // TODO: replace with real 7-day history from API when available
    // Generate plausible 7-day mock anchored to the actual current battery level
    val data = remember(currentBatteryLevel) {
        listOf(
            (currentBatteryLevel - 6).coerceIn(0, 100),
            (currentBatteryLevel - 5).coerceIn(0, 100),
            (currentBatteryLevel - 8).coerceIn(0, 100),
            (currentBatteryLevel - 4).coerceIn(0, 100),
            (currentBatteryLevel - 3).coerceIn(0, 100),
            (currentBatteryLevel - 1).coerceIn(0, 100),
            currentBatteryLevel.coerceIn(0, 100)
        )
    }
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxVal = data.max()
    val minVal = data.min()
    val range = (maxVal - minVal).coerceAtLeast(1)

    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)
        val padding = 20f

        // Draw line
        val path = androidx.compose.ui.graphics.Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - padding - ((value - minVal) / range.toFloat()) * (height - 2 * padding)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = MaterialTheme.colorScheme.primary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))

        // Draw dots
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - padding - ((value - minVal) / range.toFloat()) * (height - 2 * padding)
            drawCircle(color = MaterialTheme.colorScheme.primary, radius = 5f, center = androidx.compose.ui.geometry.Offset(x, y))
        }
    }

    // Labels
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
