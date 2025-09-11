package com.example.sw0b_001.ui.onboarding

import android.accessibilityservice.GestureDescription
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNextButton
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.theme.AppTheme

data class InteractiveOnboarding(
    val title: String,
    val description: String,
    val image: Int,
    val actionButtonText: String? = null,
    val subDescription: String? = null,
    val onClickCallToAction: () -> Unit
)

@Composable
fun OnboardingInteractive(
    navController: NavController
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current
    var screenIndex = 0

    Scaffold(

    ) { innerPadding ->
        Column(Modifier
            .fillMaxSize()
            .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var showingOnboarding by remember {
                mutableStateOf<InteractiveOnboarding?>(null) }

            val onboardingScreens = mutableListOf(
                InteractiveOnboarding(
                    title = stringResource(R.string.sms_an_email_right_now),
                    description = stringResource(R.string.you_don_t_need_an_account_we_d_create_one_for_you_email_yourself),
                    actionButtonText = stringResource(R.string.compose_email),
                    image = R.drawable.try_sending_message_illus,
                    onClickCallToAction = {
                        if(previewMode) {
                            showingOnboarding = InteractiveOnboarding(
                                title = "Way to go!!",
                                description = "You have interacted with how easy it is to send your first message!",
                                subDescription = "There is more!!",
                                image = R.drawable.undraw_success_288d,
                            ){}
                        }
                        navController.navigate(EmailComposeNav { sent ->
                            if(sent) {
                                showingOnboarding = InteractiveOnboarding(
                                    title = "Way to go!!",
                                    description = "You have interacted with how easy it is to send your first message!",
                                    subDescription = "There is more!!",
                                    image = R.drawable.undraw_success_288d,
                                ){}
                            }
                        })
                    }
                ),
                InteractiveOnboarding(
                    title = "Save your accounts!",
                    description = "You can also use SMS to send messages from your online accounts! Saving them guarantees you can use them without an internet connection.",
                    actionButtonText = "Give it a try!",
                    image = R.drawable.vault_illus,
                    onClickCallToAction = { TODO() }
                ),
                InteractiveOnboarding(
                    title = "Start messaging now!",
                    description = "You can now send messages from your saved accounts!\nYou can also save more accounts later...",
                    actionButtonText = "Give it a try!",
                    image = R.drawable.relay_sms_save_vault,
                    onClickCallToAction = { TODO() }
                ),
                InteractiveOnboarding(
                    title = "Secure your app!",
                    description = "From locking with device pin code to other secure ways of making sure you maintain your app's privacy!",
                    actionButtonText = "Let's lock this down!",
                    image = R.drawable.undraw_fingerprint_kdwq,
                    onClickCallToAction = { TODO() }
                ),
                InteractiveOnboarding(
                    title = "Make default SMS app",
                    description = "You can manage all your SMS messages from a single place.",
                    subDescription = "This also unlocks features like sending images with SMS (yes not MMS)",
                    actionButtonText = "Set as default SMS app",
                    image = R.drawable.try_sending_message_illus,
                    onClickCallToAction = { TODO() }
                ),
            )

            Spacer(modifier = Modifier.weight(1f))

            if(showingOnboarding == null) {
                showingOnboarding = onboardingScreens[screenIndex]
            }
            OnboardingScreen(showingOnboarding!!)

            Spacer(modifier = Modifier.weight(1f))

            OnboardingNextButton("") {
                if(screenIndex < onboardingScreens.size - 1) {
                    screenIndex += 1
                    showingOnboarding = onboardingScreens[screenIndex]
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))
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
        OnboardingInteractive(rememberNavController())
    }
}
