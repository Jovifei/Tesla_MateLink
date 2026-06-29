package com.teslamatelink.ui.vampire

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class DrainEvent(val date: String, val kWh: Double, val rangeKm: Int, val temp: Int)

// Mock drains: battery % lost between drives with gaps >1h
private val mockDrains = listOf(
    DrainEvent("2026-06-20", 2.3, 16, 22),
    DrainEvent("2026-06-18", 1.8, 12, 25),
    DrainEvent("2026-06-16", 3.1, 21, 30),
    DrainEvent("2026-06-14", 1.5, 10, 18),
    DrainEvent("2026-06-12", 2.7, 19, 15),
    DrainEvent("2026-06-10", 4.2, 29, 8),
    DrainEvent("2026-06-08", 3.5, 24, 2),
    DrainEvent("2026-06-06", 2.0, 14, 10),
    DrainEvent("2026-06-04", 1.2, 8, 28),
    DrainEvent("2026-06-02", 2.8, 20, 24)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VampireScreen(onNavigateBack: () -> Unit) {
    val totalLossKwh = mockDrains.sumOf { it.kWh }
    val totalRangeKm = mockDrains.sumOf { it.rangeKm.toDouble() }
    val maxKwh = mockDrains.maxOf { it.kWh }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vampire Drain") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard("Total Drain", "%.1f kWh".format(totalLossKwh), Modifier.weight(1f))
                    SummaryCard("Range Lost", "%.0f km".format(totalRangeKm), Modifier.weight(1f))
                    SummaryCard("Events", "${mockDrains.size}", Modifier.weight(1f))
                }
            }

            // Line chart of daily drain
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Drain Trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            val chartLeft = 40f
                            val chartBottom = size.height - 20f
                            val chartWidth = size.width - chartLeft - 10f
                            val chartHeight = chartBottom - 10f
                            val stepX = if (mockDrains.size > 1) chartWidth / (mockDrains.size - 1) else chartWidth

                            // Grid
                            listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                                val y = chartBottom - chartHeight * frac
                                drawLine(Color.Gray.copy(alpha = 0.15f), Offset(chartLeft, y), Offset(size.width - 10f, y))
                            }

                            // Line
                            val points = mockDrains.mapIndexed { i, d ->
                                Offset(chartLeft + i * stepX, chartBottom - (d.kWh.toFloat() / maxKwh.toFloat()) * chartHeight)
                            }
                            for (i in 0 until points.size - 1) {
                                drawLine(Color(0xFFEF4444), points[i], points[i + 1], strokeWidth = 2f)
                            }
                            // Dots
                            points.forEach { drawCircle(Color(0xFFEF4444), 4f, it) }
                        }
                    }
                }
            }

            // Drain event cards
            item {
                Text(
                    text = "Drain Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(mockDrains.size) { i ->
                val drain = mockDrains[i]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = drain.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.1f kWh lost".format(drain.kWh), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = "-${drain.rangeKm} km",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
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
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
