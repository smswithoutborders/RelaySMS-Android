package com.example.sw0b_001.ui.navigation

import kotlinx.serialization.Serializable
import uniffi.relaysms_spec_payload.V1ContentCategories

@Serializable
object WelcomeScreen

@Serializable
object OnboardingInteractiveScreen

@Serializable
object HomepageScreen

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
object BackupScreen

@Serializable
object RestoreScreen {

}

@Serializable
data class ComposeScreen(
    val cat: V1ContentCategories,
    val messageId: Long?,
    val supportedPlatform: String,
)

@Serializable
data class DetailsInterfaceScreen(
    val cat: V1ContentCategories,
    val messageId: Long
)

@Serializable
data class MetricsScreen(
    val tokenHash: ByteArray,
)
