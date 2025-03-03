package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentsAppBar
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.serialization.Serializable


enum class BottomTabsItems {
    BottomBarRecentsTab,
    BottomBarPlatformsTab,
    BottomBarCountriesTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageView(navController: NavController) {
    var isLoggedIn by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var bottomBarItem by remember { mutableStateOf(BottomTabsItems.BottomBarRecentsTab) }

    var showAddGatewayClientsModal by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when(bottomBarItem) {
                BottomTabsItems.BottomBarRecentsTab -> {
                    if (isLoggedIn) {
                        RecentsAppBar(navController = navController)
                    }
                }
                BottomTabsItems.BottomBarPlatformsTab -> {}
                BottomTabsItems.BottomBarCountriesTab -> {
                    GatewayClientsAppBar(navController = navController)
                }
            }
        },
        bottomBar = {
            BottomNavBar { selectedTab ->
                bottomBarItem = selectedTab
            }
        },
        floatingActionButton = {
            when(bottomBarItem) {
                BottomTabsItems.BottomBarRecentsTab -> {}
                BottomTabsItems.BottomBarPlatformsTab -> {}
                BottomTabsItems.BottomBarCountriesTab -> {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        onClick = { showAddGatewayClientsModal = true }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add Gateway Client",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when(bottomBarItem) {
                BottomTabsItems.BottomBarRecentsTab -> {
                    if (isLoggedIn) {
                        RecentsView(navController = navController)
                    } else {
                        GetStartedView(navController = navController)
                    }
                }
                BottomTabsItems.BottomBarPlatformsTab -> {
                    AvailablePlatformsView(navController = navController)
                }
                BottomTabsItems.BottomBarCountriesTab -> {
                    GatewayClientView(addShowBottomSheet = showAddGatewayClientsModal) {
                        showAddGatewayClientsModal = false
                    }
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun HomepageView_Preview() {
    AppTheme(darkTheme = false) {
        HomepageView(navController = rememberNavController())
    }
}

