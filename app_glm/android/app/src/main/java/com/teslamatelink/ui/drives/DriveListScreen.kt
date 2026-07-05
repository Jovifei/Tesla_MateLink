package com.teslamatelink.ui.drives

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.R
import com.teslamatelink.ui.components.ChipStatus
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.components.StitchDataRow
import com.teslamatelink.ui.components.StitchStatusChip
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: DriveViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDrives() }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.drives),
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
            val grouped = state.drives.groupBy { drive ->
                val date = ZonedDateTime.parse(drive.startDate).toLocalDate()
                date.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault()))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (monthKey, drives) ->
                    // Month header: "2026年6月"
                    val year = monthKey.substringBefore("-")
                    val month = monthKey.substringAfter("-").toIntOrNull() ?: 1
                    val monthHeader = "${year}年${month}月"

                    item(key = "header_$monthKey") {
                        Text(
                            text = monthHeader,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchColors.OnSurface,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(drives, key = { it.id }) { drive ->
                        StitchCard(
                            modifier = Modifier.clickable { onNavigateToDetail(drive.id) }
                        ) {
                            // Top row: start → end address + distance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = drive.startAddress,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchColors.OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "→ ${drive.endAddress}",
                                        fontSize = 14.sp,
                                        color = StitchColors.OnSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "%.1f km".format(drive.distanceKm),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = StitchColors.OnSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Data rows
                            StitchDataRow(
                                label = "时长",
                                value = formatDuration(drive.durationMinutes)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StitchDataRow(
                                label = "能耗",
                                value = "%.0f Wh/km".format(drive.efficiency)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StitchDataRow(
                                label = "电量",
                                value = "${drive.batteryStart}% → ${drive.batteryEnd}%"
                            )

                            // Efficiency chip
                            if (drive.efficiency > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val chipText = when {
                                    drive.efficiency <= 150 -> "高效"
                                    drive.efficiency <= 200 -> "正常"
                                    else -> "偏高"
                                }
                                val chipStatus = when {
                                    drive.efficiency <= 150 -> ChipStatus.ONLINE
                                    drive.efficiency <= 200 -> ChipStatus.CHARGING
                                    else -> ChipStatus.ERROR
                                }
                                StitchStatusChip(text = chipText, status = chipStatus)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
