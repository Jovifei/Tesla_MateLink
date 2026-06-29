package com.teslamatelink.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.theme.StateAsleep
import com.teslamatelink.ui.theme.StateCharging
import com.teslamatelink.ui.theme.StateDriving
import com.teslamatelink.ui.theme.StateOffline
import com.teslamatelink.ui.theme.StateOnline

@Composable
fun StatusBadge(
    state: String,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (state.lowercase()) {
        "online" -> "Online" to StateOnline
        "charging" -> "Charging" to StateCharging
        "driving" -> "Driving" to StateDriving
        "asleep", "suspended" -> "Asleep" to StateAsleep
        "offline" -> "Offline" to StateOffline
        else -> state to Color.Gray
    }

    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
