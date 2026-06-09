package com.example.sw0b_001.ui.navigation

import kotlinx.serialization.Serializable
import uniffi.relaysms_spec_payload.V1ContentCategories

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
object AboutScreen

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
    val cat: V1ContentCategories,
    val messageId: Long?,
)

@Serializable
data class EmailViewScreen(
    val cat: V1ContentCategories,
    val messageId: Long
)
@Serializable
data class TextViewScreen(
    val messageId: Long
)

@Serializable
data class MessageViewScreen(
    val messageId: Long
)
