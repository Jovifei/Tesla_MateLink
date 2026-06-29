package com.teslamatelink.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InfoGrid(
    odometer: Double,
    location: String,
    insideTemp: Double?,
    outsideTemp: Double?,
    frontLeftPsi: Double?,
    frontRightPsi: Double?,
    rearLeftPsi: Double?,
    rearRightPsi: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoTile(
                icon = Icons.Filled.Speed,
                label = "Odometer",
                value = "%.0f km".format(odometer),
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = Icons.Filled.LocationOn,
                label = "Location",
                value = location,
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoTile(
                icon = Icons.Filled.Thermostat,
                label = "Inside",
                value = insideTemp?.let { "%.1f°C".format(it) } ?: "--",
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = Icons.Filled.Thermostat,
                label = "Outside",
                value = outsideTemp?.let { "%.1f°C".format(it) } ?: "--",
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (frontLeftPsi != null || frontRightPsi != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoTile(
                    icon = Icons.Filled.TireRepair,
                    label = "FL",
                    value = frontLeftPsi?.let { "%.1f".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    icon = Icons.Filled.TireRepair,
                    label = "FR",
                    value = frontRightPsi?.let { "%.1f".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    icon = Icons.Filled.TireRepair,
                    label = "RL",
                    value = rearLeftPsi?.let { "%.1f".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    icon = Icons.Filled.TireRepair,
                    label = "RR",
                    value = rearRightPsi?.let { "%.1f".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
