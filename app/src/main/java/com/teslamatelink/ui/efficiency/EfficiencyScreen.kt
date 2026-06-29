package com.teslamatelink.ui.efficiency

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class EffPoint(val speed: Int, val eff: Int, val temp: Int, val date: String)
private data class SpeedZone(val label: String, val min: Int, val max: Int)

private val speedZones = listOf(
    SpeedZone("0-30", 0, 30), SpeedZone("30-60", 30, 60),
    SpeedZone("60-90", 60, 90), SpeedZone("90-120", 90, 120),
    SpeedZone("120+", 120, 999)
)

private val mockPoints = listOf(
    EffPoint(25, 120, 22, "2026-06-20"), EffPoint(45, 140, 25, "2026-06-19"),
    EffPoint(65, 165, 28, "2026-06-18"), EffPoint(80, 180, 30, "2026-06-17"),
    EffPoint(95, 195, 26, "2026-06-16"), EffPoint(110, 215, 22, "2026-06-15"),
    EffPoint(130, 250, 20, "2026-06-14"), EffPoint(55, 155, 18, "2026-06-13"),
    EffPoint(75, 170, 15, "2026-06-12"), EffPoint(85, 185, 12, "2026-06-11"),
    EffPoint(100, 200, 8, "2026-06-10"), EffPoint(35, 130, 5, "2026-06-09"),
    EffPoint(50, 148, 0, "2026-06-08"), EffPoint(70, 168, -2, "2026-06-07"),
    EffPoint(90, 190, 10, "2026-06-06"), EffPoint(115, 225, 16, "2026-06-05"),
    EffPoint(40, 135, 30, "2026-06-04"), EffPoint(60, 160, 28, "2026-06-03"),
    EffPoint(105, 208, 24, "2026-06-02"), EffPoint(120, 240, 18, "2026-06-01")
)

private fun tempColor(temp: Int): Color = when {
    temp < 0 -> Color(0xFF3B82F6)
    temp < 15 -> Color(0xFF10B981)
    temp < 25 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfficiencyScreen(onNavigateBack: () -> Unit) {
    val zoneStats = speedZones.map { zone ->
        val pts = mockPoints.filter { it.speed in zone.min until zone.max }
        SpeedZoneStat(zone.label, pts.size, if (pts.isNotEmpty()) pts.map { it.eff }.average().toInt() else 0)
    }

    val maxSpeed = mockPoints.maxOf { it.speed }
    val maxEff = mockPoints.maxOf { it.eff }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Efficiency Curve") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scatter chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Speed vs Efficiency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                            val chartLeft = 40f
                            val chartBottom = size.height - 20f
                            val chartWidth = size.width - chartLeft - 10f
                            val chartHeight = chartBottom - 10f

                            // Grid lines
                            listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                                val y = chartBottom - chartHeight * frac
                                drawLine(Color.Gray.copy(alpha = 0.2f), Offset(chartLeft, y), Offset(size.width - 10f, y))
                            }

                            // Points
                            mockPoints.forEach { p ->
                                val x = chartLeft + (p.speed.toFloat() / maxSpeed) * chartWidth
                                val y = chartBottom - (p.eff.toFloat() / maxEff) * chartHeight
                                drawCircle(tempColor(p.temp).copy(alpha = 0.7f), 6f, Offset(x, y))
                            }

                            // Axis labels
                            val labelColor = Color.Gray
                            drawLine(labelColor, Offset(chartLeft, chartBottom), Offset(size.width - 10f, chartBottom))
                            drawLine(labelColor, Offset(chartLeft, 10f), Offset(chartLeft, chartBottom))
                        }
                    }
                }
            }

            // Temperature legend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val legendItems = listOf(
                        "<0°C" to Color(0xFF3B82F6),
                        "0-15°C" to Color(0xFF10B981),
                        "15-25°C" to Color(0xFFF59E0B),
                        ">25°C" to Color(0xFFEF4444)
                    )
                    legendItems.forEach { (label, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(color = color)
                            }
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Speed zone table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Speed Zone Analysis",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Zone", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Drives", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text("Avg Eff", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        zoneStats.forEach { zone ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${zone.label} km/h", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("${zone.count}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text("${zone.avgEff} Wh/km", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SpeedZoneStat(val label: String, val count: Int, val avgEff: Int)
