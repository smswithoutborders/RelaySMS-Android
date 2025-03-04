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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.ui.views.BottomTabsItems

@Composable
fun BottomNavBar(
    selectedTab: BottomTabsItems,
    isLoggedIn: Boolean = true,
    onChangeTab: (BottomTabsItems) -> Unit = {}
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(
                Icons.Filled.Home,
                contentDescription = "Recents",
                modifier = Modifier.size(20.dp)
            ) },
            label = {
                Text(
                    text = if(isLoggedIn)  "Recents" else "Get started",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedTab == BottomTabsItems.BottomBarRecentTab,
            onClick = {
                onChangeTab(BottomTabsItems.BottomBarRecentTab)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        if (isLoggedIn) {
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
                selected = selectedTab == BottomTabsItems.BottomBarPlatformsTab,
                onClick = {
                    onChangeTab(BottomTabsItems.BottomBarPlatformsTab,)
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
            selected = selectedTab == BottomTabsItems.BottomBarCountriesTab,
            onClick = {
                onChangeTab(BottomTabsItems.BottomBarCountriesTab,)
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