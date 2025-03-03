package com.example.sw0b_001.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomepageScreen
@Serializable
object LoginScreen
@Serializable
object CreateAccountScreen
@Serializable
object OTPCodeScreen
@Serializable
object SettingsScreen

//object GatewayClients : Screen("gateway")
//object About : Screen("about")
//object Settings : Screen("settings")
//object AvailablePlatforms : Screen("available_platforms")
//object Security : Screen("security")
//object GetStarted : Screen("get_started")
//object Homepage : Screen("homepage")
//object OTPCode : Screen("otp_code")

sealed class Screen(val route: String) {
    //Screen Routes
    data object GatewayClients : Screen("gateway")
    data object About : Screen("about")
    data object AvailablePlatforms : Screen("available_platforms")
    data object Security : Screen("security")
    data object GetStarted : Screen("get_started")

    // Message Compose Routes
    data object EmailCompose : Screen("email_compose?isDefault={isDefault}") {
        fun withIsDefault(isDefault: Boolean): String {
            return "email_compose?isDefault=$isDefault"
        }
    }
    data object MessageCompose : Screen("message_compose")
    data object TextCompose : Screen("text_compose")

    // Message Details Routes
    data class EmailDetails(val recentMessage: String = "{recentMessage}") : Screen("email_details/$recentMessage") {
        companion object {
        }
    }
    data class TelegramDetails(val recentMessage: String = "{recentMessage}") : Screen("telegram_details/$recentMessage") {
        companion object {
        }
    }
    data class XDetails(val recentMessage: String = "{recentMessage}") : Screen("x_details/$recentMessage") {
        companion object {
        }
    }
}