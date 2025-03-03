package com.example.sw0b_001

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.example.sw0b_001.Homepage.HomepageLoggedIn
import com.example.sw0b_001.Homepage.HomepageNotLoggedIn
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.modals.NewMessageModal
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.views.AboutView
import com.example.sw0b_001.ui.views.AvailablePlatformsView
import com.example.sw0b_001.ui.views.GatewayClientView
import com.example.sw0b_001.ui.views.GetStartedView
import com.example.sw0b_001.ui.views.HomepageView
import com.example.sw0b_001.ui.views.OtpCodeVerificationView
import com.example.sw0b_001.ui.views.RecentMessage
import com.example.sw0b_001.ui.views.RecentsView
import com.example.sw0b_001.ui.views.SecurityView
import com.example.sw0b_001.ui.views.SettingsView
import com.example.sw0b_001.ui.views.compose.EmailComposeView
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeView
import com.example.sw0b_001.ui.views.details.EmailDetails
import com.example.sw0b_001.ui.views.details.EmailDetailsView
import com.example.sw0b_001.ui.views.details.MessageDetailsView
import com.example.sw0b_001.ui.views.details.TextDetailsView
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

//@Serializable
//object RecentScreen
//@Serializable
//object GatewayClientScreen
//@Serializable
//object AboutScreen
//@Serializable
//object SettingsScreen
//@Serializable
//object HomepageScreen
//@Serializable
//object HomepageNotLoggedScreen
//@Serializable
//object AddPlatformsScreen
//@Serializable
//object OtpCodeVerificationScreen
//@Serializable
//object SecurityScreen

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
//                    NavHost(
//                        modifier = Modifier,
//                        navController = navController,
//                        startDestination = RecentScreen,
//                    ) {
//                        composable<RecentScreen> {
//                            RecentScreenComposable()
//                        }
//                        composable<SettingsScreen> {
//                            SettingsScreenComposable()
//                        }
//                        composable<AboutScreen> {
//                            AboutScreenComposable()
//                        }
//                        composable<GatewayClientScreen> {
//                            GatewayClientsScreenComposable()
//                        }
//
//                    }
                }
            }
        }

    }

    @Composable
    fun MainNavigation(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Screen.Homepage.route,
        ) {
            composable(Screen.Homepage.route) {
                HomepageView(navController = navController)
            }
            composable(Screen.Recents.route) {
                RecentsView(navController = navController)
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
            composable(Screen.OTPCode.route) {
                OtpCodeVerificationView(navController = navController)
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


//    @Composable
//    fun RecentScreenComposable() {
//        RecentsView(navController = navController)
//    }
//
//    @Composable
//    fun SettingsScreenComposable() {
//        SettingsView(navController = navController)
//    }
//
//    @Composable
//    fun AboutScreenComposable() {
//        AboutView(navController = navController)
//    }
//
//    @Composable
//    fun GatewayClientsScreenComposable() {
//        GatewayClientView(navController = navController)
//    }
}

