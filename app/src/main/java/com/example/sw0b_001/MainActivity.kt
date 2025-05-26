package com.example.sw0b_001

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.ui.views.CreateAccountView
import com.example.sw0b_001.ui.views.LoginView
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import com.example.sw0b_001.ui.views.compose.EmailComposeView
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeView
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.ui.components.MissingTokenAccountInfo
import com.example.sw0b_001.ui.components.MissingTokenInfoDialog
import com.example.sw0b_001.ui.navigation.AboutScreen
import com.example.sw0b_001.ui.navigation.BridgeEmailComposeScreen
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.ForgotPasswordScreen
import com.example.sw0b_001.ui.navigation.GetMeOutScreen
import com.example.sw0b_001.ui.navigation.MessageComposeScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.OnboardingScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.navigation.TextComposeScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
import com.example.sw0b_001.ui.onboarding.MainOnboarding
import com.example.sw0b_001.ui.onboarding.OnboardingStep
import com.example.sw0b_001.ui.onboarding.PREF_USER_ONBOARDED
import com.example.sw0b_001.ui.onboarding.USER_ONBOARDED
import com.example.sw0b_001.ui.views.ForgotPasswordView
import com.example.sw0b_001.ui.views.GetMeOutOfHere
import com.example.sw0b_001.ui.views.PasteEncryptedTextView
import com.example.sw0b_001.ui.views.details.EmailDetailsView
import com.example.sw0b_001.ui.views.details.MessageDetailsView
import com.example.sw0b_001.ui.views.details.TextDetailsView
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import androidx.core.content.edit


enum class OnboardingState {
    Welcome,
    VaultStore,
    SaveVault,
    SendMessage,
    Complete
}

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    val platformsViewModel: PlatformsViewModel by viewModels()
    val messagesViewModel: MessagesViewModel by viewModels()
    val gatewayClientViewModel: GatewayClientViewModel by viewModels()

    var showMissingTokenDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val composeView = LocalView.current

            DisposableEffect(Unit) {
                composeView.filterTouchesWhenObscured = true
                onDispose {
                    composeView.filterTouchesWhenObscured = false
                }
            }

            AppTheme {
                navController = rememberNavController()

                LaunchedEffect(platformsViewModel.accountsForMissingDialog) {
                    refreshTokensCallback(platformsViewModel.accountsForMissingDialog)
                }

                if (showMissingTokenDialog) {
                    MissingTokenInfoDialog(
                        groupedAccounts = platformsViewModel.accountsForMissingDialog,
                        onDismiss = { showMissingTokenDialog = false },
                        onConfirm = { doNotShowAgain ->
                            showMissingTokenDialog = false
                            if (doNotShowAgain) {
                                PreferenceManager
                                    .getDefaultSharedPreferences(applicationContext).edit {
                                        putBoolean(
                                            Vaults.Companion.PrefKeys
                                                .KEY_DO_NOT_SHOW_MISSING_TOKEN_DIALOG,
                                            true
                                        )
                                    }
                            }
                        }
                    )
                }

                Surface(
                    Modifier
                        .fillMaxSize()
                ) {
                    MainNavigation(navController = navController)
                }
            }
        }
    }

    @Composable
    fun MainNavigation(navController: NavHostController) {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences(PREF_USER_ONBOARDED, MODE_PRIVATE)

        var hasSeenOnboarding by remember {
            mutableStateOf(sharedPreferences.getBoolean(USER_ONBOARDED, false))
        }

        NavHost(
            navController = navController,
            startDestination = if(hasSeenOnboarding) HomepageScreen else OnboardingScreen,
        ) {
            composable<OnboardingScreen> {
                MainOnboarding(navController)
            }
            composable<GetMeOutScreen> {
                GetMeOutOfHere(navController)
            }
            composable<HomepageScreen> {
                HomepageView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
                    gatewayClientViewModel = gatewayClientViewModel,
                )
            }
            composable<LoginScreen> {
                LoginView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<ForgotPasswordScreen> {
                ForgotPasswordView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<CreateAccountScreen> {
                CreateAccountView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<OTPCodeScreen> {
                OtpCodeVerificationView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<AboutScreen> {
                AboutView(navController = navController)
            }

            composable<BridgeEmailComposeScreen> {
                EmailComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    isBridge = true
                )
            }
            composable<EmailComposeScreen> {
                EmailComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<TextComposeScreen> {
                TextComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<MessageComposeScreen> {
                MessageComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<EmailViewScreen> {
                EmailDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<BridgeViewScreen> {
                EmailDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    isBridge = true
                )
            }
            composable<TextViewScreen> {
                TextDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<MessageViewScreen> {
                MessageDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<PasteEncryptedTextScreen> {
                PasteEncryptedTextView(
                    platformsViewModel = platformsViewModel,
                    navController = navController,
                )
            }
        }
    }

    fun refreshTokensCallback(accountsInfo: Map<String, List<String>> ){
        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
        val doNotShowDialog = sharedPreferences
            .getBoolean(Vaults.Companion.PrefKeys
                .KEY_DO_NOT_SHOW_MISSING_TOKEN_DIALOG, false)

        if (!doNotShowDialog) {
            showMissingTokenDialog = true
            platformsViewModel.accountsForMissingDialog = accountsInfo
        }

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(Vaults.isGetMeOut(applicationContext)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(GetMeOutScreen) {
                            popUpTo(HomepageScreen) {
                                inclusive = true
                            }
                        }
                    }
                } else {
                    Vaults.fetchLongLivedToken(applicationContext).let {
                        if(it.isNotEmpty()) {
                            val vault = Vaults(applicationContext)
                            try {
                                vault.refreshStoredTokens(applicationContext, ) {
                                    if(it.isNotEmpty())
                                        refreshTokensCallback(it)
                                }
                            } catch(e: StatusRuntimeException) {
                                if(e.status.code == Status.UNAUTHENTICATED.code) {
                                    Vaults.setGetMeOut(applicationContext, true)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(GetMeOutScreen) {
                                            popUpTo(HomepageScreen) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            } finally {
                                vault.shutdown()
                            }

                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }

        Platforms.refreshAvailablePlatforms(applicationContext)
    }
}
