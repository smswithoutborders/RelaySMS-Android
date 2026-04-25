package com.example.sw0b_001.ui.navigation

import com.example.sw0b_001.data.repositories.TransportTypes
import com.example.sw0b_001.ui.views.accounts.OTPCodeVerificationType
import kotlinx.serialization.Serializable

@Serializable
object WelcomeScreen

@Serializable
object OnboardingSkipScreen

@Serializable
object OnboardingInteractiveScreen

@Serializable
object GetMeOutScreen

@Serializable
object HomepageScreen

@Serializable
object HomepageScreenRelay

@Serializable
data class LoginScreen(
    val isOnboarding: Boolean = false
)

@Serializable
data class CreateAccountScreen(
    val isOnboarding: Boolean = false
)

@Serializable
data class OTPCodeScreen(
    val email: String,
    val loginSignupPhoneNumber: String,
    val loginSignupPassword: String,
    val countryCode: String,
    val recaptcha: String,
    val otpRequestType: OTPCodeVerificationType = OTPCodeVerificationType.AUTHENTICATE,
    val nextAttemptTimestamp: Int? = null,
    val isOnboarding: Boolean = false
)

@Serializable
object AboutScreen

@Serializable
object MessageViewScreen

@Serializable
object PasteEncryptedTextScreen

@Serializable
object SettingsScreen

@Serializable
data class ForgotPasswordScreen(
    val isOnboarding: Boolean = false
)

@Serializable
data class ComposeScreen(
    val transportType: TransportTypes,
    val platformName: String?,
    val isOnboarding: Boolean = false,
    val messageId: Long?,
)

@Serializable
data class EmailViewScreen(
    val transportTypes: TransportTypes,
    val messageId: Long? = null
)
@Serializable
data class TextViewScreen(
    val platformName: String,
    val messageId: Long
)
