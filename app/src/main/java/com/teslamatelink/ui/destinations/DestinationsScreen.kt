package com.teslamatelink.ui.destinations

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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

private data class Destination(
    val name: String, val visits: Int,
    val totalKm: Int, val avgEff: Int
)

private val mockDestinations = listOf(
    Destination("Home", 128, 0, 0),
    Destination("Office Downtown", 86, 720, 175),
    Destination("Supercharger Mall", 32, 960, 190),
    Destination("Grocery Center", 45, 180, 165),
    Destination("Airport Parking", 18, 1260, 200),
    Destination("Gym", 52, 260, 160),
    Destination("Shopping Plaza", 38, 456, 178),
    Destination("Friend's House", 24, 600, 180),
    Destination("Park & Ride", 20, 400, 170),
    Destination("Hospital", 15, 300, 172),
    Destination("School", 42, 168, 155),
    Destination("Restaurant District", 28, 224, 168),
    Destination("Hotel Downtown", 12, 360, 185),
    Destination("Train Station", 22, 440, 175),
    Destination("Beach Parking", 16, 640, 195),
    Destination("Sports Arena", 10, 350, 188),
    Destination("Library", 30, 90, 158),
    Destination("Pharmacy", 35, 70, 155),
    Destination("Coffee Shop", 48, 96, 160),
    Destination("Service Center", 8, 200, 172)
)

private enum class SortMode(val label: String) { VISITS("Visits"), DISTANCE("Distance"), EFFICIENCY("Efficiency") }

private fun efficiencyColor(eff: Int): Color = when {
    eff <= 160 -> Color(0xFF22C55E)
    eff <= 190 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationsScreen(onNavigateBack: () -> Unit) {
    var sortMode by remember { mutableStateOf(SortMode.VISITS) }

    val sorted = when (sortMode) {
        SortMode.VISITS -> mockDestinations.sortedByDescending { it.visits }
        SortMode.DISTANCE -> mockDestinations.sortedByDescending { it.totalKm }
        SortMode.EFFICIENCY -> mockDestinations.sortedBy { if (it.avgEff == 0) Int.MAX_VALUE else it.avgEff }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Destinations") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortMode.entries.forEach { mode ->
                        FilterChip(
                            selected = sortMode == mode,
                            onClick = { sortMode = mode },
                            label = { Text(mode.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                    Text("Destination", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("Visits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                    Text("km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                    Text("Eff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                }
                HorizontalDivider()
            }

            itemsIndexed(sorted.take(20)) { i, dest ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${i + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        dest.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        "${dest.visits}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "${dest.totalKm}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (dest.avgEff > 0) "${dest.avgEff}" else "--",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End,
                        color = if (dest.avgEff > 0) efficiencyColor(dest.avgEff) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (i < sorted.size - 1 && i < 19) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
