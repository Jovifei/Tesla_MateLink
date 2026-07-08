package com.teslamatelink.ui.charges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.R
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// Stitch-aligned local colors (current-charge.html)
private val BrandOrange = Color(0xFFF59E0B)   // active power / charging tag / phase bar
private val BrandGreen = Color(0xFF059669)    // ETA card accent
private val BrandGold = Color(0xFFA16207)     // estimated cost
private val RingTrack = Color(0xFFF5F5F5)     // progress ring background
private val ProgressTrack = StitchColors.SurfaceContainerHighest

/**
 * 当前充电 (Current Charge) — real-time charging monitor.
 *
 * 1:1 restoration of Stitch screen 5d52c8ca82df434e9bd4a67e74290ffc.
 *
 * DATA-LAYER GAP: `ChargeViewModel` only exposes historical charge records
 * (getCharges) and has no live charging fields (no `isCharging`, no live
 * charger_power/voltage/current stream). `CarStatus` (data.api.model.CarModels)
 * DOES carry every field this screen needs — chargerPower, chargerVoltage,
 * chargerActualCurrent, batteryLevel, chargeLimitSoc, timeToFullCharge,
 * chargeEnergyAdded — but nothing wires a live CarStatus poll into the charges
 * feature. The 1-second power curve is also not persisted anywhere.
 *
 * TODO(data-layer): add a `CurrentChargeViewModel` that polls
 *   StatusRepository.getStatus(carId) on a 1s cadence while `isCharging`,
 *   maps CarStatus -> CurrentChargeUiState, and keeps a rolling power buffer
 *   for the live curve. Until then this screen renders representative mock
 *   values matching the Stitch comp so layout/visuals can be reviewed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentChargeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChargeLimit: () -> Unit = {}
) {
    // TODO(data-layer): replace mock snapshot with live CarStatus mapping.
    val soc = 78
    val startSoc = 65
    val targetSoc = 100
    val powerKw = 7.4
    val voltageV = 230
    val currentA = 32
    val chargeType = "AC 慢充"
    val stationName = "家庭充电桩"
    val pluggedTime = "18:30"
    val elapsed = "42 min"
    val energyAdded = 5.2
    val etaClock = "21:45"
    val etaRemaining = "约 2h 15min 后"
    val etaEnergy = "+12.3 kWh"
    val batteryTemp = 42
    val costSoFar = 3.85
    val costEstimate = 9.10
    val avgPrice = 0.74
    // Rolling power-curve samples (0..1 normalized height per point). Mock trace.
    val powerCurve = listOf(0.20f, 0.25f, 0.22f, 0.30f, 0.28f, 0.60f, 0.55f, 0.70f, 0.68f)

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "正在充电",
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
                    IconButton(onClick = { /* TODO(data-layer): trigger live refresh */ }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            tint = StitchColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Surface,
                    scrolledContainerColor = StitchColors.Surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Section 2: Giant charging status card (progress ring)
            ChargingStatusCard(
                soc = soc,
                startSoc = startSoc,
                targetSoc = targetSoc
            )

            // Section 3: Real-time data grid (power / voltage / current)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RealtimeMetricCell(
                    modifier = Modifier.weight(1f),
                    value = "%.1f".format(powerKw),
                    unit = "kW",
                    label = "功率",
                    valueColor = BrandOrange
                )
                RealtimeMetricCell(
                    modifier = Modifier.weight(1f),
                    value = "$voltageV",
                    unit = "V",
                    label = "电压"
                )
                RealtimeMetricCell(
                    modifier = Modifier.weight(1f),
                    value = "$currentA",
                    unit = "A",
                    label = "电流"
                )
            }

            // Section 4: Charging info list
            ChargingInfoList(
                chargeType = chargeType,
                stationName = stationName,
                pluggedTime = pluggedTime,
                elapsed = elapsed,
                energyAdded = energyAdded
            )

            // Section 5: Estimated completion card
            EstimatedCompletionCard(
                etaClock = etaClock,
                etaRemaining = etaRemaining,
                etaEnergy = etaEnergy
            )

            // Section 6: Real-time power curve
            PowerCurveCard(powerKw = powerKw, samples = powerCurve)

            // Section 7: Charging phases
            ChargingPhasesCard(soc = soc)

            // Section 8 & 9: Temperature & cost (2-column)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BatteryTempCard(modifier = Modifier.weight(1f), temp = batteryTemp)
                CostEstimateCard(
                    modifier = Modifier.weight(1f),
                    costSoFar = costSoFar,
                    costEstimate = costEstimate,
                    avgPrice = avgPrice
                )
            }

            // Section 10: Control buttons
            ControlButton(
                text = "停止充电",
                color = StitchColors.Error,
                onClick = { /* TODO(data-layer): POST stop-charge command */ }
            )
            ControlButton(
                text = "设置充电限制",
                color = StitchColors.Primary,
                onClick = onNavigateToChargeLimit
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Giant status card: circular progress ring with SoC in the centre. */
@Composable
private fun ChargingStatusCard(soc: Int, startSoc: Int, targetSoc: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(192.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 8.dp.toPx()
                    val inset = strokeW / 2
                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                    val topLeft = Offset(inset, inset)
                    // Track
                    drawArc(
                        color = RingTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )
                    // Progress (start at top, clockwise)
                    drawArc(
                        color = StitchColors.OnSurface,
                        startAngle = -90f,
                        sweepAngle = 360f * (soc / 100f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$soc%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$startSoc% → $targetSoc%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BrandOrange)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "充电中",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = StitchColors.White
                )
            }
        }
    }
}

