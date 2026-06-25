package com.example.sw0b_001.ui.views.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.threads.RecentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class BottomTabsItems {
    BottomBarRecentTab,
    BottomBarPlatformsTab,
    BottomBarCountriesTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageView(
    navController: NavController,
    tokensViewModel: TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    showTopBar: Boolean = true
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showAddGatewayClientsModal by remember { mutableStateOf(false) }
    var sendNewMessageRequested by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (showTopBar) {
                when (tokensViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        RecentAppBar(
                            onSearchQueryChanged = { searchQuery = it },
                            searchQuery = searchQuery,
                            isSearchActive = isSearchActive,
                            onToggleSearch = { isSearchActive = !isSearchActive },
                            onSearchDone = {},
                            isSelectionMode = tokensViewModel.isSelectionMode,
                            selectedCount = tokensViewModel.selectedMessagesCount,
                            onSelectAll = tokensViewModel.onSelectAll,
                            onDeleteSelected = tokensViewModel.onDeleteSelected,
                            onCancelSelection = tokensViewModel.onCancelSelection,
                            onComposeClicked = {
                                sendNewMessageRequested = true
                            }
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

                    else -> {}
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedTab = tokensViewModel.bottomTabsItem
            ) { selectedTab ->
                tokensViewModel.bottomTabsItem = selectedTab
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
    when (bottomTabsItems) {
        BottomTabsItems.BottomBarRecentTab -> {
            RecentView(
                navController = navController,
                payloadsViewModel = payloadsViewModel
            )
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
    }
}