package com.teslamatelink.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.components.StitchDataRow
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors
import java.util.Calendar

/**
 * 里程钻取 - 月度详情页 (Stitch 白色瑞士风)。
 * 年度总览 -> [本页] -> 当日行程 (onDayClick)。
 * 内容: 月度汇总大数字 / 日历式日格网格 (点击某日钻取)。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    year: Int,
    month: Int,
    onDayClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val daysInMonth = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    // TODO: 每日里程待接入 CarRepository, 暂用确定性 mock
    val dayKm = remember(year, month) {
        var seed = (year * 100L + month) * 2654435761L
        (1..daysInMonth).associateWith {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            ((seed ushr 40).toInt() and 0x7F)
        }
    }
    val monthKm = dayKm.values.sum()
    val activeDays = dayKm.values.count { it > 0 }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$year 年 $month 月",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            // 月度汇总
            StitchCard {
                Text(
                    "月度总里程",
                    color = StitchColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$monthKm",
                        color = StitchColors.OnSurface,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFamily
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "km",
                        color = StitchColors.OnSurfaceVariant,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                StitchDataRow(label = "活跃天数", value = "$activeDays 天")
            }

            // 日历式日格 (7 列)
            StitchCard {
                Text(
                    "每日里程",
                    color = StitchColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(16.dp))
                val maxKm = (dayKm.values.maxOrNull() ?: 1).coerceAtLeast(1)
                val rows = (daysInMonth + 6) / 7
                for (r in 0 until rows) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0 until 7) {
                            val day = r * 7 + c + 1
                            if (day <= daysInMonth) {
                                val km = dayKm[day] ?: 0
                                val ratio = km.toFloat() / maxKm
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(dayCellColor(ratio))
                                        .clickable { onDayClick(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "$day",
                                            color = if (ratio > 0.5f) StitchColors.OnPrimary else StitchColors.OnSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = JetBrainsMonoFamily
                                        )
                                        Text(
                                            "$km",
                                            color = if (ratio > 0.5f) StitchColors.OnPrimary else StitchColors.OnSurfaceVariant,
                                            fontSize = 8.sp,
                                            fontFamily = JetBrainsMonoFamily
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "点击某日钻取至当日行程",
                    color = StitchColors.OnSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun dayCellColor(ratio: Float) = when {
    ratio <= 0f -> StitchColors.SurfaceContainer
    ratio < 0.25f -> StitchColors.SurfaceContainerHighest
    ratio < 0.5f -> StitchColors.OutlineVariant
    ratio < 0.75f -> StitchColors.Outline
    else -> StitchColors.OnSurface
}
