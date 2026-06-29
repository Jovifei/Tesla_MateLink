package com.teslamatelink.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val HOURS = 0..23
private const val DAYS = 15

// Mock 24x15 grid: hour x day, values in km
private val mockGrid: List<List<Double>> = List(24) { h ->
    List(DAYS) { d ->
        when {
            h in 7..9 -> (5.0..25.0).random() // Morning commute
            h in 17..19 -> (8.0..30.0).random() // Evening commute
            h in 10..16 -> (0.0..12.0).random() // Midday driving
            h in 20..23 -> (0.0..8.0).random()  // Evening
            else -> (0.0..3.0).random()          // Night
        }
    }
}

private val dayLabels = List(DAYS) { i ->
    val d = java.time.LocalDate.now().minusDays(DAYS - 1L - i)
    "${d.monthValue}/${d.dayOfMonth}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(onNavigateBack: () -> Unit) {
    val maxVal = maxOf(1.0, mockGrid.flatten().maxOrNull() ?: 1.0)
    var tooltipText by remember { mutableStateOf<String?>(null) }

    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val emptyColor = Color.Gray.copy(alpha = 0.15f)
    val lightBlue = Color(0xFF93C5FD)
    val midBlue = Color(0xFF3B82F6)
    val darkBlue = Color(0xFF1D4ED8)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drive Heatmap") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "15 days x 24 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Heatmap canvas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val cellSize = 14.dp
                    val gap = 2.dp
                    val labelWidth = 28.dp
                    val labelHeight = 16.dp

                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Column {
                            // Hour labels on left + grid
                            HOURS.forEach { hour ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Hour label
                                    if (hour % 3 == 0) {
                                        Text(
                                            text = "%02d".format(hour),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(labelWidth)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(labelWidth))
                                    }

                                    // Day cells
                                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                        (0 until DAYS).forEach { day ->
                                            val value = mockGrid[hour][day]
                                            val cellColor = when {
                                                value <= 0 -> emptyColor
                                                value / maxVal < 0.25f -> lightBlue.copy(alpha = 0.4f)
                                                value / maxVal < 0.5f -> lightBlue.copy(alpha = 0.7f)
                                                value / maxVal < 0.75f -> midBlue.copy(alpha = 0.7f)
                                                else -> darkBlue
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .padding(0.dp)
                                            ) {
                                                // Simulated cell
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    drawRoundRect(
                                                        color = cellColor,
                                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Day labels below
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.padding(start = labelWidth),
                                horizontalArrangement = Arrangement.spacedBy(gap)
                            ) {
                                (0 until DAYS).forEach { day ->
                                    Text(
                                        text = dayLabels[day].substringAfter("/"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(cellSize)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(emptyColor, lightBlue.copy(alpha = 0.4f), lightBlue.copy(alpha = 0.7f), midBlue.copy(alpha = 0.7f), darkBlue).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(1.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRoundRect(color = c, cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (tooltipText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tooltipText!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
