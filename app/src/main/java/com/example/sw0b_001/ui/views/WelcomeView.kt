package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets

@Composable
fun WelcomeMainView(
    navController: NavController
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState { 2 }
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
            title = "Send Messages online even when the internet is gone.",
            description = "RelaySMS Send your messages from SMS to online platforms."
        ),
        OnboardingPageData(
            illustration = painterResource(R.drawable.try_sending_message_illus),
            title = "Make RelaySMS your default SMS app",
            description = "RelaySMS needs to be your default SMS app so it can send and receive secure messages.",
            buttonText = "Set default SMS app",
            buttonAction = {}
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationSection(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                onContinue = {
                    if (pagerState.currentPage == 0) {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    } else {
                        finishOnboarding()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            OnboardingPage(data = pages[page])
        }
    }
}

@Composable
private fun BottomNavigationSection(
    currentPage: Int,
    pageCount: Int,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 32.dp,
                top = 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
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

        Button(
            onClick = onContinue,
            modifier = Modifier.height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (currentPage == 0) "Continue" else "Done!",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "WelcomeMainViewLight", showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "WelcomeMainViewDark", showBackground = true)
@Composable
fun WelcomeMainViewPreview() {
    AppTheme {
        WelcomeMainView(navController = rememberNavController())
    }
}