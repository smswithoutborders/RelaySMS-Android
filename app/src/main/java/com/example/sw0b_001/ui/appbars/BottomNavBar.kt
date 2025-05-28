package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
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
                contentDescription = stringResource(R.string.recents),
                modifier = Modifier.size(20.dp)
            ) },
            label = {
                Text(
                    text = if(isLoggedIn) stringResource(R.string.recents_text) else stringResource(
                        R.string.get_started_text
                    ),
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
                    contentDescription = stringResource(R.string.platforms),
                    modifier = Modifier.size(20.dp)
                ) },
                label = { Text(
                    text = stringResource(R.string.platforms),
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
                Icons.Filled.Inbox,
                contentDescription = stringResource(R.string.inbox),
                modifier = Modifier.size(20.dp)
            ) },
            label = { Text(
                text = stringResource(R.string.inbox),
                style = MaterialTheme.typography.labelSmall
            ) },
            selected = selectedTab == BottomTabsItems.BottomBarInboxTab,
            onClick = {
                onChangeTab(BottomTabsItems.BottomBarInboxTab,)
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
                contentDescription = stringResource(R.string.countries),
                modifier = Modifier.size(20.dp)
            ) },
            label = { Text(
                text = stringResource(R.string.countries),
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