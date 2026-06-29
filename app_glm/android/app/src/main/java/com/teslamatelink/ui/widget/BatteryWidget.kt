package com.teslamatelink.ui.widget

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Battery status composable that shows charge level and estimated range.
 *
 * Designed for both in-app display and future home-screen widget usage.
 *
 * TODO: Convert to AppWidgetProvider for home screen widget
 * Steps:
 *   1. Create BatteryWidgetProvider extending AppWidgetProvider
 *   2. Define widget layout in XML (RemoteViews)
 *   3. Use Glance (Jetpack Glance) for Compose-based widget
 *   4. Configure widget in AndroidManifest.xml with APPWIDGET intent filter
 *   5. Add widget_info.xml for sizing/config
 *   6. Update via Worker/JobScheduler for periodic refresh
 */
@Composable
fun BatteryWidget(
    batteryPercent: Int,
    rangeKm: Double,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF3B82F6)
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCharging)
                accentColor.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Battery icon with level
            Box(
                modifier = Modifier.height(48.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BatteryIcon(
                    percent = batteryPercent,
                    isCharging = isCharging,
                    accentColor = accentColor,
                    modifier = Modifier.width(80.dp).height(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage text
            Text(
                text = "${batteryPercent}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Range
            Text(
                text = "${rangeKm.toInt()} km range",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Charging indicator
            if (isCharging) {
                Text(
                    text = "Charging",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BatteryIcon(
    percent: Int,
    isCharging: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 2.dp.toPx()

        // Battery body
        val bodyRect = Size(w * 0.82f, h * 0.7f)
        val bodyTopLeft = Offset(0f, (h - bodyRect.height) / 2f)
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.4f),
            topLeft = bodyTopLeft,
            size = bodyRect,
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(strokeW)
        )

        // Fill level
        val fillPct = percent.coerceIn(0, 100) / 100f
        if (fillPct > 0f) {
            val fillW = (bodyRect.width - 4.dp.toPx()) * fillPct
            val fillColor = when {
                percent > 60 -> Color(0xFF22C55E)
                percent > 20 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(bodyTopLeft.x + 2.dp.toPx(), bodyTopLeft.y + 2.dp.toPx()),
                size = Size(fillW, bodyRect.height - 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }

        // Battery terminal (right nub)
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.4f),
            topLeft = Offset(w * 0.84f, h * 0.32f),
            size = Size(w * 0.14f, h * 0.36f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(strokeW)
        )

        // Charging bolt
        if (isCharging) {
            val boltPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.55f, h * 0.15f)
                lineTo(w * 0.35f, h * 0.52f)
                lineTo(w * 0.48f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.85f)
                lineTo(w * 0.65f, h * 0.48f)
                lineTo(w * 0.50f, h * 0.48f)
                close()
            }
            drawPath(boltPath, accentColor)
        }
    }
}
