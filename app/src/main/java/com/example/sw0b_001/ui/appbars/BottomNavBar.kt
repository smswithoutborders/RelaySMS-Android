package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.BottomTabsItems
import kotlinx.serialization.Serializable

@Composable
@Preview
fun BottomNavBar(
    selectedTab: BottomTabsItems = BottomTabsItems.BottomBarRecentsTab,
    onChangeTab: (BottomTabsItems) -> Unit = {}
) {
    var selectedTab = remember { mutableStateOf(selectedTab) }

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(
                Icons.Filled.Home,
                contentDescription = "Recents",
                modifier = Modifier.size(20.dp)
            ) },
            label = {
                Text(
                    text = "Recents",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedTab.value == BottomTabsItems.BottomBarRecentsTab,
            onClick = {
                selectedTab.value = BottomTabsItems.BottomBarRecentsTab
                onChangeTab(selectedTab.value)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(
                Icons.Filled.PhoneAndroid,
                contentDescription = "Platforms",
                modifier = Modifier.size(20.dp)
            ) },
            label = { Text(
                text = "Platforms",
                style = MaterialTheme.typography.labelSmall
            ) },
            selected = selectedTab.value == BottomTabsItems.BottomBarPlatformsTab,
            onClick = {
                selectedTab.value = BottomTabsItems.BottomBarPlatformsTab
                onChangeTab(selectedTab.value)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(
                Icons.Filled.Public,
                contentDescription = "Countries",
                modifier = Modifier.size(20.dp)
            ) },
            label = { Text(
                text = "Countries",
                style = MaterialTheme.typography.labelSmall
            ) },
            selected = selectedTab.value == BottomTabsItems.BottomBarCountriesTab,
            onClick = {
                selectedTab.value = BottomTabsItems.BottomBarCountriesTab
                onChangeTab(selectedTab.value)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

    }
}