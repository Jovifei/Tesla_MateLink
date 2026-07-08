package com.teslamatelink.ui.charges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.R
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// Stitch-aligned local colors (charges.html)
private val DcOrange = Color(0xFFF59E0B)      // DC left bar + tag border/text
private val AcBlue = Color(0xFF3B82F6)        // AC tag border/text
private val FooterBg = Color(0xFFF8FAFC)      // list item footer grey background
private val TrackBg = StitchColors.SurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCurrentCharge: () -> Unit = {},
    viewModel: ChargeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCharges() }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "充电历史",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.OnSurface,
                        letterSpacing = (-0.24).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCurrentCharge) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = "正在充电",
                            tint = StitchColors.Accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background,
                    scrolledContainerColor = StitchColors.Background
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(color = StitchColors.OnSurface) }
        } else {
            // Group charges by month (yyyy-MM from ISO date)
            val grouped = state.charges.groupBy { it.date.take(7) }
            val currentMonthCharges = grouped.values.firstOrNull() ?: emptyList()
            val totalEnergy = currentMonthCharges.sumOf { it.energyKwh }
            val totalCost = currentMonthCharges.sumOf { it.cost }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Stats Bento (2-column grid, swiss-gold energy / swiss-black cost)
                item(key = "stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BentoStatCard(
                            modifier = Modifier.weight(1f),
                            label = "总电量",
                            suffix = "本月",
                            value = "%.0f".format(totalEnergy),
                            unit = "kWh",
                            valueColor = StitchColors.Accent
                        )
                        BentoStatCard(
                            modifier = Modifier.weight(1f),
                            label = "总费用",
                            suffix = "本月",
                            value = "%.2f".format(totalCost),
                            prefix = "¥",
                            valueColor = StitchColors.OnSurface
                        )
                    }
                }

                // 历史明细 section header (label-caps)
                item(key = "history_header") {
                    Text(
                        text = "历史明细",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }

                grouped.forEach { (monthKey, charges) ->
                    val year = monthKey.substringBefore("-")
                    val month = monthKey.substringAfter("-").toIntOrNull() ?: 1

                    item(key = "header_$monthKey") {
                        Text(
                            text = "${year}年${month}月",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchColors.OnSurface
                        )
                    }

                    items(charges, key = { it.id }) { charge ->
                        ChargeListItem(
                            charge = charge,
                            onClick = { onNavigateToDetail(charge.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

/**
 * Stitch bento stat card: white bg, 1px border, 8px radius, 24px padding.
 * Label (label-caps) + suffix (body-sm) top row, big mono value below.
 */
@Composable
private fun BentoStatCard(
    modifier: Modifier = Modifier,
    label: String,
    suffix: String,
    value: String,
    unit: String = "",
    prefix: String = "",
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                if (prefix.isNotEmpty()) {
                    Text(
                        text = prefix,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.padding(end = 2.dp, bottom = 2.dp)
                    )
                }
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = valueColor
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stitch list item card. DC has a 4px orange left bar, AC has none.
 * Top segment: address + energy(kWh, swiss-gold) + battery range bar.
 * Footer (grey bg): date + DC/AC tag + power + cost + chevron.
 */
@Composable
private fun ChargeListItem(
    charge: ChargeItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row {
            // DC orange left bar (only DC; AC has no marker)
            if (charge.isDc) {
                Box(Modifier.width(4.dp).fillMaxHeight().background(DcOrange))
            }
            Column {
                // Top segment
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = charge.address,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "+%.1f".format(charge.energyKwh),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = JetBrainsMonoFamily,
                                color = StitchColors.Accent
                            )
                            Text(
                                text = "kWh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Battery range row: start% ... bar ... end% ... duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${charge.startBattery}%",
                            fontSize = 14.sp,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurfaceVariant
                        )
                        BatteryRangeBar(
                            start = charge.startBattery,
                            end = charge.endBattery,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${charge.endBattery}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = formatDuration(charge.durationMinutes),
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
                // Top border line between segments
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(StitchColors.Border)
                )
                // Footer segment (grey background)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FooterBg)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatShortDate(charge.date),
                        fontSize = 13.sp,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    DcAcTag(isDc = charge.isDc)
                    Spacer(modifier = Modifier.weight(1f))
                    if (charge.maxPower > 0) {
                        Text(
                            text = "%.0fkW".format(charge.maxPower),
                            fontSize = 16.sp,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Text(
                        text = "¥%.2f".format(charge.cost),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Battery range bar with highlighted [start, end] segment on a neutral track.
 */
@Composable
private fun BatteryRangeBar(
    start: Int,
    end: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(TrackBg)
    ) {
        val startFraction = (start / 100f).coerceIn(0f, 1f)
        val rangeFraction = ((end - start) / 100f).coerceIn(0f, 1f)
        val trackWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(trackWidth * rangeFraction)
                .offset(x = trackWidth * startFraction)
                .clip(RoundedCornerShape(2.dp))
                .background(StitchColors.Accent)
        )
    }
}

/**
 * DC/AC tag: outlined pill, orange for DC, blue for AC.
 */
@Composable
private fun DcAcTag(isDc: Boolean) {
    val color = if (isDc) DcOrange else AcBlue
    val text = if (isDc) "DC 直流" else "AC 交流"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, color, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/** ISO "2025-07-22T14:30:..." -> "07-22 14:30" */
private fun formatShortDate(iso: String): String {
    if (iso.length < 16) return iso
    return iso.substring(5, 16).replace("T", " ")
}
