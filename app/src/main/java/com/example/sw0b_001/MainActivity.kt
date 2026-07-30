package com.example.sw0b_001

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.lib_image_android.ui.BindActivity
import com.afkanerd.lib_image_android.ui.ImageRender
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NEW_NOTIFICATION_ACTION
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.makeE16PhoneNumber
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.NavHostControllerInstance
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.requiredReadPhoneStatePermissions
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import com.example.sw0b_001.data.CrashHandler
import com.example.sw0b_001.data.CrashHandler.Companion.saveFileToUri
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsGetLockDownApp
import com.example.sw0b_001.extensions.context.settingsGetOnboardedCompletely
import com.example.sw0b_001.extensions.context.settingsSetLockDownApp
import com.example.sw0b_001.ui.navigation.AboutScreen
import com.example.sw0b_001.ui.navigation.BackupScreen
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.DetailsInterfaceScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.MetricsScreen
import com.example.sw0b_001.ui.navigation.OnboardingInteractiveScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.navigation.RestoreScreen
import com.example.sw0b_001.ui.navigation.SettingsScreen
import com.example.sw0b_001.ui.navigation.WelcomeScreen
import com.example.sw0b_001.ui.onboarding.OnboardingInteractive
import com.example.sw0b_001.ui.onboarding.OnboardingView
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.BackupRestoreViewModel
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.OfflineFirstPublisherViewModel
import com.example.sw0b_001.ui.viewModels.OnboardingViewModel
import com.example.sw0b_001.ui.viewModels.OnlineFirstPublisherViewModel
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.backupRestore.BackupView
import com.example.sw0b_001.ui.views.backupRestore.RecoveryView
import com.example.sw0b_001.ui.views.compose.ComposerInterface
import com.example.sw0b_001.ui.views.details.DetailsInterfaceView
import com.example.sw0b_001.ui.views.incoming.PasteEncryptedTextView
import com.example.sw0b_001.ui.views.settings.SettingsView
import com.example.sw0b_001.ui.views.tabs.HomepageView
import com.example.sw0b_001.ui.views.threads.MetricsView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.Transports
import uniffi.relaysms_spec_payload.v1CalculateSegments
import java.io.File

@AndroidEntryPoint
class MainActivity : BindActivity() {
    companion object {
        init {
            System.loadLibrary("relaysms_spec_payload")
        }
    }

    private lateinit var navController: NavHostController
    val searchViewModel: SearchViewModel by viewModels()
    val supportedPlatformsViewModel: SupportedPlatformsViewModel by viewModels()

    val threadsViewModel: ThreadsViewModel by viewModels()
    val conversationViewModel: ConversationsViewModel by viewModels()
    val onboardingViewModel: OnboardingViewModel by viewModels()

    val tokensViewModel: TokensViewModel by viewModels()
    val payloadsViewModel: PayloadsViewModel by viewModels()
    val gatewayClientViewModel: GatewayClientViewModel by viewModels()
    val imageViewModel: ImageViewModel by viewModels()
    val onlineFirstPublisherViewModel: OnlineFirstPublisherViewModel by viewModels()
    val offlineFirstPublisherViewModel: OfflineFirstPublisherViewModel by viewModels()

