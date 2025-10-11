package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.BottomTabsItems

@Composable
fun BottomNavBar(
    selectedTab: BottomTabsItems,
    isLoggedIn: Boolean = true,
    onChangeTab: (BottomTabsItems) -> Unit = {}
) {
    val context = LocalContext.current
    val isDefaultSmsApp = if(LocalInspectionMode.current) true else context.isDefault()

    NavigationBar {
        if(isDefaultSmsApp || LocalInspectionMode.current) {
            NavigationBarItem(
                icon = { Icon(
                    Icons.Filled.Inbox,
                    contentDescription = stringResource(R.string.sms_mms),
                    modifier = Modifier.size(20.dp)
                ) },
                label = {
                    Text(
                        text = stringResource(R.string.sms_mms),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selectedTab == BottomTabsItems.BottomBarSmsMmsTab,
                onClick = {
                    onChangeTab(BottomTabsItems.BottomBarSmsMmsTab)
                },
            )
        }

        NavigationBarItem(
            icon = { Icon(
                Icons.Filled.Home,
                contentDescription = stringResource(R.string.recents),
                modifier = Modifier.size(20.dp)
            ) },
            label = {
                Text(
                    text = if(isLoggedIn) stringResource(R.string.recents_text)
                    else stringResource( R.string.get_started_text ),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedTab == BottomTabsItems.BottomBarRecentTab,
            onClick = {
                onChangeTab(BottomTabsItems.BottomBarRecentTab)
            },
        )

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
        )

        if(!isDefaultSmsApp || LocalInspectionMode.current) {
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
            )
        }

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
        )

    }
}

@Preview
@Composable
fun BottomNavBar_Preview() {
    AppTheme {
        BottomNavBar(
            selectedTab = BottomTabsItems.BottomBarRecentTab
        )
    }
}
