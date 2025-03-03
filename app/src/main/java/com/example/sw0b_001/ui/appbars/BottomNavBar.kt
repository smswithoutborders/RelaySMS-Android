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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun BottomNavBar(
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
//        containerColor = MaterialTheme.colorScheme.primary,
    ) {
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
            selected = true,
            onClick = {
                navController.navigate(HomepageScreen) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
//            onClick = { navController.navigate(RecentScreen)},
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
            selected = currentRoute == Screen.AvailablePlatforms.route,
            onClick = {
                navController.navigate(Screen.AvailablePlatforms.route) {
                    launchSingleTop = true
                    restoreState = true
                }
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
            selected = currentRoute == Screen.GatewayClients.route,
            onClick = {
                navController.navigate(Screen.GatewayClients.route) {
                    launchSingleTop = true
                    restoreState = true
                }
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


@Preview(showBackground = true)
@Composable
fun RelayBottomNavBarPreview() {
    AppTheme(darkTheme = false) {
        BottomNavBar (
            navController = NavController(LocalContext.current)
        )
    }
}