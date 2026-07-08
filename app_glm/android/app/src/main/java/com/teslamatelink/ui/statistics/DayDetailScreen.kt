package com.teslamatelink.ui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.components.StitchDataRow
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

/**
 * 里程钻取 - 当日行程页 (Stitch 白色瑞士风)。
 * 年度总览 -> 月度详情 -> [本页]。
 * 内容: 当日汇总 / 行程列表 (点击进入行程详情)。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    year: Int,
    month: Int,
    day: Int,
    onDriveClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    // TODO: 当日行程待接入 CarRepository (按日期过滤 drives), 暂用确定性 mock
    val drives = remember(year, month, day) {
        var seed = (year * 10000L + month * 100L + day) * 2654435761L
        val count = ((seed ushr 45).toInt() and 0x3) + 1
        (0 until count).map { i ->
            seed = seed * 6364136223846793005L + 1442695040888963407L
            val km = ((seed ushr 40).toInt() and 0x3F) + 5
            seed = seed * 6364136223846793005L + 1442695040888963407L
            val min = ((seed ushr 42).toInt() and 0x3F) + 8
            MockDrive(
                id = (year * 10000L + month * 100L + day) * 10 + i,
                from = mockPlaces[(i) % mockPlaces.size],
                to = mockPlaces[(i + 1) % mockPlaces.size],
                km = km,
                min = min
            )
        }
    }
    val totalKm = drives.sumOf { it.km }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$month 月 $day 日 行程",
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

            // 当日汇总
            StitchCard {
                Text(
                    "当日总里程",
                    color = StitchColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$totalKm",
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
                StitchDataRow(label = "行程数", value = "${drives.size} 次")
            }

            // 行程列表
            Text(
                "行程明细",
                color = StitchColors.OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            drives.forEach { drive ->
                StitchCard(modifier = Modifier.clickable { onDriveClick(drive.id) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                drive.from,
                                color = StitchColors.OnSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "→ ${drive.to}",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${drive.km} km",
                                color = StitchColors.OnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = JetBrainsMonoFamily
                            )
                            Text(
                                "${drive.min} min",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMonoFamily
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private data class MockDrive(
    val id: Long,
    val from: String,
    val to: String,
    val km: Int,
    val min: Int
)

private val mockPlaces = listOf("家", "公司", "超市", "健身房", "机场", "咖啡馆")
