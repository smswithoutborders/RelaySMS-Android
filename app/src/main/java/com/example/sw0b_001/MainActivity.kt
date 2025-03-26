package com.example.sw0b_001

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.sw0b_001.Models.NavigationFlowHandler
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
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.ui.navigation.AboutScreen
import com.example.sw0b_001.ui.navigation.BridgeEmailComposeScreen
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.GetMeOutScreen
import com.example.sw0b_001.ui.navigation.MessageComposeScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.navigation.TextComposeScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
import com.example.sw0b_001.ui.onboarding.OnboardingStep
import com.example.sw0b_001.ui.views.PasteEncryptedTextView
import com.example.sw0b_001.ui.views.details.EmailDetailsView
import com.example.sw0b_001.ui.views.details.MessageDetailsView
import com.example.sw0b_001.ui.views.details.TextDetailsView
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


enum class OnboardingState {
    Welcome,
    VaultStore,
    SaveVault,
    SendMessage,
    Complete
}

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    val navigationFlowHandler = NavigationFlowHandler()

    val platformsViewModel: PlatformsViewModel by viewModels()
    val messagesViewModel: MessagesViewModel by viewModels()
    val gatewayClientViewModel: GatewayClientViewModel by viewModels()

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
                Surface(
                    Modifier
                        .fillMaxSize()
                ) {
                    MainScreen(navController = navController)
                }
            }
        }
    }

    @Composable
    fun MainScreen(navController: NavHostController) {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var hasSeenOnboarding by remember {
            mutableStateOf(sharedPreferences.getBoolean("hasSeenOnboarding", false))
        }
        var currentOnboardingState by remember { mutableStateOf(OnboardingState.Welcome) }
        var navigateToHomepage by remember { mutableStateOf(false) }

        LaunchedEffect(navigateToHomepage) {
            if (navigateToHomepage) {
                navController.navigate(HomepageScreen) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        }

        if (!hasSeenOnboarding) {
            val onboardingSteps = listOf(
                OnboardingStep(
                    title = context.getString(R.string.welcome_to_relaysms_),
                    description = context.getString(R.string.use_sms_to_make_a_post_send_emails_and_messages_with_no_internet_connection),
                    image = R.drawable.relay_sms_welcome,
                    showLanguageButton = true,
                    showPrivacyPolicyLink = true,
                    isCompleteScreen = false,
                    nextButtonText = context.getString(R.string.learn_how_it_works_),
                    isWelcomeScreen = true
                ),
                OnboardingStep(
                    title = context.getString(R.string.relaysms_vaults_securely_stores_your_online_accounts_so_that_you_can_access_them_without_an_internet_connection),
                    description = "",
                    image = R.drawable.vault_illus,
                    showBackButton = true,
                    showSkipButton = true,
                    isCompleteScreen = false,
                ),
                OnboardingStep(
                    title = context.getString(R.string.you_can_add_online_accounts_to_your_vault),
                    description = "",
                    image = R.drawable.relay_sms_save_vault,
                    showBackButton = true,
                    showSkipButton = true,
                    isCompleteScreen = false,
                ),
                OnboardingStep(
                    title = context.getString(R.string.you_can_also_send_out_emails_without_an_account_or_vault),
                    description = context.getString(R.string.this_will_create_a_default_email_your_phonenumber_relaysms_me_using_your_phone_number),
                    image = R.drawable.try_sending_message_illus,
                    showBackButton = true,
                    showSkipButton = true,
                    isCompleteScreen = false,
                ),
                OnboardingStep(
                    title = context.getString(R.string.you_are_ready_to_begin_sending_messages_from_relaysms),
                    description = "",
                    image = R.drawable.ready_to_begin_illus,
                    showBackButton = true,
                    showSkipButton = false,
                    isCompleteScreen = true,
                    nextButtonText = context.getString(R.string.great),
                ),
            )
            com.example.sw0b_001.ui.onboarding.OnboardingView(
                step = onboardingSteps[currentOnboardingState.ordinal],
                onBack = {
                    currentOnboardingState = when (currentOnboardingState) {
                        OnboardingState.VaultStore -> OnboardingState.Welcome
                        OnboardingState.SaveVault -> OnboardingState.VaultStore
                        OnboardingState.SendMessage -> OnboardingState.SaveVault
                        OnboardingState.Complete -> OnboardingState.SendMessage
                        else -> OnboardingState.Welcome
                    }
                },
                onSkip = {
                    currentOnboardingState = OnboardingState.Complete
                },
                onNext = {
                    if (currentOnboardingState == OnboardingState.Complete) {
                        hasSeenOnboarding = true
                        with(sharedPreferences.edit()) {
                            putBoolean("hasSeenOnboarding", true)
                            apply()
                        }
                        navigateToHomepage = true
                    } else {
                        currentOnboardingState = when (currentOnboardingState) {
                            OnboardingState.Welcome -> OnboardingState.VaultStore
                            OnboardingState.VaultStore -> OnboardingState.SaveVault
                            OnboardingState.SaveVault -> OnboardingState.SendMessage
                            OnboardingState.SendMessage -> OnboardingState.Complete
                            else -> OnboardingState.Complete
                        }
                    }
                },
                onPrivacyPolicyClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://smswithoutborders.com/privacy-policy"))
                    context.startActivity(intent)
                }
            )
        } else {
            MainNavigation(navController = navController)
        }
    }

    @Composable
    fun MainNavigation(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = HomepageScreen,
        ) {
            composable<GetMeOutScreen> {
                GetMeOutOfHere(navController)
            }
            composable<HomepageScreen> {
                HomepageView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel,
                    gatewayClientViewModel = gatewayClientViewModel,
                    navigationFlowHandler = navigationFlowHandler
                )
            }
            composable<LoginScreen> {
                LoginView(
                    navController = navController,
                    navigationFlowHandler = navigationFlowHandler
                )
            }
            composable<CreateAccountScreen> {
                CreateAccountView(navController = navController)
            }
            composable<OTPCodeScreen> {
                OtpCodeVerificationView(
                    navController = navController,
                    navigationFlowHandler = navigationFlowHandler
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
                                vault.refreshStoredTokens(applicationContext)
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

@Composable
fun GetMeOutOfHere(
    navController: NavController
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    BackHandler {
        activity?.finish()
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.relaysms_icon_default_shape),
            contentDescription = "Drink me out of here",
            modifier = Modifier
                .size(100.dp)
                .padding(top = 42.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text("You need to log in again into this device!",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Thin,
            modifier = Modifier.padding(start=16.dp, end=16.dp)
        )
        Text(
            "You have logged in on another device",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Thin,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(bottom = 32.dp, top = 8.dp),
        )
        Image(
            painter = painterResource(R.drawable.get_me_out),
            contentDescription = "Drink me out of here",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            onClick={
                Vaults.logout(context) {
                    Vaults.setGetMeOut(context, false)
                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(HomepageScreen) {
                            popUpTo(HomepageScreen) {
                                inclusive = true
                            }
                        }
                    }
                }
        }) {
            Text("Get me out here!", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetMeOutOfHerePreview() {
    AppTheme(darkTheme = false) {
        GetMeOutOfHere(navController = rememberNavController())
    }
}
