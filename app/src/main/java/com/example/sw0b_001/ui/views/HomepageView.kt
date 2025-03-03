package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.sw0b_001.ui.appbars.RecentsAppBar
import kotlinx.serialization.Serializable


enum class BottomTabsItems {
    BottomBarRecentsTab,
    BottomBarPlatformsTab,
    BottomBarCountriesTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun HomepageView(navController: NavController = rememberNavController()) {
    var isLoggedIn by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var bottomBarItem by remember { mutableStateOf(BottomTabsItems.BottomBarRecentsTab) }

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isLoggedIn) {
                RecentsAppBar(navController = navController)
            }
        },
        bottomBar = {
            BottomNavBar { selectedTab ->
                bottomBarItem = selectedTab
            }
        },
        floatingActionButton = {}
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
                    GatewayClientView(navController = navController)
                }
            }
        }
    }
}