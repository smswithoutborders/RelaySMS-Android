package com.example.sw0b_001.ui.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.ui.features.AppFeatures
import com.example.sw0b_001.ui.features.FeatureInfo
import com.example.sw0b_001.ui.features.FeatureManager
import com.example.sw0b_001.ui.features.NewFeatureModal
import com.example.sw0b_001.ui.modals.GetStartedModal
import kotlinx.coroutines.launch


enum class BottomTabsItems {
    BottomBarRecentTab,
    BottomBarPlatformsTab,
    BottomBarInboxTab,
    BottomBarCountriesTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageView(
    _messages: List<EncryptedContent> = emptyList<EncryptedContent>(),
    navController: NavController,
    platformsViewModel : PlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    isLoggedIn: Boolean = false,
    showBottomBar: Boolean = true,
    showTopBar: Boolean = true,
    drawerCallback: (() -> Unit)? = {},
) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current

    var isLoggedIn by remember {
        mutableStateOf(
            if(inspectionMode) isLoggedIn else
            Vaults.fetchLongLivedToken(context).isNotBlank()
        )
    }

    val inboxMessages: List<EncryptedContent> = if(LocalInspectionMode.current) _messages
    else messagesViewModel.getInboxMessages(context).observeAsState(emptyList()).value

    val messagesPagingSource = messagesViewModel.getMessages(context = context)
    val messages = messagesPagingSource.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddGatewayClientsModal by remember { mutableStateOf(false) }

    val refreshSuccess = Runnable {
        Toast.makeText(context,
            context.getString(R.string.gateway_clients_refreshed_successfully), Toast.LENGTH_SHORT).show()
    }

    var sendNewMessageRequested by remember { mutableStateOf(false)}

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if(showTopBar) {
                when (platformsViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        RecentAppBar(
                            navController = navController,
                            onSearchQueryChanged = { searchQuery = it },
                            searchQuery = searchQuery,
                            isSearchActive = isSearchActive,
                            onToggleSearch = {},
                            onSearchDone = {},
                            isSelectionMode = platformsViewModel.isSelectionMode,
                            selectedCount = platformsViewModel.selectedMessagesCount,
                            onSelectAll = platformsViewModel.onSelectAll,
                            onDeleteSelected = platformsViewModel.onDeleteSelected,
                            onCancelSelection = platformsViewModel.onCancelSelection
                        )
                    }
                    BottomTabsItems.BottomBarCountriesTab -> {
                        GatewayClientsAppBar(
                            navController = navController,
                            onRefreshClicked = {
                                gatewayClientViewModel.get(context, refreshSuccess)
                            }
                        )
                    }
                    BottomTabsItems.BottomBarInboxTab -> {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.inbox),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors()
                        )
                    }
                    else -> {}
                }
            }
        },
        bottomBar = {
            if(showBottomBar) {
                BottomNavBar(
                    selectedTab = platformsViewModel.bottomTabsItem,
                    isLoggedIn = isLoggedIn,
                    onMenuChangedCallback = {
                        drawerCallback?.invoke()
                    }
                ) { selectedTab ->
                    platformsViewModel.bottomTabsItem = selectedTab
                }
            }
        },
        floatingActionButton = {
            when(platformsViewModel.bottomTabsItem) {
                BottomTabsItems.BottomBarRecentTab -> {
                    if(isLoggedIn) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                sendNewMessageRequested = true
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BubbleChart,
                                    contentDescription = stringResource(R.string.compose_new),
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.compose_new),
                                )
                            }
                        )
                    }
                    else if (LocalInspectionMode.current ||
                        (messages.loadState.isIdle && messages.itemCount > 0)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { sendNewMessageRequested = true },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = stringResource(R.string.add_account),
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.add_account_compose_new),
                                )
                            }
                        )
                    }
                }
                BottomTabsItems.BottomBarCountriesTab -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showAddGatewayClientsModal = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add_new_gateway_clients),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.add_number),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    )
                }
                BottomTabsItems.BottomBarInboxTab -> {
                    if (inboxMessages.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                navController.navigate(PasteEncryptedTextScreen)
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            icon = {
                                Icon(
                                    Icons.Filled.ContentPaste,
                                    contentDescription = stringResource(R.string.paste_new_incoming_message),
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.paste_message),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GetTabViews(
                platformsViewModel.bottomTabsItem,
                navController = navController,
                messagesViewModel = messagesViewModel,
                platformsViewModel = platformsViewModel,
                gatewayClientViewModel = gatewayClientViewModel,
                isLoggedIn = isLoggedIn,
            )

            if (sendNewMessageRequested) {
                if(isLoggedIn) {
                    ActivePlatformsModal(
                        sendNewMessageRequested = sendNewMessageRequested,
                        navController = navController,
                        isCompose = true
                    ) {
                        sendNewMessageRequested = false
                    }
                } else {
                    GetStartedModal(
                        sendNewMessageRequested,
                        navController = navController,
                        isLoggedIn = isLoggedIn,
                    ) {
                        sendNewMessageRequested = false
                    }
                }
            }

            if (showAddGatewayClientsModal) {
                AddGatewayClientModal(
                    showBottomSheet = showAddGatewayClientsModal,
                    onDismiss = { showAddGatewayClientsModal = false },
                    viewModel = gatewayClientViewModel,
                    onGatewayClientSaved = {
                        showAddGatewayClientsModal = false
                    }
                )
            }
        }
    }
}

@Composable
fun GetTabViews(
    bottomTabsItems: BottomTabsItems,
    navController: NavController,
    messagesViewModel: MessagesViewModel,
    platformsViewModel: PlatformsViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    isLoggedIn: Boolean,
) {
    when(bottomTabsItems) {
        BottomTabsItems.BottomBarRecentTab -> {
            RecentView(
                navController = navController,
                messagesViewModel = messagesViewModel,
                platformsViewModel = platformsViewModel,
                isLoggedIn = isLoggedIn
            ) {
                platformsViewModel.bottomTabsItem =
                    BottomTabsItems.BottomBarPlatformsTab
            }
        }
        BottomTabsItems.BottomBarPlatformsTab -> {
            AvailablePlatformsView(
                navController = navController,
            )
        }
        BottomTabsItems.BottomBarCountriesTab -> {
            GatewayClientView( viewModel = gatewayClientViewModel )
        }
        BottomTabsItems.BottomBarInboxTab -> {
            InboxView(
                messagesViewModel = messagesViewModel,
                platformsViewModel = platformsViewModel,
                navController = navController
            )
        }
    }

}


@Preview(showBackground = false)
@Composable
fun HomepageView_Preview() {
    AppTheme(darkTheme = false) {
        HomepageView(
            navController = rememberNavController(),
            platformsViewModel = remember{ PlatformsViewModel() },
            messagesViewModel = remember{ MessagesViewModel() },
            gatewayClientViewModel = remember{ GatewayClientViewModel() },
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
            platformsViewModel = remember{ PlatformsViewModel() },
            messagesViewModel = remember{ MessagesViewModel() },
            gatewayClientViewModel = remember{ GatewayClientViewModel() },
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
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "origin@gmail.com:dev@relaysms.me:::subject here:This is an encrypted content"

        HomepageView(
            _messages = listOf(encryptedContent),
            isLoggedIn = true,
            navController = rememberNavController(),
            platformsViewModel = remember{ PlatformsViewModel() },
            messagesViewModel = remember{ MessagesViewModel() },
            gatewayClientViewModel = remember{ GatewayClientViewModel() },
        )
    }
}
