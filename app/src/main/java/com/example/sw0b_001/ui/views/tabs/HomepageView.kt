package com.example.sw0b_001.ui.views.tabs

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.threads.InboxView
import com.example.sw0b_001.ui.views.threads.RecentView
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
    navController: NavController,
    tokensViewModel : TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    showTopBar: Boolean = true,
    drawerCallback: (() -> Unit)? = {},
) {
    val context = LocalContext.current
    val inboxMessages = payloadsViewModel.getInboxMessages()
        .observeAsState(emptyList())

    val messages by payloadsViewModel.messages.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddGatewayClientsModal by remember { mutableStateOf(false) }

    var sendNewMessageRequested by remember { mutableStateOf(false)}

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if(showTopBar) {
                when (tokensViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        RecentAppBar(
                            navController = navController,
                            onSearchQueryChanged = { searchQuery = it },
                            searchQuery = searchQuery,
                            isSearchActive = isSearchActive,
                            onToggleSearch = {},
                            onSearchDone = {},
                            isSelectionMode = tokensViewModel.isSelectionMode,
                            selectedCount = tokensViewModel.selectedMessagesCount,
                            onSelectAll = tokensViewModel.onSelectAll,
                            onDeleteSelected = tokensViewModel.onDeleteSelected,
                            onCancelSelection = tokensViewModel.onCancelSelection,
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
            BottomNavBar( selectedTab = tokensViewModel.bottomTabsItem ) { selectedTab ->
                tokensViewModel.bottomTabsItem = selectedTab
            }
        },
        floatingActionButton = {
            when(tokensViewModel.bottomTabsItem) {
                BottomTabsItems.BottomBarRecentTab -> {
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
                    if (messages?.isNotEmpty() == true) {
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
                    if (inboxMessages.value.isNotEmpty()) {
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
                tokensViewModel.bottomTabsItem,
                navController = navController,
                payloadsViewModel = payloadsViewModel,
                tokensViewModel = tokensViewModel,
                gatewayClientViewModel = gatewayClientViewModel,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
            )

            if (sendNewMessageRequested) {
                ActivePlatformsModal(
                    sendNewMessageRequested = sendNewMessageRequested,
                    navController = navController,
                    isCompose = true,
                    supportedPlatformsViewModel = supportedPlatformsViewModel,
                    tokensViewModel = tokensViewModel,
                ) {
                    sendNewMessageRequested = false
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
    payloadsViewModel: PayloadsViewModel,
    tokensViewModel: TokensViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
) {
    when(bottomTabsItems) {
        BottomTabsItems.BottomBarRecentTab -> {
            RecentView(
                navController = navController,
                payloadsViewModel = payloadsViewModel,
                tokensViewModel = tokensViewModel,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
            ) {
                tokensViewModel.bottomTabsItem =
                    BottomTabsItems.BottomBarPlatformsTab
            }
        }
        BottomTabsItems.BottomBarPlatformsTab -> {
            SupportedPlatformsView(
                navController = navController,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                tokensViewModel = tokensViewModel,
            )
        }
        BottomTabsItems.BottomBarCountriesTab -> {
            GatewayClientView(viewModel = gatewayClientViewModel)
        }
        BottomTabsItems.BottomBarInboxTab -> {
            InboxView(
                payloadsViewModel = payloadsViewModel,
                tokensViewModel = tokensViewModel,
                navController = navController
            )
        }
    }

}