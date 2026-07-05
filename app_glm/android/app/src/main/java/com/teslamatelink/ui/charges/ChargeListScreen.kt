package com.teslamatelink.ui.charges

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
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
                        text = stringResource(R.string.charging_sessions),
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
            // Group charges by month
            val grouped = state.charges.groupBy { charge ->
                charge.date.take(7) // "yyyy-MM" from ISO date
            }

            // Calculate monthly summary (current month = first group)
            val currentMonthCharges = grouped.values.firstOrNull() ?: emptyList()
            val totalEnergy = currentMonthCharges.sumOf { it.energyKwh }
            val totalCost = currentMonthCharges.sumOf { it.cost }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly summary card
                item(key = "summary") {
                    StitchCard {
                        Text(
                            text = "本月合计",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "%.1f kWh".format(totalEnergy),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = StitchColors.OnSurface
                                )
                                Text(
                                    text = "总充电量",
                                    fontSize = 12.sp,
                                    color = StitchColors.OnSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥%.2f".format(totalCost),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = StitchColors.OnSurface
                                )
                                Text(
                                    text = "总费用",
                                    fontSize = 12.sp,
                                    color = StitchColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                grouped.forEach { (monthKey, charges) ->
                    // Month header
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

                    items(charges, key = { it.id }) { charge ->
                        StitchCard(
                            modifier = Modifier.clickable { onNavigateToDetail(charge.id) }
                        ) {
                            // Top row: address + DC/AC chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = charge.address,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchColors.OnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StitchStatusChip(
                                    text = if (charge.isDc) "DC" else "AC",
                                    status = if (charge.isDc) ChipStatus.CHARGING else ChipStatus.OFFLINE
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Energy + cost row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "%.1f kWh".format(charge.energyKwh),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = StitchColors.OnSurface
                                )
                                Text(
                                    text = "¥%.2f".format(charge.cost),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = StitchColors.OnSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Data rows
                            StitchDataRow(
                                label = "时长",
                                value = formatDuration(charge.durationMinutes)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StitchDataRow(
                                label = "电量",
                                value = "${charge.startBattery}% → ${charge.endBattery}%"
                            )
                            if (charge.maxPower > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                StitchDataRow(
                                    label = "最大功率",
                                    value = "%.0f kW".format(charge.maxPower)
                                )
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
