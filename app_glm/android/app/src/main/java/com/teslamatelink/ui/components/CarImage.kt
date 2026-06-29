package com.teslamatelink.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.theme.accentForExteriorColor

@Composable
fun CarImage(
    exteriorColor: String?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = accentForExteriorColor(exteriorColor, isDark)
    // Tint the car image with the accent color based on the car's exterior
    val tintColor = when {
        exteriorColor?.lowercase()?.contains("white") == true -> Color(0xFF8B7355)
        exteriorColor?.lowercase()?.contains("black") == true -> Color(0xFF6B7280)
        exteriorColor?.lowercase()?.contains("quicksilver") == true -> Color(0xFFA09080)
        exteriorColor?.lowercase()?.contains("red") == true -> Color(0xFFEF4444)
        exteriorColor?.lowercase()?.contains("blue") == true -> Color(0xFF3B82F6)
        exteriorColor?.lowercase()?.contains("grey") == true || exteriorColor?.lowercase()?.contains("silver") == true -> Color(0xFF6B7280)
        else -> palette.accent
    }

    Box(
        modifier = modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = "Car",
            modifier = Modifier.size(160.dp),
            tint = tintColor
        )
    }
}

@Composable
fun CarImagePlaceholder(
    exteriorColor: String?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tintColor = when {
        exteriorColor?.lowercase()?.contains("white") == true -> Color(0xFF8B7355)
        exteriorColor?.lowercase()?.contains("black") == true -> Color(0xFF6B7280)
        exteriorColor?.lowercase()?.contains("red") == true -> Color(0xFFEF4444)
        exteriorColor?.lowercase()?.contains("blue") == true -> Color(0xFF3B82F6)
        else -> Color(0xFF6B7280)
    }

    Box(
        modifier = modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = "Car",
            modifier = Modifier.size(80.dp),
            tint = tintColor.copy(alpha = 0.4f)
        )
    }
}
