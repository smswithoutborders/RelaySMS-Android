package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.SettingsScreen
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.ChatBubble,
                            contentDescription = stringResource(R.string.Messages),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.Messages),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selectedTab == BottomTabsItems.BottomBarRecentTab,
                    onClick = {
                        onChangeTab(BottomTabsItems.BottomBarRecentTab)
                    },
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.platforms),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.platforms),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selectedTab == BottomTabsItems.BottomBarPlatformsTab,
                    onClick = {
                        onChangeTab(BottomTabsItems.BottomBarPlatformsTab,)
                    },
                )


                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Public,
                            contentDescription = stringResource(R.string.countries),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.countries),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selectedTab == BottomTabsItems.BottomBarCountriesTab,
                    onClick = {
                        onChangeTab(BottomTabsItems.BottomBarCountriesTab,)
                    },
                )


                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.Settings),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.Settings),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = false,
                    onClick = {
                        navController.navigate(SettingsScreen)
                    },
                )


            }
        }
    }
}



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