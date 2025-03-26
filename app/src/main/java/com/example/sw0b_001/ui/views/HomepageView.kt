package com.example.sw0b_001.ui.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.appbars.RecentAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.collectAsState
import com.example.sw0b_001.Models.NavigationFlowHandler
import com.example.sw0b_001.ui.modals.GetStartedModal
import com.example.sw0b_001.ui.onboarding.OnboardingCompleteView
import com.example.sw0b_001.ui.onboarding.OnboardingSaveVaultView
import com.example.sw0b_001.ui.onboarding.OnboardingVaultStoreView
import com.example.sw0b_001.ui.onboarding.OnboardingWelcomeView

enum class BottomTabsItems {
    BottomBarRecentTab,
    BottomBarPlatformsTab,
    BottomBarInboxTab,
    BottomBarCountriesTab
}

enum class OnboardingState {
    Welcome,
    VaultStore,
    SaveVault,
    SendMessage,
    Complete
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageView(
    _messages: List<EncryptedContent> = emptyList<EncryptedContent>(),
    isLoggedIn: Boolean = false,
    navController: NavController,
    platformsViewModel : PlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    navigationFlowHandler: NavigationFlowHandler,
) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current

    val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    var hasSeenOnboarding by remember {
        mutableStateOf(sharedPreferences.getBoolean("hasSeenOnboarding", false))
    }
    var currentOnboardingState by remember { mutableStateOf(OnboardingState.Welcome) }

    var isLoggedIn by remember {
        mutableStateOf(
            if(inspectionMode) isLoggedIn else
            Vaults.fetchLongLivedToken(context).isNotBlank()
        )
    }

    val messages: List<EncryptedContent> = if(LocalInspectionMode.current) _messages
    else messagesViewModel.getMessages(context).observeAsState(emptyList()).value

    val inboxMessages: List<EncryptedContent> = if(LocalInspectionMode.current) _messages
    else messagesViewModel.getInboxMessages(context).observeAsState(emptyList()).value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAddGatewayClientsModal by remember { mutableStateOf(false) }

    val refreshSuccess = Runnable {
        Toast.makeText(context,
            context.getString(R.string.gateway_clients_refreshed_successfully), Toast.LENGTH_SHORT).show()
        Log.d("GatewayClients", "Gateway clients refreshed successfully!")
    }

    var sendNewMessageRequested by remember { mutableStateOf(false)}

    val isLoading by messagesViewModel.isLoading.collectAsState()

    if (!hasSeenOnboarding) {
        when (currentOnboardingState) {
            OnboardingState.Welcome -> {
                OnboardingWelcomeView (
                    onContinueClicked = { currentOnboardingState = OnboardingState.VaultStore },
                    onPrivacyPolicyClicked = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://relaysms.me/privacy-policy"))
                        context.startActivity(intent)
                    }
                )
            }

            OnboardingState.VaultStore -> {
                OnboardingVaultStoreView(
                    onBack = { currentOnboardingState = OnboardingState.Welcome },
                    onSkip = {
                        currentOnboardingState = OnboardingState.Complete
                    },
                    onContinue = { currentOnboardingState = OnboardingState.SaveVault }
                )
            }

            OnboardingState.SaveVault -> {
                OnboardingSaveVaultView(
                    onBack = { currentOnboardingState = OnboardingState.VaultStore },
                    onSkip = {
                        currentOnboardingState = OnboardingState.Complete
                    },
                    onContinue = { currentOnboardingState = OnboardingState.SendMessage }
                )
            }

            OnboardingState.SendMessage -> {
                OnboardingSaveVaultView(
                    onBack = { currentOnboardingState = OnboardingState.SaveVault },
                    onSkip = {
                        currentOnboardingState = OnboardingState.Complete
                    },
                    onContinue = { currentOnboardingState = OnboardingState.Complete }
                )
            }

            OnboardingState.Complete -> {
                OnboardingCompleteView(
                    onBack = { currentOnboardingState = OnboardingState.SendMessage },
                    onContinue = {
                        hasSeenOnboarding = true
                        with(sharedPreferences.edit()) {
                            putBoolean("hasSeenOnboarding", true)
                            apply()
                        }
                    }
                )
            }
        }
    } else {
        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                when(platformsViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        if (isLoggedIn || messages.isNotEmpty()) {
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
                }
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = platformsViewModel.bottomTabsItem,
                    isLoggedIn = isLoggedIn
                ) { selectedTab ->
                    platformsViewModel.bottomTabsItem = selectedTab
                }
            },
            floatingActionButton = {
                when(platformsViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        if(messages.isNotEmpty()) {
                            if(isLoggedIn) {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        sendNewMessageRequested = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.Message,
                                            contentDescription = "Add Account",
                                            tint = MaterialTheme.colorScheme.onSecondary
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Compose new",
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }
                                )
                            } else {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        sendNewMessageRequested = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.PersonAdd,
                                            contentDescription = "Add Account",
                                            tint = MaterialTheme.colorScheme.onSecondary
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Add account / Compose new",
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }
                                )
                            }
                        }
                    }
                    BottomTabsItems.BottomBarPlatformsTab -> {}
                    BottomTabsItems.BottomBarCountriesTab -> {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showAddGatewayClientsModal = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add New Gateway clients",
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            },
                            text = {
                                Text(
                                    text = "Add Number",
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
                                        contentDescription = "Paste new incoming message",
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
                }
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when(platformsViewModel.bottomTabsItem) {
                    BottomTabsItems.BottomBarRecentTab -> {
                        if (isLoggedIn || messages.isNotEmpty()) {
                            RecentView(
                                _messages = _messages,
                                navController = navController,
                                messagesViewModel = messagesViewModel,
                                platformsViewModel = platformsViewModel,
                            ) {
                                platformsViewModel.bottomTabsItem =
                                    BottomTabsItems.BottomBarPlatformsTab
                            }
                        } else if(!isLoading){
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

                if (sendNewMessageRequested) {
                    if(isLoggedIn) {
                        ActivePlatformsModal(
                            sendNewMessageRequested = sendNewMessageRequested,
                            platformsViewModel = platformsViewModel,
                            navController = navController,
                            isCompose = true
                        ) {
                            sendNewMessageRequested = false
                        }
                    } else {
                        GetStartedModal(sendNewMessageRequested, navController) {
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

}

@Preview(showBackground = false)
@Composable
fun HomepageView_Preview() {
    AppTheme(darkTheme = false) {
        HomepageView(
            navController = rememberNavController(),
            platformsViewModel = PlatformsViewModel(),
            messagesViewModel = MessagesViewModel(),
            gatewayClientViewModel = GatewayClientViewModel(),
            navigationFlowHandler = NavigationFlowHandler()
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
            gatewayClientViewModel = GatewayClientViewModel(),
            navigationFlowHandler = NavigationFlowHandler()
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
            platformsViewModel = PlatformsViewModel(),
            messagesViewModel = MessagesViewModel(),
            gatewayClientViewModel = GatewayClientViewModel(),
            navigationFlowHandler = NavigationFlowHandler()
        )
    }
}


