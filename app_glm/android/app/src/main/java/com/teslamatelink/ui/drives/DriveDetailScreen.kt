package com.teslamatelink.ui.drives

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.teslamatelink.ui.theme.StateOnline

private val chartTabs = listOf("Speed", "Power", "Altitude", "Temp", "Tires")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveDetailScreen(
    driveId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DriveViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(driveId) { viewModel.loadDriveDetail(driveId) }

    val drive = state.selectedDrive

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drive Detail") },
                navigationIcon = { IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                } }
            )
        }
    ) { padding ->
        if (drive == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(drive.startAddress, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("→ ${drive.endAddress}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Stat Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Distance", "%.1f km".format(drive.distanceKm))
                    StatItem("Duration", formatDuration(drive.durationMinutes))
                    StatItem("Avg Speed", "%.0f km/h".format(drive.avgSpeed))
                    StatItem("Efficiency", "%.0f Wh/km".format(drive.efficiency))
                }
            }

            // Battery bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Battery", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(64.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { drive.batteryStart / 100f },
                        modifier = Modifier.weight(1f).height(8.dp).padding(end = 8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = StateOnline
                    )
                    Text("${drive.batteryStart}%", style = MaterialTheme.typography.labelMedium)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(start = 80.dp, end = 16.dp, bottom = 16.dp)) {
                    Text("→ ${drive.batteryEnd}%", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Tab picker
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chartTabs.forEachIndexed { i, label ->
                    FilterChip(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // LineChart
            Card(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                AndroidView(
                    factory = { ctx ->
                        LineChart(ctx).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(false)
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            axisLeft.setDrawGridLines(false)
                            axisRight.isEnabled = false
                            legend.isEnabled = false

                            val entries = generateChartData(selectedTab)
                            val dataSet = LineDataSet(entries, chartTabs[selectedTab]).apply {
                                color = com.teslamatelink.ui.theme.StateOnline.toArgb()
                                setCircleColor(com.teslamatelink.ui.theme.StateOnline.toArgb())
                                lineWidth = 2f
                                circleRadius = 3f
                                setDrawValues(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                            }
                            data = LineData(dataSet)
                            invalidate()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun generateChartData(tab: Int): List<Entry> {
    val baseValues = when (tab) {
        0 -> listOf(0f, 45f, 80f, 65f, 50f, 90f, 70f, 55f, 40f, 30f,
            60f, 75f, 85f, 50f, 35f, 20f, 55f, 70f, 45f, 30f,
            65f, 80f, 60f, 40f, 25f, 50f, 75f, 55f, 35f, 0f) // Speed
        1 -> listOf(0f, 20f, 60f, 40f, 30f, 80f, 110f, 50f, 35f, 25f,
            55f, 90f, 120f, 70f, 40f, 15f, 45f, 85f, 50f, 20f,
            60f, 95f, 75f, 45f, 20f, 40f, 70f, 55f, 25f, 0f) // Power
        2 -> listOf(50f, 80f, 150f, 200f, 300f, 350f, 420f, 380f, 250f, 180f,
            120f, 90f, 150f, 220f, 310f, 400f, 450f, 350f, 200f, 100f,
            80f, 120f, 180f, 250f, 320f, 380f, 300f, 200f, 100f, 50f) // Altitude
        3 -> listOf(22f, 23f, 24f, 25f, 26f, 27f, 26f, 25f, 24f, 23f,
            22f, 21f, 22f, 23f, 24f, 25f, 26f, 25f, 24f, 23f,
            22f, 21f, 20f, 21f, 22f, 23f, 24f, 23f, 22f, 21f) // Temp
        4 -> listOf(2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.6f, 2.6f, 2.6f, 2.6f, 2.5f,
            2.5f, 2.5f, 2.5f, 2.5f, 2.6f, 2.6f, 2.6f, 2.6f, 2.5f, 2.5f,
            2.5f, 2.5f, 2.5f, 2.5f, 2.6f, 2.6f, 2.6f, 2.5f, 2.5f, 2.5f) // Tires
        else -> listOf()
    }
    return baseValues.mapIndexed { i, v -> Entry(i.toFloat(), v) }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
