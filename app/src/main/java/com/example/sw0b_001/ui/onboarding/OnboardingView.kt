package com.example.sw0b_001.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.OnboardingState
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNextButton
import com.example.sw0b_001.ui.navigation.HomepageScreen
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.Settings.SettingsFragment.Companion.changeLanguageLocale
import com.example.sw0b_001.Settings.SettingsFragment.Companion.getCurrentLocale
import com.example.sw0b_001.ui.components.LanguageSelectionPopup
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.theme.provider

data class OnboardingStep(
    val title: String,
    val description: String,
    val image: Int? = null,
    val showBackButton: Boolean = false,
    val showSkipButton: Boolean = false,
    val showLanguageButton: Boolean = false,
    val showPrivacyPolicyLink: Boolean = false,
    val isCompleteScreen: Boolean = false,
    val nextButtonText: String = "",
    val isWelcomeScreen: Boolean = false
)

const val PREF_USER_ONBOARDED = "PREF_USER_ONBOARDED"
const val USER_ONBOARDED = "USER_ONBOARDED"

@Composable
fun MainOnboarding(navController: NavController) {
    val context = LocalContext.current
    var currentOnboardingState by remember { mutableStateOf(OnboardingState.Welcome) }

    val onboardingSteps = listOf(
        OnboardingStep(
            title = stringResource(R.string.welcome_to_relaysms_),
            description = stringResource(R.string.use_sms_to_make_a_post_send_emails_and_messages_with_no_internet_connection),
            image = R.drawable.relay_sms_welcome,
            showLanguageButton = true,
            showPrivacyPolicyLink = true,
            isCompleteScreen = false,
            nextButtonText = stringResource(R.string.learn_how_it_works_),
            isWelcomeScreen = true
        ),
        OnboardingStep(
            title = stringResource(R.string.relaysms_vaults_securely_stores_your_online_accounts_so_that_you_can_access_them_without_an_internet_connection),
            description = "",
            image = R.drawable.vault_illus,
            showBackButton = true,
            showSkipButton = true,
            isCompleteScreen = false,
        ),
        OnboardingStep(
            title = stringResource(R.string.you_can_add_online_accounts_to_your_vault),
            description = "",
            image = R.drawable.relay_sms_save_vault,
            showBackButton = true,
            showSkipButton = true,
            isCompleteScreen = false,
        ),
        OnboardingStep(
            title = stringResource(R.string.you_can_also_send_out_emails_without_an_account_or_vault),
            description = stringResource(R.string.this_will_create_a_default_email_your_phonenumber_relaysms_me_using_your_phone_number),
            image = R.drawable.try_sending_message_illus,
            showBackButton = true,
            showSkipButton = true,
            isCompleteScreen = false,
        ),
        OnboardingStep(
            title = stringResource(R.string.you_are_ready_to_begin_sending_messages_from_relaysms),
            description = "",
            image = R.drawable.ready_to_begin_illus,
            showBackButton = true,
            showSkipButton = false,
            isCompleteScreen = true,
            nextButtonText = stringResource(R.string.great),
        ),
    )


    OnboardingView(
        step = onboardingSteps[currentOnboardingState.ordinal],
        onBack = {
            currentOnboardingState = when (currentOnboardingState) {
                OnboardingState.VaultStore -> OnboardingState.Welcome
                OnboardingState.SaveVault -> OnboardingState.VaultStore
                OnboardingState.SendMessage -> OnboardingState.SaveVault
                OnboardingState.Complete -> OnboardingState.SendMessage
                else -> OnboardingState.Welcome
            }
        },
        onSkip = {
            currentOnboardingState = OnboardingState.Complete
        },
        onNext = {
            if (currentOnboardingState == OnboardingState.Complete) {
                val sharedPreferences = context
                    .getSharedPreferences(PREF_USER_ONBOARDED, Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean(USER_ONBOARDED, true)
                    apply()
                }
                navController.navigate(HomepageScreen) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            } else {
                currentOnboardingState = when (currentOnboardingState) {
                    OnboardingState.Welcome -> OnboardingState.VaultStore
                    OnboardingState.VaultStore -> OnboardingState.SaveVault
                    OnboardingState.SaveVault -> OnboardingState.SendMessage
                    OnboardingState.SendMessage -> OnboardingState.Complete
                    else -> OnboardingState.Complete
                }
            }
        },
        onPrivacyPolicyClicked = {
            val intent = Intent(Intent.ACTION_VIEW,
                context.getString(R.string.https_smswithoutborders_com_privacy_policy).toUri())
            context.startActivity(intent)
        }
    )
}

@Composable
fun OnboardingView(
    step: OnboardingStep,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onPrivacyPolicyClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showLanguagePopup by remember { mutableStateOf(false) }
    var currentLanguageCode by remember { mutableStateOf(getCurrentLocale(context)) }

    LaunchedEffect(currentLanguageCode) {
        changeLanguageLocale(context, currentLanguageCode)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (LocalInspectionMode.current || (step.showBackButton || step.showSkipButton)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 72.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step.showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    if (step.showSkipButton) {
                        Row(
                            modifier = if(LocalInspectionMode.current) Modifier else
                                Modifier.clickable(onClick = onSkip),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.skip),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.skip),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }

            // Illustration
            step.image?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(250.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (step.showLanguageButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showLanguagePopup = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = stringResource(R.string.language),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Helpers.getLanguageNameFromCode(context, currentLanguageCode),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(64.dp))
            }

            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            if (step.isCompleteScreen) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = step.nextButtonText, color = MaterialTheme.colorScheme.onPrimary)
                }
            } else if (step.isWelcomeScreen){
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = step.nextButtonText, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            else {
                OnboardingNextButton(onNext = onNext, nextButtonText = step.nextButtonText)
            }

            if (step.showPrivacyPolicyLink && onPrivacyPolicyClicked != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.read_our_privacy_policy),
                    modifier = if(LocalInspectionMode.current) Modifier
                    else Modifier.clickable(onClick = onPrivacyPolicyClicked),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showLanguagePopup) {
        LanguageSelectionPopup(
            currentLanguageCode = currentLanguageCode,
            onLanguageSelected = { selectedLanguage ->
                currentLanguageCode = selectedLanguage.code
            },
            onDismiss = { showLanguagePopup = false }
        )
    }
}

@Composable
private fun ShowLanguageButton() {

}

@Preview
@Composable
fun MainOnboardingPreview() {
    AppTheme {
        MainOnboarding(rememberNavController())
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    group = "Default"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
)
@Composable
fun OnboardingViewPreview() {
    AppTheme {
        val step = OnboardingStep(
            title = stringResource(R.string.relaysms_vaults_securely_stores_your_online_accounts_so_that_you_can_access_them_without_an_internet_connection),
            description = "",
            image = R.drawable.vault_illus,
            showBackButton = true,
            showSkipButton = true,
            isCompleteScreen = false,
        )
        OnboardingView(step, {}, {}, {})
    }
}