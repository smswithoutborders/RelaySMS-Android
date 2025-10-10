package com.example.sw0b_001

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.ui.views.CreateAccountView
import com.example.sw0b_001.ui.views.LoginView
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.ui.components.MissingTokenInfoDialog
import com.example.sw0b_001.ui.navigation.AboutScreen
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.ForgotPasswordScreen
import com.example.sw0b_001.ui.navigation.GetMeOutScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
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
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.toRoute
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.lib_image_android.ui.components.ImageRender
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NEW_NOTIFICATION_ACTION
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.NavHostControllerInstance
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.requiredReadPhoneStatePermissions
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsGetLockDownApp
import com.example.sw0b_001.extensions.context.settingsGetOnboardedCompletely
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.HomepageScreenRelay
import com.example.sw0b_001.ui.navigation.MessageComposeNav
import com.example.sw0b_001.ui.navigation.OnboardingInteractiveScreen
import com.example.sw0b_001.ui.navigation.OnboardingSkipScreen
import com.example.sw0b_001.ui.navigation.TextComposeNav
import com.example.sw0b_001.ui.navigation.WelcomeScreen
import com.example.sw0b_001.ui.onboarding.OnboardingInteractive
import com.example.sw0b_001.ui.views.WelcomeMainView
import com.example.sw0b_001.ui.viewModels.OnboardingViewModel
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.compose.ComposerInterface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController

    val threadsViewModel: ThreadsViewModel by viewModels()
    val onboardingViewModel: OnboardingViewModel by viewModels()

    private lateinit var searchViewModel: SearchViewModel

    val platformsViewModel: PlatformsViewModel by viewModels()
    val messagesViewModel: MessagesViewModel by viewModels()
    val gatewayClientViewModel: GatewayClientViewModel by viewModels()
    val imageViewModel: ImageViewModel by viewModels()

    var showMissingTokenDialog by mutableStateOf(false)

    var loggedInAlready by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Fix for three-button nav not properly going edge-to-edge.
            // TODO: https://issuetracker.google.com/issues/298296168
            window.isNavigationBarContrastEnforced = false
        }

        searchViewModel = SearchViewModel(getDatabase().threadsDao()!!)

        fun beginAppLifecycle() {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    WindowInfoTracker.getOrCreate(this@MainActivity)
                        .windowLayoutInfo(this@MainActivity)
                        .collect { newLayoutInfo ->
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

                                    LaunchedEffect(true) {
                                        refreshTokensCallback(platformsViewModel
                                            .accountsForMissingDialog)
                                    }

                                    LaunchedEffect(loggedInAlready) {
                                        if(loggedInAlready) {
                                            navController.navigate(GetMeOutScreen) {
                                                popUpTo(HomepageScreen) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    }

                                    if (showMissingTokenDialog) {
                                        MissingTokenInfoDialog(
                                            groupedAccounts = platformsViewModel.accountsForMissingDialog,
                                            onDismiss = { showMissingTokenDialog = false },
                                            onConfirm = { doNotShowAgain ->
                                                showMissingTokenDialog = false
                                                if (doNotShowAgain) {
                                                    PreferenceManager
                                                        .getDefaultSharedPreferences(applicationContext)
                                                        .edit {
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

                                    Surface( Modifier.fillMaxSize()) {
                                        if(LocalContext.current.isDefault()) {
                                            platformsViewModel.bottomTabsItem =
                                                BottomTabsItems.BottomBarSmsMmsTab
                                        }
                                        MainNavigation(navController = navController, newLayoutInfo)
                                    }
                                }

                            }
                        }
                }
            }
        }

        if(!settingsGetOnboardedCompletely) {
            beginAppLifecycle()
        }
        else securityChecks {
            beginAppLifecycle()
        }
    }

    private fun securityChecks(callback: () -> Unit) {
        if(settingsGetLockDownApp) {
            promptBiometrics(this) {
                if(!it) {
                    finish()
                    exitProcess(0)
                } else {
                    callback()
                }
            }
        } else {
            callback()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MainNavigation(
        navController: NavHostController,
        newLayoutInfo: WindowLayoutInfo,
    ) {
        val context = LocalContext.current
        val inPreview = LocalInspectionMode.current
        var defaultSmsApp by remember { mutableStateOf(inPreview || context.isDefault()) }

        val readPhoneStatePermission = rememberPermissionState(requiredReadPhoneStatePermissions)
        LaunchedEffect(readPhoneStatePermission.status) {
            defaultSmsApp = context.isDefault()
        }

        var hasSeenOnboarding by remember {
            mutableStateOf(context.settingsGetOnboardedCompletely)
        }

        var isLoggedIn by remember {
            mutableStateOf(
                if(inPreview) true else
                    Vaults.fetchLongLivedToken(context).isNotBlank()
            )
        }

        var showThreadsTopBar by remember { mutableStateOf(true) }
        var customThreadView: (@Composable () -> Unit)? by remember { mutableStateOf(null)}

        LaunchedEffect(platformsViewModel.bottomTabsItem, defaultSmsApp) {
            customThreadView = when(platformsViewModel.bottomTabsItem) {
                BottomTabsItems.BottomBarSmsMmsTab -> {
                    showThreadsTopBar = true
                    null
                }
                else -> {
                    {
                        showThreadsTopBar = !defaultSmsApp
                        HomepageView(
                            navController = navController,
                            platformsViewModel = platformsViewModel,
                            messagesViewModel = messagesViewModel,
                            gatewayClientViewModel = gatewayClientViewModel,
                            showBottomBar = true,
                        )
                    }
                }
            }
        }

        NavHostControllerInstance(
            newLayoutInfo,
            navController,
            threadsViewModel,
            searchViewModel,
            appName = stringResource(R.string.app_name),
            showThreadsTopBar = showThreadsTopBar,
            startDestination = if(hasSeenOnboarding) {
                if(defaultSmsApp) HomeScreenNav() else HomepageScreen
            } else WelcomeScreen,
            customBottomBar = {
                BottomNavBar(
                    selectedTab = platformsViewModel.bottomTabsItem,
                    isLoggedIn = isLoggedIn,
//                    isDefaultSmsApp = false
                ) { selectedTab ->
                    platformsViewModel.bottomTabsItem = selectedTab
                }
            },
            customThreadsView = customThreadView,
        ) {
            composable<WelcomeScreen> {
                WelcomeMainView(navController)
            }
            composable<OnboardingInteractiveScreen> {
                OnboardingInteractive(
                    navController,
                    onboardingViewModel,
                )
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
            composable<LoginScreen> { backEntry ->
                val loginNav: LoginScreen = backEntry.toRoute()
                LoginView(
                    navController = navController,
                    isOnboarding = loginNav.isOnboarding,
                )
            }
            composable<ForgotPasswordScreen> { backEntry ->
                val forgotPasswordNav: ForgotPasswordScreen = backEntry.toRoute()
                ForgotPasswordView(
                    navController = navController,
                    isOnboarding = forgotPasswordNav.isOnboarding,
                )
            }
            composable<CreateAccountScreen> { backEntry ->
                val createAccountNav: ForgotPasswordScreen = backEntry.toRoute()
                CreateAccountView(
                    navController = navController,
                    isOnboarding = createAccountNav.isOnboarding,
                )
            }
            composable<OTPCodeScreen> { backEntry ->
                val otpCodeNav: OTPCodeScreen = backEntry.toRoute()
                OtpCodeVerificationView(
                    navController = navController,
                    loginSignupPhoneNumber = otpCodeNav.loginSignupPhoneNumber,
                    loginSignupPassword = otpCodeNav.loginSignupPassword,
                    countryCode = otpCodeNav.countryCode,
                    otpRequestType = otpCodeNav.otpRequestType,
                    nextAttemptTimestamp = otpCodeNav.nextAttemptTimestamp,
                    platformViewModel = platformsViewModel,
                    onCompleteCallback = if(otpCodeNav.isOnboarding)
                        onboardingViewModel.callback else null,
                )
            }
            composable<AboutScreen> {
                AboutView(navController = navController)
            }

            composable<ComposeScreen> { backEntry ->
                val composeScreenNav: ComposeScreen = backEntry.toRoute()
                ComposerInterface(
                    navController = navController,
                    type = composeScreenNav.type,
                    messageId = composeScreenNav.messageId,
                    imageViewModel = imageViewModel,
                    messagesViewModel = messagesViewModel,
                    onSendCallback = if(composeScreenNav.isOnboarding)
                        onboardingViewModel.callback else null
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

            composable<ImageRenderNav>{ backStackEntry ->
                val imageRenderNav: ImageRenderNav = backStackEntry.toRoute()
                ImageRender(
                    navController = navController,
                    imageViewModel = imageViewModel,
                    initialize = imageRenderNav.initialize
                )
            }
        }

        processIntent(navController)
    }

    private fun refreshTokensCallback(accountsInfo: Map<String, List<String>> ){
        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
        val doNotShowDialog = sharedPreferences
            .getBoolean(Vaults.Companion.PrefKeys
                .KEY_DO_NOT_SHOW_MISSING_TOKEN_DIALOG, false)

        if (!doNotShowDialog && accountsInfo.isNotEmpty()) {
            showMissingTokenDialog = true
            platformsViewModel.accountsForMissingDialog = accountsInfo
        }

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                if(Vaults.isGetMeOut(applicationContext)) {
                    loggedInAlready = true
                } else {
                    Vaults.fetchLongLivedToken(applicationContext).let { llt ->
                        if(llt.isNotEmpty()) {
                            val vault = Vaults(applicationContext)
                            try {
                                vault.refreshStoredTokens(applicationContext, ) {
                                    if(it.isNotEmpty()) refreshTokensCallback(it)
                                }
                            } catch(e: StatusRuntimeException) {
                                if(e.status.code == Status.UNAUTHENTICATED.code) {
                                    loggedInAlready = true
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

    private fun processIntent(navController: NavController, newIntent: Intent? = null) {
        val intent = newIntent ?: intent
        when(intent.action) {
            intent.NEW_NOTIFICATION_ACTION -> {
                val address = intent.getStringExtra("address")
                address?.let {
                    intent.removeExtra("address")
                    navController.navigate(ConversationsScreenNav(address))
                }
            }
            Intent.ACTION_SEND -> {

            }
            Intent.ACTION_SENDTO -> {
                intent.data?.let { uri ->
                    val address = makeE16PhoneNumber(uri.toString())

                    val text = intent.getStringExtra("sms_body")
                        ?: intent.getStringExtra(Intent.EXTRA_TEXT)

                    intent.removeExtra("sms_body")
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    intent.data = null

                    navController.navigate(ConversationsScreenNav(
                        address = address,
                        text = text,
                    ))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(::navController.isInitialized)
            processIntent(navController, intent)
    }
}
