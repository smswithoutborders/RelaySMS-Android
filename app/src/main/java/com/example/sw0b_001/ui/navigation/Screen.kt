package com.example.sw0b_001.ui.navigation

import android.content.Context
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.ui.views.OTPCodeVerificationType
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
    val loginSignupPhoneNumber: String,
    val loginSignupPassword: String,
    val countryCode: String,
    val otpRequestType: OTPCodeVerificationType = OTPCodeVerificationType.AUTHENTICATE,
    val nextAttemptTimestamp: Int? = null,
    val isOnboarding: Boolean = false
)

@Serializable
object AboutScreen

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
object SettingsScreen

@Serializable
data class ForgotPasswordScreen(
    val isOnboarding: Boolean = false
)

@Serializable
data class ComposeScreen(
    val type: Platforms.ServiceTypes,
    val platformName: String?,
    val isOnboarding: Boolean = false,
)

