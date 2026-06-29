package com.teslamatelink.ui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class MonthStat(
    val name: String, val km: Int, val kWh: Double,
    val drives: Int, val efficiency: Int
)

private val mockMonths = listOf(
    MonthStat("Jan", 1240, 210.5, 18, 170),
    MonthStat("Feb", 980, 166.6, 14, 168),
    MonthStat("Mar", 1560, 280.8, 22, 180),
    MonthStat("Apr", 1430, 257.4, 20, 176),
    MonthStat("May", 1890, 340.2, 26, 182),
    MonthStat("Jun", 2100, 399.0, 30, 190),
    MonthStat("Jul", 2350, 470.0, 32, 200),
    MonthStat("Aug", 2210, 419.9, 29, 192),
    MonthStat("Sep", 1780, 302.6, 24, 174),
    MonthStat("Oct", 1450, 246.5, 19, 170),
    MonthStat("Nov", 1120, 190.4, 16, 168),
    MonthStat("Dec", 1340, 241.2, 20, 178)
)

private data class DailyStat(val day: String, val km: Double, val kWh: Double, val drives: Int)

private val mockDailyStats = mapOf(
    "Jun" to listOf(
        DailyStat("Jun 1", 85.0, 14.5, 2),
        DailyStat("Jun 2", 62.0, 10.5, 1),
        DailyStat("Jun 3", 120.0, 21.6, 3),
        DailyStat("Jun 4", 45.0, 7.7, 1),
        DailyStat("Jun 5", 78.0, 13.3, 2),
        DailyStat("Jun 6", 95.0, 16.2, 2),
        DailyStat("Jun 7", 110.0, 19.8, 3)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedMonth by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
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
            // Summary cards
            item {
                val totalKm = mockMonths.sumOf { it.km }
                val totalKwh = mockMonths.sumOf { it.kWh }
                val totalDrives = mockMonths.sumOf { it.drives }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard("Total km", "${totalKm} km", Modifier.weight(1f))
                    SummaryCard("Total kWh", "%.0f kWh".format(totalKwh), Modifier.weight(1f))
                    SummaryCard("Drives", "$totalDrives", Modifier.weight(1f))
                }
            }

            // Month selection grid
            item {
                Text(
                    text = if (selectedMonth != null) "Tap month for daily view" else "Select a month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedMonth == null) {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mockMonths) { month ->
                            MonthCard(
                                month = month,
                                onClick = { selectedMonth = month.name }
                            )
                        }
                    }
                }
            } else {
                // Daily stats for selected month
                item {
                    val dailyStats = mockDailyStats[selectedMonth] ?: emptyList()
                    Text(
                        text = "Daily stats — $selectedMonth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                val dailyStats = mockDailyStats[selectedMonth] ?: emptyList()
                items(dailyStats.size) { i ->
                    DailyStatCard(dailyStats[i])
                }

                item {
                    androidx.compose.material3.TextButton(onClick = { selectedMonth = null }) {
                        Text("Back to year view")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthCard(month: MonthStat, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = month.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${month.km} km",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${month.drives} drives",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Simple bar indicator
            Spacer(modifier = Modifier.height(4.dp))
            val barWidth = (month.km.toFloat() / mockMonths.maxOf { it.km }) * 100
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 4.dp)
            ) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { barWidth / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DailyStatCard(stat: DailyStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stat.day, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "${stat.drives} drive(s)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "%.0f km".format(stat.km), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp))
            Text(text = "%.1f kWh".format(stat.kWh), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
