package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WarehouseViewModel

enum class NavigationTab(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    DASHBOARD("dashboard", "Beranda", Icons.Default.Dashboard),
    STOCK("stock", "Stok", Icons.Default.Inventory2),
    TRANSACTIONS("transactions", "Mutasi", Icons.Default.SwapHoriz),
    SUPPLIER("supplier", "Pemasok", Icons.Default.Business),
    REPORTS("reports", "Laporan AI", Icons.Default.AutoAwesome),
    SETTINGS("settings", "Pengaturan", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WarehouseViewModel = viewModel()
            val themeColor by viewModel.themeColor.collectAsState()

            MyApplicationTheme(themeType = themeColor) {
                // Trigger first-launch DB seed data if required
                LaunchedEffect(Unit) {
                    viewModel.seedDataIfNeeded()
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavigationTab.DASHBOARD.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentRoute == tab.route,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(NavigationTab.DASHBOARD.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title, fontSize = 10.sp) },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_item_${tab.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationTab.DASHBOARD.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationTab.DASHBOARD.route) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToStock = {
                                    navController.navigate(NavigationTab.STOCK.route) {
                                        popUpTo(NavigationTab.DASHBOARD.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(NavigationTab.STOCK.route) {
                            StockScreen(viewModel = viewModel)
                        }
                        composable(NavigationTab.TRANSACTIONS.route) {
                            TransactionScreen(viewModel = viewModel)
                        }
                        composable(NavigationTab.SUPPLIER.route) {
                            SupplierScreen(viewModel = viewModel)
                        }
                        composable(NavigationTab.REPORTS.route) {
                            ReportScreen(viewModel = viewModel)
                        }
                        composable(NavigationTab.SETTINGS.route) {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
