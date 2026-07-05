package com.teslamatelink.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.theme.StitchColors

/**
 * Stitch white-minimal card: white background, 1px #E5E5E5 border,
 * 8px corner radius, 24px internal padding, zero elevation (no shadow).
 */
@Composable
fun StitchCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}
