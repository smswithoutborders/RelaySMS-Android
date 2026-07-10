package com.example.sw0b_001.ui.views

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
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.navigation.HomepageScreen
import kotlinx.coroutines.launch
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.theme.AppTheme

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.sw0b_001.ui.utils.requestDefaultSmsApp


@Composable
fun WelcomeMainView(
    navController: NavController
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState {
        2
    }

    val activity = LocalContext.current as Activity

    val scope = rememberCoroutineScope()
    fun finishOnboarding() {
        context.settingsSetOnboardedCompletely(true)
        navController.navigate(HomepageScreen) {
            popUpTo(0)
            launchSingleTop = true
        }
    }
    val pages = listOf(
        OnboardingPageData(
            illustration = painterResource(R.drawable.relay_sms_welcome),
            title = "Welcome to RelaySMS",
            description =
                "Send encrypted emails and online updates using SMS. No internet required.",
            cardTitle = "Zero Accounts, Zero Passwords",
            cardDescription =
                "Your device is completely independent. We never ask you to register or log in.",
            cardIcon = painterResource(R.drawable.lock_open_right)
        ),

        OnboardingPageData(
            illustration = painterResource(R.drawable.relay_to_account),
            title = "Make RelaySMS your default SMS app",
            description =
                "RelaySMS needs to be your default SMS app so it can send and receive secure messages.",
            buttonText = "Set default SMS app",
            buttonAction = {

                requestDefaultSmsApp(activity)

            }
        )
    )
    Scaffold(
        bottomBar = {
            BottomNavigationSection(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                onContinue = {
                    if (pagerState.currentPage == 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    } else {
                        finishOnboarding()
                    }
                }
            )
        }

    ) { padding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            OnboardingPage(
                data = pages[page]
            )
        }
    }
}

@Composable
private fun BottomNavigationSection(

    currentPage: Int,

    pageCount: Int,

    onContinue: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
//            horizontalArrangement = if (showBack)
//                Arrangement.SpaceBetween
//            else
//                Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (index == currentPage) 22.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(),
            shape = CircleShape
        ) {
            Text(
                if (currentPage == 0)
                    "Continue"
                else
                    "Done!",
            )
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "WelcomeMainViewLight", showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "WelcomeMainViewDark", showBackground = true)
@Composable
fun WelcomeMainViewPreview() {
    AppTheme {
        WelcomeMainView(
            navController = rememberNavController()
        )
    }
}