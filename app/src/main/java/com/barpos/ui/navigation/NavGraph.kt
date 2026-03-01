package com.barpos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.barpos.BarApplication
import com.barpos.ui.admin.AdminPanelScreen
import com.barpos.ui.admin.login.AdminLoginScreen
import com.barpos.ui.admin.members.MemberManagementScreen
import com.barpos.ui.admin.products.ProductManagementScreen
import com.barpos.ui.admin.settings.SettingsScreen
import com.barpos.ui.admin.stats.StatsScreen
import com.barpos.ui.history.HistoryScreen
import com.barpos.ui.payment.QRPaymentScreen
import com.barpos.ui.pos.POSScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    application: BarApplication
) {
    NavHost(
        navController = navController,
        startDestination = Screen.POS.route
    ) {
        composable(Screen.POS.route) {
            POSScreen(
                application = application,
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminLogin.route) },
                onNavigateToPayment = { memberId ->
                    navController.navigate(Screen.QRPayment.createRoute(memberId))
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                application = application,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QRPayment.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getLong("memberId") ?: return@composable
            QRPaymentScreen(
                application = application,
                memberId = memberId,
                onPaymentComplete = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(
                application = application,
                onLoginSuccess = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.AdminLogin.route) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onNavigateToMembers = { navController.navigate(Screen.MemberManagement.route) },
                onNavigateToProducts = { navController.navigate(Screen.ProductManagement.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateBack = { navController.popBackStack(Screen.POS.route, inclusive = false) }
            )
        }

        composable(Screen.MemberManagement.route) {
            MemberManagementScreen(
                application = application,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProductManagement.route) {
            ProductManagementScreen(
                application = application,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                application = application,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                application = application,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