    val backupRestoreViewModel: BackupRestoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Fix for three-button nav not properly going edge-to-edge.
            // TODO: https://issuetracker.google.com/issues/298296168
            window.isNavigationBarContrastEnforced = false
        }

        imageTransmissionCallback()
        CrashHandler.initialize(applicationContext)

        lifecycleScope.launch {
            onboardingViewModel.showBiometrics.collect { callback ->
                promptBiometrics(this@MainActivity) {
                    if(it) {
                        settingsSetLockDownApp(true)
                        callback()
                    }
                    else {
                        Toast.makeText(applicationContext,
                            getString(R.string.failed_to_set_biometric_authentication),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            onboardingViewModel.navigate.collect { composable ->
                navController.navigate( composable )
            }
        }

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
            TODO()
//            promptBiometrics(this) {
//                if(!it) {
//                    finish()
//                    exitProcess(0)
//                } else {
//                    callback()
//                }
//            }
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

        var showThreadsTopBar by remember { mutableStateOf(true) }
        var customThreadView: (@Composable () -> Unit)? by remember { mutableStateOf(null)}

        var navDrawItemSelected by remember{ mutableStateOf(false) }
        var drawerCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

        val inboxType by threadsViewModel.inboxType.collectAsStateWithLifecycle()

        LaunchedEffect(inboxType) {
            navDrawItemSelected = inboxType == ThreadsViewModel.InboxType.CUSTOM
        }

        LaunchedEffect(navDrawItemSelected) {
            customThreadView = when {
                navDrawItemSelected -> {
                    {
                        showThreadsTopBar = false
                        imageViewModel.reset()
                        payloadsViewModel.reset()
                        HomepageView(
                            navController = navController,
                            tokensViewModel = tokensViewModel,
                            payloadsViewModel = payloadsViewModel,
                            gatewayClientViewModel = gatewayClientViewModel,
                            supportedPlatformsViewModel = supportedPlatformsViewModel,
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
            newLayoutInfo = newLayoutInfo,
            navController = navController,
            threadsViewModel = threadsViewModel,
            conversationsViewModel = conversationViewModel,
            searchViewModel = searchViewModel,
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
                OnboardingView(navController = navController)
            }
            composable<OnboardingInteractiveScreen> {
                OnboardingInteractive(
                    navController,
                    onboardingViewModel,
                    tokensViewModel = tokensViewModel,
                    supportedPlatformsViewModel,
                )
            }
            composable<HomepageScreen> {
                imageViewModel.reset()
                payloadsViewModel.reset()
                HomepageView(
                    navController = navController,
                    tokensViewModel = tokensViewModel,
                    payloadsViewModel = payloadsViewModel,
                    gatewayClientViewModel = gatewayClientViewModel,
                    supportedPlatformsViewModel = supportedPlatformsViewModel,
                )
            }
            composable<AboutScreen> {
                AboutView(navController = navController)
            }
            composable<ComposeScreen> { backEntry ->
                val composeScreenNav: ComposeScreen = backEntry.toRoute()
                ComposerInterface(
                    navController = navController,
                    imageViewModel = imageViewModel,
                    gatewayClientViewModel = gatewayClientViewModel,
                    tokensViewModel = tokensViewModel,
                    messageId = composeScreenNav.messageId,
                    payloadsViewModel = payloadsViewModel,
                    onlineFirstPublisherViewModel = onlineFirstPublisherViewModel,
                    offlineFirstPublisherViewModel = offlineFirstPublisherViewModel,
                    catId = composeScreenNav.cat,
                    supportedPlatformName = composeScreenNav.supportedPlatform,
                    isOfflineCompose = composeScreenNav.isOfflineCompose,
                    supportedPlatformsViewModel = supportedPlatformsViewModel,
                )
            }
            composable<DetailsInterfaceScreen> { backEntry ->
                val detailsInterfaceScreen: DetailsInterfaceScreen = backEntry.toRoute()
                DetailsInterfaceView(
                    navController = navController,
                    tokensViewModel = tokensViewModel,
                    payloadsViewModel = payloadsViewModel,
                    imageViewModel = imageViewModel,
                    cat = detailsInterfaceScreen.cat,
                    messageId = detailsInterfaceScreen.messageId,
                    supportedPlatformsViewModel = supportedPlatformsViewModel,
                )
            }
            composable<PasteEncryptedTextScreen> {
                PasteEncryptedTextView(
                    tokensViewModel = tokensViewModel,
                    payloadsViewModel = payloadsViewModel,
                    navController = navController,
                )
            }
            composable<ImageRenderNav>{ backStackEntry ->
                val imageRenderNav: ImageRenderNav = backStackEntry.toRoute()
                ImageRender(
                    navController = navController,
                    imageViewModel = imageViewModel,
                    uri = imageRenderNav.uri?.toUri(),
                    attachmentCounterCallback = { payloadSize ->
                        v1CalculateSegments(
                            payloadSize.toUInt(),
                            Transports.SMS,
                            true
                        ).toInt()
                    },
                    onApplyCallback = {
                        navController.popBackStack()
                    },
                    backActionCallback = {
                        navController.popBackStack()
                    }
                )
            }
            composable<SettingsScreen> {
                SettingsView(
                    navController = navController,
                    tokensViewModel = tokensViewModel,
                    activity = this@MainActivity
                )
            }
            composable<BackupScreen> {
                BackupView(navController, backupRestoreViewModel)
            }
            composable<RestoreScreen> {
                RecoveryView(navController, backupRestoreViewModel)
            }
            composable<MetricsScreen> { backStackEntry ->
                val metricsNav: MetricsScreen = backStackEntry.toRoute()
                MetricsView(
                    navController,
                    tokensId = metricsNav.tokenId,
                    tokensViewModel = tokensViewModel
                )
            }
        }

        processIntent(navController)
    }

    private fun imageTransmissionCallback() {
        setRemoteExecutionCallback { payload ->
            onlineFirstPublisherViewModel.attachmentExecutor(payload)
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

    private var pendingSaveFile: File? = null
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            pendingSaveFile?.let { file ->
                saveFileToUri(this, file, destUri)
            }
        }
        pendingSaveFile = null
    }

    fun launchSaveCrashLog(mergedFile: File) {
        pendingSaveFile = mergedFile
        createDocumentLauncher.launch(mergedFile.name)
    }

}
