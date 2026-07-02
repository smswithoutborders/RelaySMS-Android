package com.example.sw0b_001.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.components.OnboardingNavigationRow
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

val UnboundedFontFamily = FontFamily(
    Font(R.font.unbounded_regular, FontWeight.Normal),
    Font(R.font.unbounded_semibold, FontWeight.Bold)
)

val MonasansFontFamily = FontFamily(
    Font(R.font.mona_sans_medium, FontWeight.Normal),
    Font(R.font.mona_sans_semibold, FontWeight.Bold)
)

@Composable
fun WelcomeMainView(navController: NavController) {

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun finishOnboarding() {
        context.settingsSetOnboardedCompletely(true)
        navController.navigate(HomepageScreen) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> PageOneContent()

                    1 -> ChooseMessageModeScreen(
                        onSkip = { finishOnboarding() },
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onNext = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )

                    2 -> DefaultSmsAppScreen(
                        navController = navController,
                        onSkip = { finishOnboarding() },
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onSetDefault = { },
                        onDone = { finishOnboarding() }
                    )
                }
            }

            if (pagerState.currentPage == 0) {
                OnboardingNavigationRow(
                    onNext = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    showBack = false,
                    showSkip = false,
                    isGetStarted = true,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun PageOneContent() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.relay_sms_welcome),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Welcome to RelaySMS",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Send encrypted emails and online updates using SMS. No internet required.",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontFamily = MonasansFontFamily,
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(50.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lock_open_right),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Zero Accounts, Zero Passwords",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = UnboundedFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your device is completely independent. We never ask you to register or log in.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontFamily = MonasansFontFamily,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "DefaultPreviewDark", group = "Default")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "DefaultPreviewLight")
@Composable
fun WelcomeMainViewPreview() {
    AppTheme {
        WelcomeMainView(rememberNavController())
    }
}