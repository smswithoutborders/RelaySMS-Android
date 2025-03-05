package com.example.sw0b_001

import android.content.Intent
import android.os.Bundle
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
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sw0b_001.Models.GatewayClients.GatewayClient
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
import com.example.sw0b_001.ui.views.GetStartedView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import com.example.sw0b_001.ui.views.SecurityView
import com.example.sw0b_001.ui.views.SettingsView
import com.example.sw0b_001.ui.views.compose.EmailComposeView
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeView
import androidx.activity.viewModels
import com.example.sw0b_001.ui.navigation.AboutScreen
import com.example.sw0b_001.ui.navigation.BridgeEmailScreen
import com.example.sw0b_001.ui.navigation.EmailScreen
import com.example.sw0b_001.ui.navigation.MessageScreen
import com.example.sw0b_001.ui.navigation.TextScreen


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    val navigationFlowHandler = NavigationFlowHandler()

    val platformsViewModel: PlatformsViewModel by viewModels()
    val messagesViewModel: MessagesViewModel by viewModels()

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
                    MainNavigation(navController = navController)
                }
            }
        }
    }

    @Composable
    fun MainNavigation(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = HomepageScreen,
        ) {
            composable<HomepageScreen> {
                HomepageView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    messagesViewModel = messagesViewModel
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

            composable<BridgeEmailScreen> {
                EmailComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                    isBridge = true
                )
            }
            composable<EmailScreen> {
                EmailComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<TextScreen> {
                TextComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
            composable<MessageScreen> {
                MessageComposeView(
                    navController = navController,
                    platformsViewModel = platformsViewModel,
                )
            }
        }
    }


    override fun onResume() {
        super.onResume()

        try {
            GatewayClient.refreshGatewayClients(applicationContext)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}

