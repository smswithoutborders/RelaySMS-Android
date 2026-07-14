package com.example.sw0b_001.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingBottomBar
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun WelcomeMainView(
    onContinue: () -> Unit
) {

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = 0,
                pageCount = 2,
                isDone = false,
                onClick = onContinue
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 28.dp,
                    vertical = padding.calculateBottomPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(90.dp)
            )

            Image(
                painter = painterResource(R.drawable.welcome_group),
                contentDescription = null,
                modifier = Modifier.size(220.dp)
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Send messages online even when the internet is gone",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp
                ),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "RelaySMS sends your messages from SMS to online Platforms.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp
                )
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

        }

    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "WelcomeDark", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
fun WelcomeMainViewPreview() {
    AppTheme {
        WelcomeMainView(onContinue = {})
    }
}