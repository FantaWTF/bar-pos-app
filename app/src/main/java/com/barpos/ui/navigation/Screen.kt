package com.barpos.ui.navigation

sealed class Screen(val route: String) {
    data object POS : Screen("pos")
    data object History : Screen("history")
    data object QRPayment : Screen("qr_payment/{memberId}") {
        fun createRoute(memberId: Long) = "qr_payment/$memberId"
    }
    data object AdminLogin : Screen("admin_login")
    data object AdminPanel : Screen("admin_panel")
    data object MemberManagement : Screen("admin_members")
    data object ProductManagement : Screen("admin_products")
    data object Stats : Screen("admin_stats")
    data object Settings : Screen("admin_settings")
}
