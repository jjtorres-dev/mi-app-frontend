package com.dvt.medicimapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dvt.medicimapp.presentation.screens.home.HomeScreen
import com.dvt.medicimapp.presentation.screens.healthcenters.HealthCentersScreen
import com.dvt.medicimapp.presentation.screens.report.ReportScreen
import com.dvt.medicimapp.presentation.screens.guide.GuideScreen
import com.dvt.medicimapp.presentation.screens.reports.ReportsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object HealthCenters : Screen("health_centers")
    object Report : Screen("report")
    object Guide : Screen("guide")
    object Reports : Screen("reports")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.HealthCenters.route) { HealthCentersScreen(navController) }
        composable(Screen.Report.route) { ReportScreen(navController) }
        composable(Screen.Guide.route) { GuideScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
    }
}