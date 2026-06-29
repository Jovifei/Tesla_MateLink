package com.teslamatelink.ui.range

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

private data class RangeDataPoint(
    val date: String, val estPercent: Int,
    val actualPercent: Int, val diffPercent: Double, val temp: Int
)

private val mockRangeData = listOf(
    RangeDataPoint("Jun 20", 80, 75, 5.0, 22),
    RangeDataPoint("Jun 19", 65, 62, 3.0, 25),
    RangeDataPoint("Jun 18", 90, 82, 8.0, 28),
    RangeDataPoint("Jun 17", 55, 53, 2.0, 30),
    RangeDataPoint("Jun 16", 70, 68, 2.0, 26),
    RangeDataPoint("Jun 15", 85, 78, 7.0, 22),
    RangeDataPoint("Jun 14", 60, 58, 2.0, 20),
    RangeDataPoint("Jun 13", 75, 70, 5.0, 18),
    RangeDataPoint("Jun 12", 50, 48, 2.0, 15),
    RangeDataPoint("Jun 11", 95, 85, 10.0, 12),
    RangeDataPoint("Jun 10", 45, 44, 1.0, 8),
    RangeDataPoint("Jun 09", 80, 72, 8.0, 5),
    RangeDataPoint("Jun 08", 70, 67, 3.0, 0),
    RangeDataPoint("Jun 07", 65, 60, 5.0, -2),
    RangeDataPoint("Jun 06", 55, 53, 2.0, 10)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeScreen(onNavigateBack: () -> Unit) {
    val avgDiff = if (mockRangeData.isNotEmpty()) mockRangeData.map { it.diffPercent }.average() else 0.0
    val avgTemp = if (mockRangeData.isNotEmpty()) mockRangeData.map { it.temp.toDouble() }.average() else 0.0
    val maxVal = 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projected Range") },
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
                    RangeSummary("Avg Diff", "%.1f%%".format(avgDiff), Modifier.weight(1f))
                    RangeSummary("Trips", "${mockRangeData.size}", Modifier.weight(1f))
                    RangeSummary("Avg Temp", "%.0f°C".format(avgTemp), Modifier.weight(1f))
                }
            }

            // Estimated vs Actual line chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Est. vs Actual Battery per Trip",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            val left = 35f
                            val bottom = size.height - 20f
                            val w = size.width - left - 10f
                            val h = bottom - 10f
                            val stepX = if (mockRangeData.size > 1) w / (mockRangeData.size - 1) else w

                            // Grid
                            listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                                val y = bottom - h * frac
                                drawLine(Color.Gray.copy(alpha = 0.15f), Offset(left, y), Offset(size.width - 10f, y))
                            }

                            // Estimated line (blue)
                            val estPoints = mockRangeData.mapIndexed { i, d ->
                                Offset(left + i * stepX, bottom - (d.estPercent.toFloat() / maxVal) * h)
                            }
                            for (i in 0 until estPoints.size - 1) {
                                drawLine(Color(0xFF3B82F6), estPoints[i], estPoints[i + 1], strokeWidth = 2f)
                            }
                            estPoints.forEach { drawCircle(Color(0xFF3B82F6), 3f, it) }

                            // Actual line (green)
                            val actPoints = mockRangeData.mapIndexed { i, d ->
                                Offset(left + i * stepX, bottom - (d.actualPercent.toFloat() / maxVal) * h)
                            }
                            for (i in 0 until actPoints.size - 1) {
                                drawLine(Color(0xFF22C55E), actPoints[i], actPoints[i + 1], strokeWidth = 2f)
                            }
                            actPoints.forEach { drawCircle(Color(0xFF22C55E), 3f, it) }
                        }

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Canvas(modifier = Modifier.size(10.dp)) { drawLine(Color(0xFF3B82F6), Offset(0f, 5f), Offset(10f, 5f), strokeWidth = 2f) }
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text("Est %", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Canvas(modifier = Modifier.size(10.dp)) { drawLine(Color(0xFF22C55E), Offset(0f, 5f), Offset(10f, 5f), strokeWidth = 2f) }
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text("Actual %", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeSummary(label: String, value: String, modifier: Modifier = Modifier) {
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
