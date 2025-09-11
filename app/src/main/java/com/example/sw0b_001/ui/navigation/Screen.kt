package com.example.sw0b_001.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object OnboardingScreen

@Serializable
object GetMeOutScreen
@Serializable
object HomepageScreen
@Serializable
object LoginScreen
@Serializable
object CreateAccountScreen
@Serializable
object OTPCodeScreen
@Serializable
object AboutScreen

@Serializable
object EmailComposeScreen

@Serializable
object EmailComposeNav

@Serializable
data class CreateAccountNav(
    val callback: (Boolean) -> Unit = {},
)

@Serializable
data class LoginAccountNav(
    val callback: (Boolean) -> Unit = {},
)

@Serializable
data class ForgotPasswordNav(
    val callback: (Boolean) -> Unit = {},
)

@Serializable
data class OtpCodeNav(
    val callback: (Boolean) -> Unit = {},
)

@Serializable
object BridgeEmailComposeScreen
@Serializable
object TextComposeScreen
@Serializable
object MessageComposeScreen

@Serializable
object BridgeViewScreen
@Serializable
object EmailViewScreen
@Serializable
object TextViewScreen
@Serializable
object MessageViewScreen

@Serializable
object PasteEncryptedTextScreen

@Serializable
object ForgotPasswordScreen