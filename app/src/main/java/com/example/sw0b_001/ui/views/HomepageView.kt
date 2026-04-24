package com.example.sw0b_001.ui.views

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.modals.GetStartedModal
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.AccountsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    _messages: List<Messages> = emptyList<Messages>(),
    navController: NavController,
    accountsViewModel : AccountsViewModel,
    messagesViewModel: MessagesViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    imageViewModel: ImageViewModel,
    isLoggedIn: Boolean = false,
    showTopBar: Boolean = true,
    drawerCallback: (() -> Unit)? = {},
) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current

    val inboxMessages: List<Messages> = if(LocalInspectionMode.current) _messages
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

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if(showTopBar) {
                when (accountsViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        RecentAppBar(
                            navController = navController,
                            onSearchQueryChanged = { searchQuery = it },
                            searchQuery = searchQuery,
                            isSearchActive = isSearchActive,
                            onToggleSearch = {},
                            onSearchDone = {},
                            isSelectionMode = accountsViewModel.isSelectionMode,
                            selectedCount = accountsViewModel.selectedMessagesCount,
                            onSelectAll = accountsViewModel.onSelectAll,
                            onDeleteSelected = accountsViewModel.onDeleteSelected,
                            onCancelSelection = accountsViewModel.onCancelSelection,
                            onMenuClickCallback = drawerCallback
                        )
                    }
                    BottomTabsItems.BottomBarCountriesTab -> {
                        GatewayClientsAppBar(
                            navController = navController,
                            onAddClicked = {
                                showAddGatewayClientsModal = true
                            },
                            onRefreshClicked = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    gatewayClientViewModel.fetch()
                                }
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
            BottomNavBar(
                selectedTab = accountsViewModel.bottomTabsItem,
                isLoggedIn = isLoggedIn,
            ) { selectedTab ->
                accountsViewModel.bottomTabsItem = selectedTab
            }
        },
        floatingActionButton = {
            when(accountsViewModel.bottomTabsItem) {
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
                accountsViewModel.bottomTabsItem,
                navController = navController,
                messagesViewModel = messagesViewModel,
                accountsViewModel = accountsViewModel,
                gatewayClientViewModel = gatewayClientViewModel,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                isLoggedIn = isLoggedIn,
            )

            if (sendNewMessageRequested) {
                if(isLoggedIn) {
                    ActivePlatformsModal(
                        sendNewMessageRequested = sendNewMessageRequested,
                        navController = navController,
                        isCompose = true,
                        isLoggedIn = true,
                        supportedPlatformsViewModel = supportedPlatformsViewModel,
                        accountsViewModel = accountsViewModel,
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
    accountsViewModel: AccountsViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    isLoggedIn: Boolean,
) {
    when(bottomTabsItems) {
        BottomTabsItems.BottomBarRecentTab -> {
            RecentView(
                navController = navController,
                messagesViewModel = messagesViewModel,
                accountsViewModel = accountsViewModel,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                isLoggedIn = isLoggedIn
            ) {
                accountsViewModel.bottomTabsItem =
                    BottomTabsItems.BottomBarPlatformsTab
            }
        }
        BottomTabsItems.BottomBarPlatformsTab -> {
            SupportedPlatformsView(
                navController = navController,
                isLoggedIn = true,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                accountsViewModel = accountsViewModel,
            )
        }
        BottomTabsItems.BottomBarCountriesTab -> {
            GatewayClientView( viewModel = gatewayClientViewModel )
        }
        BottomTabsItems.BottomBarInboxTab -> {
            InboxView(
                messagesViewModel = messagesViewModel,
                accountsViewModel = accountsViewModel,
                navController = navController
            )
        }
    }

}