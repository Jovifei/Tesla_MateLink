package com.teslamatelink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teslamatelink.ui.battery.BatteryHealthScreen
import com.teslamatelink.ui.charges.ChargeDetailScreen
import com.teslamatelink.ui.charges.ChargeListScreen
import com.teslamatelink.ui.cost.CostScreen
import com.teslamatelink.ui.dashboard.DashboardScreen
import com.teslamatelink.ui.destinations.DestinationsScreen
import com.teslamatelink.ui.drives.DriveDetailScreen
import com.teslamatelink.ui.drives.DriveListScreen
import com.teslamatelink.ui.efficiency.EfficiencyScreen
import com.teslamatelink.ui.heatmap.HeatmapScreen
import com.teslamatelink.ui.onboarding.OnboardingScreen
import com.teslamatelink.ui.range.RangeScreen
import com.teslamatelink.ui.settings.AboutScreen
import com.teslamatelink.ui.settings.SettingsScreen
import com.teslamatelink.ui.statistics.DayDetailScreen
import com.teslamatelink.ui.statistics.MonthDetailScreen
import com.teslamatelink.ui.statistics.StatisticsScreen
import com.teslamatelink.ui.more.MoreScreen
import com.teslamatelink.ui.timeline.TimelineScreen
import com.teslamatelink.ui.updates.UpdatesScreen
import com.teslamatelink.ui.vampire.VampireScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val DRIVES = "drives"
    const val DRIVE_DETAIL = "driveDetail/{driveId}"
    const val CHARGES = "charges"
    const val CHARGE_DETAIL = "chargeDetail/{chargeId}"
    const val BATTERY = "battery"
    const val STATISTICS = "statistics"
    const val STATISTICS_MONTH = "stats/month/{year}/{month}"
    const val STATISTICS_DAY = "stats/day/{year}/{month}/{day}"
    const val HEATMAP = "heatmap"
    const val EFFICIENCY = "efficiency"
    const val VAMPIRE = "vampire"
    const val RANGE = "range"
    const val COST = "cost"
    const val DESTINATIONS = "destinations"
    const val TIMELINE = "timeline"
    const val UPDATES = "updates"
    const val SETTINGS = "settings"
    const val MORE = "more"
    const val ABOUT = "about"

    fun driveDetail(id: Int) = "driveDetail/$id"
    fun chargeDetail(id: Int) = "chargeDetail/$id"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.DASHBOARD,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // -- Onboarding --
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // -- Dashboard --
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToDrives = { navController.navigate(Routes.DRIVES) },
                onNavigateToCharges = { navController.navigate(Routes.CHARGES) },
                onNavigateToBattery = { navController.navigate(Routes.BATTERY) },
                onNavigateToUpdates = { navController.navigate(Routes.UPDATES) },
                // Additional analytics navigations
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                onNavigateToHeatmap = { navController.navigate(Routes.HEATMAP) },
                onNavigateToEfficiency = { navController.navigate(Routes.EFFICIENCY) },
                onNavigateToVampire = { navController.navigate(Routes.VAMPIRE) },
                onNavigateToRange = { navController.navigate(Routes.RANGE) },
                onNavigateToCost = { navController.navigate(Routes.COST) },
                onNavigateToDestinations = { navController.navigate(Routes.DESTINATIONS) },
                onNavigateToTimeline = { navController.navigate(Routes.TIMELINE) }
            )
        }

        // -- Drives --
        composable(Routes.DRIVES) {
            DriveListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { driveId ->
                    navController.navigate(Routes.driveDetail(driveId))
                }
            )
        }
        composable(
            route = Routes.DRIVE_DETAIL,
            arguments = listOf(navArgument("driveId") { type = NavType.IntType })
        ) { backStackEntry ->
            val driveId = backStackEntry.arguments?.getInt("driveId") ?: return@composable
            DriveDetailScreen(
                driveId = driveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // -- Charges --
        composable(Routes.CHARGES) {
            ChargeListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { chargeId ->
                    navController.navigate(Routes.chargeDetail(chargeId))
                }
            )
        }
        composable(
            route = Routes.CHARGE_DETAIL,
            arguments = listOf(navArgument("chargeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val chargeId = backStackEntry.arguments?.getInt("chargeId") ?: return@composable
            ChargeDetailScreen(
                chargeId = chargeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // -- Battery --
        composable(Routes.BATTERY) {
            BatteryHealthScreen(onNavigateBack = { navController.popBackStack() })
        }

        // -- Analytics screens --
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onMonthClick = { year, month ->
                    navController.navigate("stats/month/$year/$month")
                }
            )
        }
        composable(
            route = Routes.STATISTICS_MONTH,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2026
            val month = (backStackEntry.arguments?.getInt("month") ?: 1).coerceIn(1, 12)
            MonthDetailScreen(
                year = year,
                month = month,
                onDayClick = { day -> navController.navigate("stats/day/$year/$month/$day") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.STATISTICS_DAY,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2026
            val month = (backStackEntry.arguments?.getInt("month") ?: 1).coerceIn(1, 12)
            val day = (backStackEntry.arguments?.getInt("day") ?: 1).coerceIn(1, 31)
            DayDetailScreen(
                year = year,
                month = month,
                day = day,
                onDriveClick = { driveId -> navController.navigate(Routes.driveDetail(driveId.toInt())) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HEATMAP) {
            HeatmapScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.EFFICIENCY) {
            EfficiencyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.VAMPIRE) {
            VampireScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RANGE) {
            RangeScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.COST) {
            CostScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DESTINATIONS) {
            DestinationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TIMELINE) {
            TimelineScreen(onBack = { navController.popBackStack() })
        }

        // -- Updates --
        composable(Routes.UPDATES) {
            UpdatesScreen(onNavigateBack = { navController.popBackStack() })
        }

        // -- More (placeholder tab) --
        composable(Routes.MORE) {
            MoreScreen()
        }

        // -- Settings --
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
