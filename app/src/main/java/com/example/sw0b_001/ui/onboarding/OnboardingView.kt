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
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.components.OnboardingNavigationRow
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.DefaultSmsAppScreen
import com.example.sw0b_001.ui.views.WelcomeMainView
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 2

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
                    0 -> WelcomeMainView()
                    1 -> DefaultSmsAppScreen(
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
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        finishOnboarding()
                    }
                },
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 20.dp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingViewPreview() {
    AppTheme {
        OnboardingView(rememberNavController())
    }
}