package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PhoneForwarded
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.tabs.BottomTabsItems

@Composable
fun BottomNavBar(
    navController: NavController,
    selectedTab: BottomTabsItems,
    onChangeTab: (BottomTabsItems) -> Unit = {},
) {
    val context = LocalContext.current
    val isDefaultSmsApp = if (LocalInspectionMode.current) true else context.isDefault()

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {

            val isMessagesSelected = selectedTab == BottomTabsItems.BottomBarRecentTab
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = navIcon(
                            selected = isMessagesSelected,
                            filled = Icons.Filled.Chat,
                            outlined = Icons.Outlined.ChatBubbleOutline,
                        ),
                        contentDescription = stringResource(R.string.Messages),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.Messages),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isMessagesSelected,
                onClick = {
                    onChangeTab(BottomTabsItems.BottomBarRecentTab)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )

            val isPlatformsSelected = selectedTab == BottomTabsItems.BottomBarPlatformsTab
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = navIcon(
                            selected = isPlatformsSelected,
                            filled = Icons.Filled.GridView,
                            outlined = Icons.Outlined.GridView,
                        ),
                        contentDescription = stringResource(R.string.platforms),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.platforms),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isPlatformsSelected,
                onClick = {
                    onChangeTab(BottomTabsItems.BottomBarPlatformsTab)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )

            val isRoutingSelected = selectedTab == BottomTabsItems.BottomBarCountriesTab
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = navIcon(
                            selected = isRoutingSelected,
                            filled = Icons.Filled.PhoneForwarded,
                            outlined = Icons.Outlined.PhoneForwarded,
                        ),
                        contentDescription = stringResource(R.string.countries),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.routing_numbers),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isRoutingSelected,
                onClick = {
                    onChangeTab(BottomTabsItems.BottomBarCountriesTab)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private fun navIcon(
    selected: Boolean,
    filled: ImageVector,
    outlined: ImageVector,
): ImageVector = if (selected) filled else outlined

@Preview
@Composable
fun BottomNavBar_Preview() {
    AppTheme {
        BottomNavBar(
            navController = NavController(LocalContext.current),
            selectedTab = BottomTabsItems.BottomBarRecentTab
        )
    }
}