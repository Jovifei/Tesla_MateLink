package com.teslamatelink.ui.cost

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class MonthlyCost(val month: String, val acCost: Double, val dcCost: Double, val total: Double)
private data class LocationCost(val name: String, val cost: Double, val kWh: Double, val count: Int, val pricePerKwh: Double)

private val mockMonthlyCost = listOf(
    MonthlyCost("Jan", 320.0, 180.0, 500.0), MonthlyCost("Feb", 280.0, 150.0, 430.0),
    MonthlyCost("Mar", 350.0, 220.0, 570.0), MonthlyCost("Apr", 310.0, 200.0, 510.0),
    MonthlyCost("May", 380.0, 260.0, 640.0), MonthlyCost("Jun", 420.0, 300.0, 720.0)
)

private val mockLocations = listOf(
    LocationCost("Home", 1560.0, 5200.0, 52, 0.30),
    LocationCost("Supercharger Downtown", 890.0, 1400.0, 18, 0.64),
    LocationCost("Office Garage", 320.0, 1100.0, 22, 0.29),
    LocationCost("Supercharger Highway", 450.0, 700.0, 8, 0.64),
    LocationCost("Shopping Mall", 180.0, 500.0, 10, 0.36),
    LocationCost("Hotel Parking", 120.0, 400.0, 5, 0.30),
    LocationCost("Airport", 95.0, 250.0, 3, 0.38),
    LocationCost("Fast Charger East", 200.0, 300.0, 4, 0.67)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostScreen(onNavigateBack: () -> Unit) {
    val totalCost = mockMonthlyCost.sumOf { it.total }
    val totalAC = mockMonthlyCost.sumOf { it.acCost }
    val totalDC = mockMonthlyCost.sumOf { it.dcCost }
    val maxCost = mockMonthlyCost.maxOf { it.total }

    val sortedLocations = mockLocations.sortedBy { it.pricePerKwh }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charging Cost") },
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
            // Total cost cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CostSummaryCard("Total", "¥%.0f".format(totalCost), Color(0xFF3B82F6), Modifier.weight(1f))
                    CostSummaryCard("AC", "¥%.0f".format(totalAC), Color(0xFF3B82F6), Modifier.weight(1f))
                    CostSummaryCard("DC", "¥%.0f".format(totalDC), Color(0xFFF59E0B), Modifier.weight(1f))
                }
            }

            // Stacked bar chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Cost Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            val barWidth = (size.width - 30f) / mockMonthlyCost.size / 1.5f
                            val chartBottom = size.height - 20f
                            val chartHeight = chartBottom - 10f

                            mockMonthlyCost.forEachIndexed { i, m ->
                                val x = 15f + i * (barWidth * 1.5f + 4f)
                                val acHeight = (m.acCost / maxCost * chartHeight).toFloat()
                                val dcHeight = (m.dcCost / maxCost * chartHeight).toFloat()

                                // AC portion (blue)
                                if (acHeight > 0) {
                                    drawRoundRect(
                                        color = Color(0xFF3B82F6),
                                        topLeft = Offset(x, chartBottom - acHeight - dcHeight),
                                        size = Size(barWidth, acHeight + dcHeight),
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )
                                }
                                // DC portion (orange) on top
                                if (dcHeight > 0) {
                                    drawRect(
                                        color = Color(0xFFF59E0B),
                                        topLeft = Offset(x, chartBottom - dcHeight),
                                        size = Size(barWidth, dcHeight)
                                    )
                                }
                            }
                        }

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem("AC", Color(0xFF3B82F6))
                            Spacer(modifier = Modifier.size(16.dp))
                            LegendItem("DC", Color(0xFFF59E0B))
                        }
                    }
                }
            }

            // Location ranking
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Location Ranking (¥/kWh)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("#", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp))
                            Text("Location", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("¥/kWh", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                            Text("Visits", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        sortedLocations.forEachIndexed { i, loc ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${i + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                                Text(loc.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(
                                    "¥%.2f".format(loc.pricePerKwh),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (loc.pricePerKwh <= 0.35) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.End
                                )
                                Text("${loc.count}x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostSummaryCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) { drawRect(color = color) }
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
