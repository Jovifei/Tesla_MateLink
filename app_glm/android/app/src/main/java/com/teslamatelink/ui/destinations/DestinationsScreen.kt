package com.teslamatelink.ui.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Destination(
    val name: String,
    val visits: Int,
    val totalKm: Int,
    val avgEff: Int
)

private enum class Region(val label: String, val icon: String) {
    NORTH_AMERICA("North America", "🌎"),
    EUROPE("Europe", "🌍"),
    ASIA("Asia", "🌏"),
    AUSTRALIA("Australia", "🦘"),
    OTHER("Other", "🌐")
}

private data class RegionStat(
    val region: Region,
    var visitCount: Int,
    var totalKm: Double
)

private val countryRegionMap = buildMap {
    listOf("US", "CA", "MX").forEach { put(it, Region.NORTH_AMERICA) }
    listOf("GB", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "CH", "SE", "NO", "DK", "FI", "PT", "IE", "PL", "CZ", "HU", "RO", "GR", "HR", "SK", "SI", "BG", "LT", "LV", "EE", "LU").forEach { put(it, Region.EUROPE) }
    listOf("CN", "JP", "KR", "TW", "HK", "SG", "IN", "TH", "VN", "MY", "PH", "ID").forEach { put(it, Region.ASIA) }
    listOf("AU", "NZ").forEach { put(it, Region.AUSTRALIA) }
}

private fun regionFromAddress(address: String): Region {
    val upper = address.uppercased()
    for ((code, region) in countryRegionMap) {
        if (upper.contains(code)) return region
    }
    if (upper.contains("USA") || upper.contains("UNITED STATES") || upper.contains("CANADA")) return Region.NORTH_AMERICA
    if (upper.contains("UNITED KINGDOM") || upper.contains("GERMANY") || upper.contains("FRANCE") || upper.contains("EUROPE")) return Region.EUROPE
    if (upper.contains("CHINA") || upper.contains("JAPAN") || upper.contains("KOREA")) return Region.ASIA
    if (upper.contains("AUSTRALIA")) return Region.AUSTRALIA
    return Region.OTHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationsScreen(
    onBack: () -> Unit
) {
    var sortMode by remember { mutableStateOf("Visits") }
    val destinations = rememberDestinations()
    val regionStats = rememberRegionStats(destinations)

    val sorted = when (sortMode) {
        "Visits" -> destinations.sortedByDescending { it.visits }
        "Distance" -> destinations.sortedByDescending { it.totalKm }
        "Efficiency" -> destinations.sortedBy { it.avgEff }
        else -> destinations
    }.take(20)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Destinations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Sort chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Visits", "Distance", "Efficiency").forEach { mode ->
                        FilterChip(
                            selected = sortMode == mode,
                            onClick = { sortMode = mode },
                            label = { Text(mode, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Destination list
            items(sorted) { dest ->
                DestinationRow(dest)
            }

            // Visited Regions section
            if (regionStats.isNotEmpty()) {
                item {
                    Text(
                        text = "Visited Regions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(regionStats) { rs ->
                    RegionRow(rs)
                }
            }
        }
    }
}

@Composable
private fun DestinationRow(dest: Destination) {
    val effColor = when {
        dest.avgEff < 150 -> Color(0xFF22C55E)
        dest.avgEff < 200 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = "${dest.visits}",
            modifier = Modifier.padding(end = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        // Name and sub info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dest.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "${dest.visits} visits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Distance
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Text(
                text = "${dest.totalKm}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Efficiency
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${dest.avgEff}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = effColor
            )
            Text(
                text = "Wh/km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun rememberDestinations(): List<Destination> {
    return androidx.compose.runtime.remember {
        listOf(
            Destination("Home", 42, 1260, 145),
            Destination("Work", 28, 840, 152),
            Destination("Supercharger Mall", 15, 780, 188),
            Destination("Beach Road", 12, 960, 142),
            Destination("Grandparents", 10, 520, 158),
            Destination("Shopping Center", 9, 270, 165),
            Destination("Airport", 8, 640, 172),
            Destination("Gym", 7, 140, 148),
            Destination("Park & Ride", 6, 360, 155),
            Destination("Ski Resort", 5, 850, 210),
            Destination("IKEA", 5, 180, 160),
            Destination("Downtown Market", 4, 85, 148),
            Destination("Tech Park", 4, 220, 150),
            Destination("University", 3, 95, 145),
            Destination("Costco", 3, 120, 155),
            Destination("Train Station", 3, 65, 142),
            Destination("Lake View", 2, 190, 138),
            Destination("Mountain Cabin", 2, 320, 195),
            Destination("Friends Place", 2, 45, 150),
            Destination("Vet Clinic", 1, 30, 148)
        )
    }
}

@Composable
private fun rememberRegionStats(destinations: List<Destination>): List<RegionStat> {
    return androidx.compose.runtime.remember(destinations) {
        // Simulate region assignment from destination names
        // In production, this would use endAddress from drive data
        val rMap = mutableMapOf<Region, RegionStat>()
        for (dest in destinations) {
            val region = regionFromAddress(dest.name)
            val existing = rMap[region]
            if (existing != null) {
                existing.visitCount += dest.visits
                existing.totalKm += dest.totalKm.toDouble()
            } else {
                rMap[region] = RegionStat(region, dest.visits, dest.totalKm.toDouble())
            }
        }
        rMap.values.sortedByDescending { it.visitCount }
    }
}

@Composable
private fun RegionRow(stat: RegionStat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Region icon
        Text(
            text = stat.region.icon,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Region name and visit count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.region.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${stat.visitCount} visits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Total distance
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${stat.totalKm.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
