package com.teslamatelink.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.StitchColors

enum class ChipStatus(val bg: Color, val fg: Color) {
    ONLINE(StitchColors.StatusOnlineBg, StitchColors.StatusOnline),
    OFFLINE(StitchColors.StatusOfflineBg, StitchColors.StatusOffline),
    CHARGING(StitchColors.StatusChargingBg, StitchColors.StatusCharging),
    ERROR(StitchColors.StatusErrorBg, StitchColors.StatusError)
}

/**
 * Stitch status chip: 4px corner radius, tinted background, dark text.
 * Uses label-caps typography (Inter 12px Bold, 0.05em letter-spacing).
 */
@Composable
fun StitchStatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = status.bg,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            color = status.fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
