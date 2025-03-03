package com.example.sw0b_001

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.ui.modals.CreateAccountView
import com.example.sw0b_001.ui.modals.LoginView
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.AvailablePlatformsView
import com.example.sw0b_001.ui.views.GatewayClientView
import com.example.sw0b_001.ui.views.GetStartedView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import com.example.sw0b_001.ui.views.RecentMessage
import com.example.sw0b_001.ui.views.SecurityView
import com.example.sw0b_001.ui.views.SettingsView
import com.example.sw0b_001.ui.views.compose.EmailComposeView
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeView
import com.example.sw0b_001.ui.views.details.EmailDetailsView
import com.example.sw0b_001.ui.views.details.MessageDetailsView
import com.example.sw0b_001.ui.views.details.TextDetailsView
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    val messagesViewModel: MessagesViewModel = MessagesViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
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
                HomepageView(navController = navController)
            }
            composable<LoginScreen> {
                LoginView(navController = navController)
            }
            composable<CreateAccountScreen> {
                CreateAccountView(navController = navController)
            }
            composable<OTPCodeScreen> {
                OtpCodeVerificationView(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsView(navController = navController)
            }
            composable(Screen.About.route) {
                AboutView(navController = navController)
            }
            composable(Screen.GatewayClients.route) {
                GatewayClientView(navController = navController)
            }
            composable(Screen.AvailablePlatforms.route) {
                AvailablePlatformsView(navController = navController)
            }
            composable(Screen.Security.route) {
                SecurityView(navController = navController)
            }
            composable(Screen.GetStarted.route) {
                GetStartedView(navController = navController)
            }

            // Compose Screens
            composable(
                route = Screen.EmailCompose.route,
                arguments = listOf(
                    navArgument("isDefault") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isDefault = backStackEntry.arguments?.getBoolean("isDefault") ?: false
                EmailComposeView(navController = navController, isDefault = isDefault)
            }
            composable(Screen.MessageCompose.route) {
                MessageComposeView(navController = navController)
            }
            composable(Screen.TextCompose.route) {
                TextComposeView(navController = navController)
            }

            // Message Details Screens
            composable(
                route = Screen.EmailDetails().route,
                arguments = listOf(navArgument("recentMessage") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedJson = backStackEntry.arguments?.getString("recentMessage")
                encodedJson?.let {
                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
                    EmailDetailsView(message = recentMessage, navController = navController)
                }
            }
            composable(
                route = Screen.TelegramDetails().route,
                arguments = listOf(navArgument("recentMessage") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedJson = backStackEntry.arguments?.getString("recentMessage")
                encodedJson?.let {
                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
                    MessageDetailsView(message = recentMessage, navController = navController)
                }
            }
            composable(
                route = Screen.XDetails().route,
                arguments = listOf(navArgument("recentMessage") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedJson = backStackEntry.arguments?.getString("recentMessage")
                encodedJson?.let {
                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
                    TextDetailsView(message = recentMessage, navController = navController)
                }
            }
        }
    }
}

