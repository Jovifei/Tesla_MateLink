package com.teslamatelink

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teslamatelink.ui.components.StitchBottomBar
import com.teslamatelink.ui.navigation.NavGraph
import com.teslamatelink.ui.navigation.Routes
import com.teslamatelink.ui.theme.StitchColors

/**
 * Routes that display the bottom navigation bar.
 */
private val mainRoutes = setOf(
    Routes.DASHBOARD,
    Routes.DRIVES,
    Routes.CHARGES,
    Routes.MORE
)

/**
 * Root screen: Scaffold + conditional [StitchBottomBar] + [NavGraph].
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = StitchColors.Background,
        bottomBar = {
            if (currentRoute in mainRoutes) {
                StitchBottomBar(
                    currentRoute = currentRoute ?: Routes.DASHBOARD,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
