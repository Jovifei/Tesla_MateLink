package com.teslamatelink.ui.cost

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.data.model.TariffConfig

private data class MonthCost(val month: String, val acCost: Float, val dcCost: Float, val total: Float)
private data class LocationPrice(val address: String, val pricePerKwh: Float, val count: Int, val kwh: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostScreen(
    onBack: () -> Unit
) {
    val monthlyData = rememberMonthlyCost()
    val locationRanking = rememberLocationRanking()
    val totalCost = monthlyData.sumOf { it.total.toDouble() }
    val acTotal = monthlyData.sumOf { it.acCost.toDouble() }
    val dcTotal = monthlyData.sumOf { it.dcCost.toDouble() }
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charging Cost") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ValueCard("Total", String.format("%.2f", totalCost), Modifier.weight(1f))
                ValueCard("Home AC", String.format("%.2f", acTotal), Modifier.weight(1f))
                ValueCard("DC", String.format("%.2f", dcTotal), Modifier.weight(1f), Color(0xFFF59E0B))
            }

            // Stacked bar chart
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    Text(
                        "Monthly Cost",
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Canvas(modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 4.dp)) {
                        val padL = 8.dp.toPx()
                        val padT = 8.dp.toPx()
                        val padB = 40.dp.toPx()
                        val chartH = size.height - padT - padB
                        val barCount = monthlyData.size
                        val barW = (size.width - padL * 2) / barCount * 0.7f
                        val gap = (size.width - padL * 2) / barCount * 0.3f
                        val maxCost = monthlyData.maxOf { it.total.coerceAtLeast(0.01f) }

                        val labelStyle = TextStyle(fontSize = 8.sp, color = Color.Gray)
                        monthlyData.forEachIndexed { index, m ->
                            val x = padL + index * (barW + gap)
                            val acH = chartH * (m.acCost / maxCost)
                            val dcH = chartH * (m.dcCost / maxCost)

                            // AC bar (blue)
                            if (acH > 0) {
                                drawRect(
                                    color = Color(0xFF3B82F6),
                                    topLeft = Offset(x, padT + chartH - acH),
                                    size = Size(barW, acH)
                                )
                            }
                            // DC bar (orange) stacked on top
                            if (dcH > 0) {
                                drawRect(
                                    color = Color(0xFFF59E0B),
                                    topLeft = Offset(x, padT + chartH - acH - dcH),
                                    size = Size(barW, dcH)
                                )
                            }

                            // Month label
                            val label = textMeasurer.measure(m.month, labelStyle)
                            drawText(label, topLeft = Offset(x + barW / 2 - label.size.width / 2, size.height - padB + 4.dp.toPx()))
                        }
                    }
                }
            }

            // Location ranking
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Location Ranking (price/kWh)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    locationRanking.forEachIndexed { i, loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${i + 1}",
                                modifier = Modifier.width(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                loc.address,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            Text(
                                String.format("%.2f/kWh", loc.pricePerKwh),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A)
                            )
                            Text(
                                " ${loc.count}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // TOU estimated cost breakdown
            TouEstimateCard()
        }
    }
}

@Composable
private fun rememberMonthlyCost(): List<MonthCost> {
    return androidx.compose.runtime.remember {
        listOf(
            MonthCost("Jan", 42.50f, 15.80f, 58.30f),
            MonthCost("Feb", 38.20f, 12.50f, 50.70f),
            MonthCost("Mar", 55.00f, 22.30f, 77.30f),
            MonthCost("Apr", 48.00f, 18.00f, 66.00f),
            MonthCost("May", 40.50f, 20.50f, 61.00f),
            MonthCost("Jun", 62.00f, 28.00f, 90.00f)
        )
    }
}

@Composable
private fun rememberLocationRanking(): List<LocationPrice> {
    return androidx.compose.runtime.remember {
        listOf(
            LocationPrice("Home", 0.35f, 28, 340.0f),
            LocationPrice("Work", 0.42f, 15, 180.0f),
            LocationPrice("Supercharger Downtown", 0.98f, 6, 120.0f),
            LocationPrice("Supercharger Mall", 1.05f, 4, 85.0f),
            LocationPrice("Public Charger East", 0.65f, 8, 95.0f)
        ).sortedBy { it.pricePerKwh }
    }
}

@Composable
private fun TouEstimateCard() {
    val tariff = TariffConfig.DEFAULT
    // Example: simulate a charge session for each TOU period
    val sampleCharges = remember {
        listOf(
            Triple("Valley (00:00-06:00)", 3, 25.0),   // hour=3, 25 kWh
            Triple("Peak (09:00-11:00)", 10, 15.0),     // hour=10, 15 kWh
            Triple("Peak (17:00-21:00)", 19, 20.0),     // hour=19, 20 kWh
            Triple("Flat (other)", 14, 10.0)             // hour=14, 10 kWh
        )
    }
    val symbol = if (tariff.currencyCode == "CNY") "¥" else tariff.currencyCode

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOU Estimate (${tariff.currencyCode})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "默认费率",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            sampleCharges.forEach { (label, hour, kwh) ->
                val price = tariff.priceForHour(hour)
                val cost = kwh * price
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "%.2f kWh × %.2f = %s%.2f".format(kwh, price, symbol, cost),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            val totalTou = sampleCharges.sumOf { it.third * tariff.priceForHour(it.second) }
            // API cost (simulated flat-rate average for comparison)
            val totalKwh = sampleCharges.sumOf { it.third }
            val apiCostPerKwh = 0.65 // typical average from location ranking
            val apiCost = totalKwh * apiCostPerKwh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOU Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    "%s%.2f".format(symbol, totalTou),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("API Cost", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "%s%.2f".format(symbol, apiCost),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ValueCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
