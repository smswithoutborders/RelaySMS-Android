package com.example.sw0b_001.ui.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.theme.AppTheme

enum class BottomTabsItems {
    BottomBarRecentTab,
    BottomBarPlatformsTab,
    BottomBarCountriesTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageView(
    _messages: List<EncryptedContent> = emptyList<EncryptedContent>(),
    isLoggedIn: Boolean = false,
    navController: NavController,
    platformsViewModel : PlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    gatewayClientViewModel: GatewayClientViewModel
) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current

    var isLoggedIn by remember {
        mutableStateOf(
            if(inspectionMode) isLoggedIn else
            Vaults.fetchLongLivedToken(context).isNotBlank()
        )
    }

    val messages: List<EncryptedContent> = if(_messages.isNotEmpty()) _messages
    else messagesViewModel.getMessages(context).observeAsState(emptyList()).value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var bottomBarItem by remember { mutableStateOf(BottomTabsItems.BottomBarRecentTab) }

    var showAddGatewayClientsModal by remember { mutableStateOf(false) }

    val refreshSuccess = Runnable {
        Toast.makeText(context, "Gateway clients refreshed successfully!", Toast.LENGTH_SHORT).show()
        Log.d("GatewayClients", "Gateway clients refreshed successfully!")
    }

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when(bottomBarItem) {
                BottomTabsItems.BottomBarRecentTab -> {
                    if (isLoggedIn) {
                        RecentAppBar(
                            navController = navController,
                            isSearchable = messages.isNotEmpty()
                        )
                    }
                }
                BottomTabsItems.BottomBarPlatformsTab -> {}
                BottomTabsItems.BottomBarCountriesTab -> {
                    GatewayClientsAppBar(
                        navController = navController,
                        onRefreshClicked = {
                            gatewayClientViewModel.get(context, refreshSuccess)
                        }
                    )
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = bottomBarItem,
                isLoggedIn = isLoggedIn
            ) { selectedTab ->
                bottomBarItem = selectedTab
            }
        },
        floatingActionButton = {
            when(bottomBarItem) {
                BottomTabsItems.BottomBarRecentTab -> {
                    if(messages.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.End) {
                            ExtendedFloatingActionButton(
                                onClick = {

                                },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Create,
                                        contentDescription = "Compose Message",
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Compose",
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            )
                        }
                    }
                }
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
                BottomTabsItems.BottomBarRecentTab -> {
                    if (isLoggedIn) {
                        RecentView(
                            _messages = _messages,
                            navController = navController,
                            messagesViewModel = messagesViewModel,
                            platformsViewModel = platformsViewModel
                        ) {
                            bottomBarItem = BottomTabsItems.BottomBarPlatformsTab
                        }
                    } else {
                        GetStartedView(navController = navController)
                    }
                }
                BottomTabsItems.BottomBarPlatformsTab -> {
                    AvailablePlatformsView(
                        navController = navController,
                        platformsViewModel = platformsViewModel
                    )
                }
                BottomTabsItems.BottomBarCountriesTab -> {
                    GatewayClientView(addShowBottomSheet = showAddGatewayClientsModal, viewModel = gatewayClientViewModel) {
//                        showAddGatewayClientsModal = false
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
        HomepageView(
            navController = rememberNavController(),
            platformsViewModel = PlatformsViewModel(),
            messagesViewModel = MessagesViewModel(),
            gatewayClientViewModel = GatewayClientViewModel()
        )
    }
}


@Preview(showBackground = false)
@Composable
fun HomepageViewLoggedIn_Preview() {
    AppTheme(darkTheme = false) {
        HomepageView(
            isLoggedIn = true,
            navController = rememberNavController(),
            platformsViewModel = PlatformsViewModel(),
            messagesViewModel = MessagesViewModel(),
            gatewayClientViewModel = GatewayClientViewModel()
        )
    }
}


@Preview(showBackground = false)
@Composable
fun HomepageViewLoggedInMessages_Preview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.platformId = ""
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "This is an encrypted content"

        HomepageView(
            _messages = listOf(encryptedContent),
            isLoggedIn = true,
            navController = rememberNavController(),
            platformsViewModel = PlatformsViewModel(),
            messagesViewModel = MessagesViewModel(),
            gatewayClientViewModel = GatewayClientViewModel()
        )
    }
}


