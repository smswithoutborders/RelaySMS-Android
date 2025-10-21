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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.Vaults
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
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.sw0b_001.extensions.context.settingsGetStoreTokensOnDevice
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.HomepageScreenRelay
import com.example.sw0b_001.ui.navigation.OnboardingInteractiveScreen
import com.example.sw0b_001.ui.navigation.OnboardingSkipScreen
import com.example.sw0b_001.ui.navigation.SettingsScreen
import com.example.sw0b_001.ui.navigation.WelcomeScreen
import com.example.sw0b_001.ui.onboarding.OnboardingInteractive
import com.example.sw0b_001.ui.views.WelcomeMainView
import com.example.sw0b_001.ui.viewModels.OnboardingViewModel
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.SettingsView
import com.example.sw0b_001.ui.views.compose.ComposerInterface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController
    private lateinit var searchViewModel: SearchViewModel

    val threadsViewModel: ThreadsViewModel by viewModels()
    val onboardingViewModel: OnboardingViewModel by viewModels()

    val platformsViewModel: PlatformsViewModel by viewModels()
    val messagesViewModel: MessagesViewModel by viewModels()
    val gatewayClientViewModel: GatewayClientViewModel by viewModels()
    val imageViewModel: ImageViewModel by viewModels()

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

                                    LaunchedEffect(loggedInAlready) {
                                        if(loggedInAlready) {
                                            navController.navigate(GetMeOutScreen) {
                                                popUpTo(HomepageScreen) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    }

                                    Surface( Modifier.fillMaxSize()) {
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


        var navDrawItemSelected by remember{ mutableStateOf(false) }
        var drawerCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

        val lifeCycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifeCycleOwner) {
            val observer = Observer<ThreadsViewModel.InboxType> { newInboxType ->
                navDrawItemSelected = newInboxType == ThreadsViewModel.InboxType.CUSTOM
            }
            threadsViewModel.selectedInbox.observe(lifeCycleOwner, observer)

            onDispose {
                threadsViewModel.selectedInbox.removeObserver(observer)
            }
        }

        LaunchedEffect(navDrawItemSelected) {
            customThreadView = when {
                navDrawItemSelected -> {
                    {
                        showThreadsTopBar = false
                        HomepageView(
                            navController = navController,
                            platformsViewModel = platformsViewModel,
                            messagesViewModel = messagesViewModel,
                            gatewayClientViewModel = gatewayClientViewModel,
                            imageViewModel = imageViewModel,
                            drawerCallback = drawerCallback
                        )
                    }
                }
                else -> {
                    showThreadsTopBar = true
                    null
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
            customThreadsView = customThreadView,
            modalNavigationModalItems = { callback ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "RelaySMS",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            stringResource(R.string.relaysms_inbox),
                            fontSize = 14.sp
                        )
                    },
                    selected = navDrawItemSelected,
                    onClick = {
                        drawerCallback = callback.invoke(ThreadsViewModel.InboxType.CUSTOM)
                        threadsViewModel.setInboxType(ThreadsViewModel.InboxType.CUSTOM)
                    }
                )
            }
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
                imageViewModel.reset()
                HomepageView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
                    gatewayClientViewModel = gatewayClientViewModel,
                    imageViewModel = imageViewModel,
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
                    imageViewModel = imageViewModel,
                    messagesViewModel = messagesViewModel,
                    onSendCallback = if(composeScreenNav.isOnboarding)
                        onboardingViewModel.callback else null,
                    platformName = composeScreenNav.platformName,
                )
            }
            composable<EmailViewScreen> {
                EmailDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
                    imageViewModel = imageViewModel,
                )
            }
            composable<BridgeViewScreen> {
                EmailDetailsView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
                    imageViewModel = imageViewModel,
                    isBridge = true
                )
            }
            composable<TextViewScreen> {
                TextDetailsView(
                    navController = navController,
                    messagesViewModel = messagesViewModel,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<MessageViewScreen> {
                MessageDetailsView(
                    navController = navController,
                    messagesViewModel = messagesViewModel,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<PasteEncryptedTextScreen> {
                PasteEncryptedTextView(
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
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

            composable<SettingsScreen> {
                SettingsView(
                    navController = navController,
                    activity = this@MainActivity
                )
            }
        }

        processIntent(navController)
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
                                vault.refreshStoredTokens(
                                    applicationContext,
                                    settingsGetStoreTokensOnDevice)
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
