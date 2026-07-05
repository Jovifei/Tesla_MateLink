package com.teslamatelink.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

/**
 * Stitch data row: label-caps label on left (Inter 12px Bold),
 * data-md value on right (JetBrains Mono 16px Medium), right-aligned.
 */
@Composable
fun StitchDataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Text(
            text = value,
            color = StitchColors.OnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily
        )
    }
}
