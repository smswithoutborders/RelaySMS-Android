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
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.NavigationFlowHandler
import com.example.sw0b_001.ui.views.CreateAccountView
import com.example.sw0b_001.ui.views.LoginView
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.navigation.SettingsScreen
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.GetStartedView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import com.example.sw0b_001.ui.views.SecurityView
import com.example.sw0b_001.ui.views.SettingsView
import com.example.sw0b_001.ui.views.compose.EmailComposeView
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeView


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    val navigationFlowHandler = NavigationFlowHandler()

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
                HomepageView(navController = navController)
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
            composable<SettingsScreen> {
                val intent = Intent(LocalContext.current, SettingsActivity::class.java)
                startActivity(intent)
            }
            composable(Screen.About.route) {
                AboutView(navController = navController)
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
//                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
//                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
//                    EmailDetailsView(message = recentMessage, navController = navController)
                }
            }
            composable(
                route = Screen.TelegramDetails().route,
                arguments = listOf(navArgument("recentMessage") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedJson = backStackEntry.arguments?.getString("recentMessage")
                encodedJson?.let {
//                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
//                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
//                    MessageDetailsView(message = recentMessage, navController = navController)
                }
            }
            composable(
                route = Screen.XDetails().route,
                arguments = listOf(navArgument("recentMessage") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedJson = backStackEntry.arguments?.getString("recentMessage")
                encodedJson?.let {
//                    val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
//                    val recentMessage = Json.decodeFromString<RecentMessage>(decodedJson)
//                    TextDetailsView(message = recentMessage, navController = navController)
                }
            }
        }
    }
}

