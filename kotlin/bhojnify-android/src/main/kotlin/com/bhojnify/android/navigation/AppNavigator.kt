package com.bhojnify.android.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhojnify.android.components.LocalSnackbarHostState
import com.bhojnify.android.screens.*
import com.bhojnify.android.viewmodel.MessViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val labelKey: String) {
    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home, "home")
    object Admin : BottomNavItem(Routes.ADMIN, Icons.Default.Settings, "admin")
    object Profile : BottomNavItem(Routes.PROFILE, Icons.Default.Person, "profile")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigator(
    viewModel: MessViewModel,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val isReady by viewModel.isReady.collectAsStateWithLifecycle()
    if (!isReady) {
        return
    }

    // Read initial route depending on onboarding complete
    val state by viewModel.state.collectAsStateWithLifecycle()
    val startDestination = if (state.onboardingComplete) Routes.HOME else Routes.ONBOARDING

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showNavigation = currentRoute in listOf(Routes.HOME, Routes.ADMIN, Routes.PROFILE)
    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact
    val items = listOf(BottomNavItem.Home, BottomNavItem.Admin, BottomNavItem.Profile)

    val navContent: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(Routes.ONBOARDING) { OnboardingScreen(navController, viewModel) }
            composable(Routes.HOME) { HomeScreen(navController, viewModel) }
            composable(Routes.CUSTOMERS) { CustomersScreen(navController, viewModel) }
            composable(Routes.ATTENDANCE) { AttendanceScreen(navController, viewModel) }
            composable(Routes.PAYMENTS) { PaymentsScreen(navController, viewModel) }
            composable(Routes.EXPENSES) { ExpensesScreen(navController, viewModel) }
            composable(Routes.INVENTORY) { InventoryScreen(navController, viewModel) }
            composable(Routes.STAFF) { StaffScreen(navController, viewModel) }
            composable(Routes.MENU) { MenuScreen(navController, viewModel) }
            composable(Routes.FEEDBACK) { FeedbackScreen(navController, viewModel) }
            composable(Routes.REPORT) { ReportScreen(navController, viewModel) }
            composable(Routes.ADMIN) { AdminScreen(navController, viewModel) }
            composable(Routes.PROFILE) { ProfileScreen(navController, viewModel) }
            composable(Routes.LEAVE) { LeaveScreen(navController, viewModel) }
        }
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        val isImeVisible = WindowInsets.isImeVisible
        if (isCompact) {
            Scaffold(
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showNavigation && !isImeVisible) {
                        NavigationBar {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                    ) {
                        navContent(Modifier.fillMaxSize())
                    }
                }
            }
        } else {
            Scaffold(
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    if (showNavigation) {
                        NavigationRail {
                            items.forEach { item ->
                                val labelText = viewModel.t(item.labelKey)
                                NavigationRailItem(
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                        ) {
                            navContent(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
