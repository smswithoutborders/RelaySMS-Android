package com.example.sw0b_001.ui.navigation

sealed class Screen(val route: String) {
    //Screen Routes
    data object Recents : Screen("recents")
    data object GatewayClients : Screen("gateway")
    data object About : Screen("about")
    data object Settings : Screen("settings")
    data object AvailablePlatforms : Screen("available_platforms")
    data object Security : Screen("security")
    data object GetStarted : Screen("get_started")
    data object Homepage : Screen("homepage")
    data object OTPCode : Screen("otp_code")

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