/** One cell of the real-time data grid (big mono value + unit + label). */
@Composable
private fun RealtimeMetricCell(
    modifier: Modifier = Modifier,
    value: String,
    unit: String,
    label: String,
    valueColor: Color = StitchColors.OnSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = valueColor
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

/** Charging info list: section header + divided key/value rows. */
@Composable
private fun ChargingInfoList(
    chargeType: String,
    stationName: String,
    pluggedTime: String,
    elapsed: String,
    energyAdded: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchColors.SurfaceContainerLow)
                    .padding(16.dp)
            ) {
                Text(
                    text = "充电信息",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            HDivider()
            InfoRow("充电类型", chargeType, mono = false)
            HDivider()
            InfoRow("充电站", stationName, mono = false)
            HDivider()
            InfoRow("接入时间", pluggedTime, mono = true)
            HDivider()
            InfoRow("已充时间", elapsed, mono = true)
            HDivider()
            InfoRow("累计电量", "%.1f kWh".format(energyAdded), mono = true)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, mono: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = if (mono) FontWeight.Medium else FontWeight.SemiBold,
            fontFamily = if (mono) JetBrainsMonoFamily else null,
            color = StitchColors.OnSurface
        )
    }
}

/** Estimated completion card with a green left accent bar. */
@Composable
private fun EstimatedCompletionCard(
    etaClock: String,
    etaRemaining: String,
    etaEnergy: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row {
            Box(Modifier.width(4.dp).fillMaxHeight().background(BrandGreen))
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "预计完成",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = etaClock,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = etaRemaining,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "增程预计",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Text(
                            text = etaEnergy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Progress bar (3/4)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(BrandGreen)
                    )
                }
            }
        }
    }
}

/** Real-time power curve card: header + pulse dot + line chart. */
@Composable
private fun PowerCurveCard(powerKw: Double, samples: List<Float>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "功率曲线 · 实时",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(BrandOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%.1f kW".format(powerKw),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFamily,
                        color = BrandOrange
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                if (samples.size < 2) return@Canvas
                val w = size.width
                val h = size.height
                val stepX = w / (samples.size - 1)
                // y = h - normalized*h (0 at bottom, 1 at top). Leave 10% padding.
                fun px(i: Int) = stepX * i
                fun py(v: Float) = h - (v.coerceIn(0f, 1f) * h * 0.9f) - h * 0.05f

                val path = Path().apply {
                    moveTo(px(0), py(samples[0]))
                    for (i in 1 until samples.size) lineTo(px(i), py(samples[i]))
                }
                drawPath(
                    path = path,
                    color = StitchColors.OnSurface,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                // Live head marker
                drawCircle(
                    color = BrandOrange,
                    radius = 4.dp.toPx(),
                    center = Offset(px(samples.size - 1), py(samples.last()))
                )
            }
        }
    }
}

/** Charging phases card: segmented bar (CC / CV / trickle) + SoC pointer. */
@Composable
private fun ChargingPhasesCard(soc: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "充电阶段",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Segmented bar: 80% CC (orange filled), 15% CV, 5% trickle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.80f)
                        .fillMaxHeight()
                        .background(BrandOrange)
                )
                Box(
                    modifier = Modifier
                        .weight(0.15f)
                        .fillMaxHeight()
                        .background(StitchColors.SurfaceContainerHigh)
                )
                Box(
                    modifier = Modifier
                        .weight(0.05f)
                        .fillMaxHeight()
                        .background(StitchColors.SurfaceContainerHigh)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "恒流段 (0-80%)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )
                Text(
                    text = "恒压段 (80-95%)",
                    fontSize = 10.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Text(
                    text = "涓流段",
                    fontSize = 10.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
        }
    }
}

/** Battery temperature mini-card. */
@Composable
private fun BatteryTempCard(modifier: Modifier = Modifier, temp: Int) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "电池温度",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$temp",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = "°C",
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrandGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "正常",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen
                )
            }
        }
    }
}

/** Cost estimate mini-card (so-far / estimate / avg price). */
@Composable
private fun CostEstimateCard(
    modifier: Modifier = Modifier,
    costSoFar: Double,
    costEstimate: Double,
    avgPrice: Double
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "费用预估",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "已产生", fontSize = 10.sp, color = StitchColors.OnSurfaceVariant)
                Text(
                    text = "¥%.2f".format(costSoFar),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HDivider()
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "预计", fontSize = 10.sp, color = StitchColors.OnSurfaceVariant)
                Text(
                    text = "¥%.2f".format(costEstimate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = BrandGold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "均价 ¥%.2f/kWh".format(avgPrice),
                fontSize = 9.sp,
                color = StitchColors.OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

/** Outlined full-width control button. */
@Composable
private fun ControlButton(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Surface,
        border = BorderStroke(2.dp, color),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun HDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(StitchColors.Border)
    )
}
