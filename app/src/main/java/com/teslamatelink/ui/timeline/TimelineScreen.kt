package com.teslamatelink.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class TimelineEvent(
    val type: String, val startTime: String, val endTime: String,
    val durationMin: Int, val description: String, val detail: String,
    val id: Int = 0
)

private val mockEvents = listOf(
    TimelineEvent("drive", "08:15", "09:05", 50, "Home -> Office", "32 km · 170 Wh/km", 1),
    TimelineEvent("charge", "09:15", "11:30", 135, "Office Garage", "22 kWh · AC", 2),
    TimelineEvent("drive", "12:10", "12:35", 25, "Office -> Restaurant", "12 km · 165 Wh/km", 3),
    TimelineEvent("drive", "13:20", "13:50", 30, "Restaurant -> Office", "14 km · 180 Wh/km", 4),
    TimelineEvent("drive", "17:30", "18:20", 50, "Office -> Home", "35 km · 195 Wh/km", 5),
    TimelineEvent("charge", "18:30", "22:00", 210, "Home (AC)", "40 kWh · AC", 6),
    TimelineEvent("drive", "07:45", "08:30", 45, "Home -> Airport", "40 km · 185 Wh/km", 7),
    TimelineEvent("charge", "09:00", "09:20", 20, "Supercharger Airport", "18 kWh · DC", 8),
    TimelineEvent("drive", "14:00", "16:30", 150, "Airport -> Hotel", "180 km · 210 Wh/km", 9),
    TimelineEvent("charge", "17:00", "18:00", 60, "Hotel Destination Charger", "30 kWh · AC", 10)
)

private val typeColor = mapOf(
    "drive" to Color(0xFF3B82F6),
    "charge" to Color(0xFFF59E0B)
)
private val typeIcon = mapOf("drive" to "Drive", "charge" to "Charge")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, Int) -> Unit = { _, _ -> }
) {
    var selectedId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Timeline") },
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
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Legend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    typeColor.forEach { (type, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(color = color)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = typeIcon[type] ?: type,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Timeline entries
            itemsIndexed(mockEvents) { _, event ->
                val isSelected = selectedId == event.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Timeline line + dot
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(
                                color = typeColor[event.type] ?: Color.Gray,
                                radius = 6.dp.toPx()
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = MaterialTheme.colorScheme.primary,
                                    radius = 8.dp.toPx(),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Event card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                            .clickable {
                                selectedId = event.id
                                onNavigateToDetail(event.type, event.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${event.startTime} - ${event.endTime} (${event.durationMin} min)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = event.detail,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = typeColor[event.type] ?: Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
