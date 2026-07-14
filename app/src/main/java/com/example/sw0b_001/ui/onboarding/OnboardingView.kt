package com.example.sw0b_001.ui.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.example.sw0b_001.extensions.context.settingsSetOnboardedCompletely
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.views.DefaultSmsAppScreen
import com.example.sw0b_001.ui.views.WelcomeMainView
import com.example.sw0b_001.ui.views.threads.makeDefault
import kotlinx.coroutines.launch

@Composable
fun OnboardingView(
    navController: NavController
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    fun goToHomepage() {
        context.settingsSetOnboardedCompletely(true)
        navController.navigate(HomepageScreen) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    LaunchedEffect(Unit) {
        if (!previewMode && context.isDefault()) {
            goToHomepage()
        }
    }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        if (context.isDefault()) {
            goToHomepage()
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> WelcomeMainView(
                onContinue = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
            1 -> DefaultSmsAppScreen(
                onSetDefaultSms = {
                    getDefaultPermission.launch(makeDefault(context))
                },
                onDone = ::goToHomepage
            )
        }
    }
}