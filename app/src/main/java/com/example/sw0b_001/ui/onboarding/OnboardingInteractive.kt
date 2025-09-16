package com.example.sw0b_001.ui.onboarding

import android.accessibilityservice.GestureDescription
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsGetOnboardedCompletely
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.components.OnboardingNextButton
import com.example.sw0b_001.ui.modals.OnlineActivePlatformsModal
import com.example.sw0b_001.ui.modals.SignupLoginModal
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OnboardingSkipScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.OnboardingViewModel

data class InteractiveOnboarding(
    val title: String,
    val description: String,
    val image: Int,
    val actionButtonText: String? = null,
    val subDescription: String? = null,
    val onClickCallToAction: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingInteractive(
    navController: NavController,
    onboardingViewModel: OnboardingViewModel,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as AppCompatActivity
    val showingOnboarding by onboardingViewModel.onboardingState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {
                        context.settingsSetOnboardedCompletely(true)
                        navController.navigate(HomepageScreen) {
                            popUpTo(HomepageScreen) {
                                inclusive = true
                            }
                        }
                    },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.skip),
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowRight,
                                contentDescription = stringResource(R.string.skip),
                            )
                        }
                    }
                },
                navigationIcon = { },
            )
        },
    ) { innerPadding ->
        Column(Modifier
            .fillMaxSize()
            .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            if(showingOnboarding == null)
                onboardingViewModel.first(context, activity, navController)

            showingOnboarding?.let {
                OnboardingScreen(it)
            }

            Spacer(modifier = Modifier.weight(1f))

            OnboardingNextButton("") {
                if(onboardingViewModel.next()) {
                    context.settingsSetOnboardedCompletely(true)
                    navController.navigate(HomepageScreen) {
                        popUpTo(HomepageScreen) {
                            inclusive = true
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            if(onboardingViewModel.showLoginSignupModal) {
                val completedOnboarding = InteractiveOnboarding(
                    title = stringResource(R.string.ready_to_save_platforms),
                    description = stringResource(R.string.now_we_can_save_platforms_in_your_account),
                    subDescription = stringResource(R.string.it_s_easier_than_you_can_imagine),
                    actionButtonText = stringResource(R.string.save_platforms_to_your_account),
                    image = R.drawable.relay_sms_save_vault,
                ){
                    // TODO: check if has saved platforms already then skip this step for messaging step
                    onboardingViewModel.showAddPlatformsModal = true
                }
                val callback: ((Boolean) -> Unit) = { success ->
                    if(success) {
                        onboardingViewModel.setOnboarding(completedOnboarding)
                    }
                }

                SignupLoginModal(
                    onboardingViewModel.showLoginSignupModal,
                    createAccountCallback = {
                        onboardingViewModel.callback = callback
                        onboardingViewModel.showLoginSignupModal = false
                        navController.navigate(CreateAccountScreen(isOnboarding = true))
                    },
                    loginAccountCallback = {
                        onboardingViewModel.callback = callback
                        navController.navigate(LoginScreen(isOnboarding = true))
                        onboardingViewModel.showLoginSignupModal = false
                    }
                ) { onboardingViewModel.showLoginSignupModal = false }
            }

            if(onboardingViewModel.showAddPlatformsModal) {
                OnlineActivePlatformsModal(
                    onboardingViewModel.showAddPlatformsModal,
                    navController = navController,
                    isCompose = false,
                    isOnboarding = true,
                    onCompleteCallback = {
                        onboardingViewModel.setOnboarding(
                            InteractiveOnboarding(
                                title = context.getString(R.string.way_to_go),
                                description = context.getString(R.string.you_can_save_more_accounts_per_platform_at_anytime_from_inside_the_app),
                                image = R.drawable.undraw_success_288d,
                            ){}
                        )
                    }
                ) { onboardingViewModel.showAddPlatformsModal = false }
            }

            if(onboardingViewModel.showSendPlatformsModal) {
                onboardingViewModel.callback = {
                    onboardingViewModel.setOnboarding(
                        InteractiveOnboarding(
                            title = context.getString(R.string.you_are_now_ready),
                            description = context.getString(R.string.you_have_successfully_carried_out_the_essentials_of_messaging_with_relaysms),
                            image = R.drawable.undraw_success_288d,
                        ){}
                    )
                }

                OnlineActivePlatformsModal(
                    onboardingViewModel.showSendPlatformsModal,
                    navController = navController,
                    isCompose = true,
                    isOnboarding = true,
                    onCompleteCallback = {}
                ) { onboardingViewModel.showSendPlatformsModal = false }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onboardingScreen: InteractiveOnboarding) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(onboardingScreen.image),
            contentDescription = null,
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = onboardingScreen.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = onboardingScreen.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )

        onboardingScreen.subDescription?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start=24.dp, end=24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        onboardingScreen.actionButtonText?.let {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onboardingScreen.onClickCallToAction){
                Text(it)
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    group = "Default",
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
    showBackground = true,
)
@Composable
fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            InteractiveOnboarding(
                title = "SMS an email right now!",
                description = "You don't need an account, we'd create one for you!\nEmail yourself!",
                subDescription = "This also unlocks features like sending images with SMS (yes not MMS)",
                actionButtonText = "Compose email",
                image = R.drawable.undraw_success_288d,
                onClickCallToAction = {}
            )
        )
    }
}

@Preview(showBackground = true,)
@Composable
fun OnboardingInteractivePreview() {
    AppTheme {
        OnboardingInteractive(
            rememberNavController(),
            remember{ OnboardingViewModel() }
        )
    }
}
