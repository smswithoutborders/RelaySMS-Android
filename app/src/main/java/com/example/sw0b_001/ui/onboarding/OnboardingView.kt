package com.example.sw0b_001.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.components.OnboardingNavigationRow
import com.example.sw0b_001.ui.components.OnboardingTopBar
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.DefaultSmsAppScreen
import com.example.sw0b_001.ui.views.OnboardingDetailView
import com.example.sw0b_001.ui.views.WelcomeMainView
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3

@Composable
fun OnboardingView(
    navController: NavController
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { PAGE_COUNT }
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun finishOnboarding() {
        context.settingsSetOnboardedCompletely(true)
        navController.navigate(HomepageScreen) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    fun goToPage(page: Int) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(page)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OnboardingTopBar(
                currentPage = pagerState.currentPage,
                onPrevious = {
                    if (pagerState.currentPage > 0) {
                        goToPage(pagerState.currentPage - 1)
                    }
                },
                onSkip = {
                    finishOnboarding()
                },
                modifier = Modifier.padding(
                    horizontal = 36.dp,
                    vertical = 30.dp
                )
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OnboardingDetailView(
                        imageRes = R.drawable.no_account,
                        description = "RelaySMS no longer asks you to login or register because your privacy matters."
                    )
                    1 -> OnboardingDetailView(
                        imageRes = R.drawable.relay_sms_save_vault,
                        description = "Save access to your accounts. It's stored securely on your device, so you can send messages as yourself."
                    )
                    2 -> DefaultSmsAppScreen(
                        navController = navController
                    )
                }
            }

            OnboardingNavigationRow(
                currentPage = pagerState.currentPage,
                pageCount = PAGE_COUNT,
                isLastPage = pagerState.currentPage == PAGE_COUNT - 1,
                onNext = {
                    if (pagerState.currentPage < PAGE_COUNT - 1) {
                        goToPage(pagerState.currentPage + 1)
                    } else {
                        finishOnboarding()
                    }
                },
                modifier = Modifier.padding(
                    horizontal = 36.dp,
                    vertical = 15.dp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingViewPreview() {
    AppTheme {
        OnboardingView(
            rememberNavController()
        )
    }
}