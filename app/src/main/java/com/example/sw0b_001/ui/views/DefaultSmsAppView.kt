package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
fun DefaultSmsAppScreen(
    onSetDefaultSms: () -> Unit,
    onDone: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = 1,
                pageCount = 2,
                isDone = true,
                onClick = onDone
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.try_sending_message_illus),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Make RelaySMS your default SMS app",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To send attachments and enjoy the best experience, make RelaySMS your default SMS app.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSetDefaultSms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text("Set default SMS app")
            }

            Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultSmsAppScreenPreview() {
    AppTheme(darkTheme = false) {
        DefaultSmsAppScreen(
            onSetDefaultSms = {},
            onDone = {},
        )
    }
}