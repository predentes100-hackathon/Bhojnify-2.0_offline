package com.bhojnify.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhojnify.android.screens.*
import com.bhojnify.android.viewmodel.MessViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val labelKey: String) {
    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home, "home")
    object Admin : BottomNavItem(Routes.ADMIN, Icons.Default.Settings, "admin")
    object Profile : BottomNavItem(Routes.PROFILE, Icons.Default.Person, "profile")
}

@Composable
fun AppNavigator(viewModel: MessViewModel) {
    val navController = rememberNavController()

    // Read initial route depending on onboarding complete
    val state = viewModel.state.value
    val startDestination = if (state.onboardingComplete) Routes.HOME else Routes.ONBOARDING

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Show bottom bar only on root tabs
            if (currentRoute in listOf(Routes.HOME, Routes.ADMIN, Routes.PROFILE)) {
                NavigationBar {
                    val items = listOf(BottomNavItem.Home, BottomNavItem.Admin, BottomNavItem.Profile)
                    items.forEach { item ->
                        val labelText = viewModel.t(item.labelKey)
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = labelText) },
                            label = { Text(labelText) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.ONBOARDING) { OnboardingScreen(navController, viewModel) }
            composable(Routes.HOME) { HomeScreen(navController, viewModel) }
            composable(Routes.ADMIN) { AdminScreen(navController, viewModel) }
            composable(Routes.PROFILE) { ProfileScreen(navController, viewModel) }
            composable(Routes.CUSTOMERS) { CustomersScreen(navController, viewModel) }
            composable(Routes.INVENTORY) { InventoryScreen(navController, viewModel) }
            composable(Routes.LEAVE) { LeaveScreen(navController, viewModel) }
            composable(Routes.MENU) { MenuScreen(navController, viewModel) }
            composable(Routes.EXPENSES) { ExpensesScreen(navController, viewModel) }
            composable(Routes.PAYMENTS) { PaymentsScreen(navController, viewModel) }
            composable(Routes.REPORT) { ReportScreen(navController, viewModel) }
            composable(Routes.STAFF) { StaffScreen(navController, viewModel) }
            composable(Routes.FEEDBACK) { FeedbackScreen(navController, viewModel) }
            composable(Routes.ATTENDANCE) { AttendanceScreen(navController, viewModel) }
        }
    }
}