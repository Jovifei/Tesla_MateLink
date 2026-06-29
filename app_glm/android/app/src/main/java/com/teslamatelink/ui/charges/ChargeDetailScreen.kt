package com.teslamatelink.ui.charges

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.teslamatelink.ui.theme.AcChargeGreen
import com.teslamatelink.ui.theme.DcChargeOrange
import com.teslamatelink.ui.theme.StateOnline

private val chartTabs = listOf("Power", "Voltage", "Temp")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeDetailScreen(
    chargeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ChargeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(chargeId) { viewModel.loadChargeDetail(chargeId) }

    val charge = state.selectedCharge ?: return

    val badgeColor = if (charge.isDc) DcChargeOrange else AcChargeGreen
    val badgeLabel = if (charge.isDc) "DC Fast Charge" else "AC Charge"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charge Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
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
                colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ElectricBolt, contentDescription = null,
                        tint = badgeColor, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(charge.address, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = badgeLabel,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Stat grid
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Energy", "%.1f kWh".format(charge.energyKwh))
                    StatItem("Cost", "$%.2f".format(charge.cost))
                    StatItem("Efficiency", "%.0f Wh/km".format(charge.efficiency))
                    StatItem("Battery", "${charge.startBattery}→${charge.endBattery}%")
                }
            }

            // Detail stats
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Max Power", "%.0f kW".format(charge.maxPower))
                    StatItem("Avg Power", "%.0f kW".format(charge.avgPower))
                    StatItem("Duration", formatDuration(charge.durationMinutes))
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
                            val entries = generateChargeChartData(selectedTab)
                            val dataSet = LineDataSet(entries, chartTabs[selectedTab]).apply {
                                color = StateOnline.toArgb()
                                setCircleColor(StateOnline.toArgb())
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
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun generateChargeChartData(tab: Int): List<Entry> {
    val baseValues = when (tab) {
        0 -> listOf(0f, 20f, 60f, 90f, 110f, 115f, 118f, 120f, 118f, 115f,
            110f, 100f, 90f, 80f, 70f, 60f, 50f, 40f, 30f, 20f,
            15f, 12f, 10f, 8f, 6f, 5f, 4f, 3f, 2f, 0f) // Power
        1 -> listOf(230f, 232f, 235f, 238f, 240f, 242f, 245f, 248f, 250f, 252f,
            250f, 248f, 246f, 244f, 242f, 240f, 238f, 236f, 234f, 232f,
            230f, 228f, 226f, 224f, 222f, 220f, 218f, 216f, 214f, 212f) // Voltage
        2 -> listOf(22f, 23f, 24f, 25f, 26f, 27f, 28f, 29f, 30f, 31f,
            31f, 30f, 29f, 28f, 27f, 26f, 25f, 24f, 23f, 22f,
            21f, 20f, 20f, 21f, 22f, 23f, 24f, 25f, 26f, 26f) // Temp
        else -> listOf()
    }
    return baseValues.mapIndexed { i, v -> Entry(i.toFloat(), v) }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
