package com.example.sw0b_001.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNavigationRow
import com.example.sw0b_001.ui.components.OnboardingTopBar
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun ChooseMessageModeScreen(
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val UnboundedFontFamily = FontFamily(
        Font(R.font.unbounded_regular, FontWeight.Normal),
        Font(R.font.unbounded_semibold, FontWeight.Bold)
    )
    val MonosanFontFamily = FontFamily(
        Font(R.font.mona_sans_regular, FontWeight.Normal),
        Font(R.font.mona_sans_semibold, FontWeight.Bold)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        OnboardingTopBar(
            onBack = onBack,
            onSkip = onSkip,
            showBack = true,
            showSkip = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Choose how you want to\nsend a message",
                fontFamily = UnboundedFontFamily,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            MessageOptionCard(
                title = "Relay account (Default)",
                badge = "NO SETUP REQUIRED",
                badgeContainerColor = MaterialTheme.colorScheme.errorContainer,
                badgeContentColor = MaterialTheme.colorScheme.onErrorContainer,
                description = "Send with your RelaySMS email alias created with your phone number, e.g. 12345689@relaysms.me.",
                imageRes = R.drawable.relay_plateforms,
                imageCentered = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "OR",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageOptionCard(
                title = "Use your online accounts",
                badge = "REQUIRES INTERNET",
                badgeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                badgeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                description = "Link your existing Gmail, Telegram, or X account(s) to send messages from your real identity.",
                imageRes = R.drawable.social_platforms,
                imageCentered = false
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        OnboardingNavigationRow(
            onNext = onNext,
            onBack = onBack,
            onSkip = onSkip,
            showBack = false,
            showSkip = false,
            isOutlined = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun MessageOptionCard(
    title: String,
    badge: String,
    badgeContainerColor: Color,
    badgeContentColor: Color,
    description: String,
    imageRes: Int,
    imageCentered: Boolean
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = MonosanFontFamily
                    )
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp
                        ),
                        color = badgeContentColor,
                        fontFamily = MonosanFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (imageCentered) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseMessageModeScreenPreview() {
    AppTheme(darkTheme = false) {
        ChooseMessageModeScreen(
            onSkip = {},
            onBack = {},
            onNext = {}
        )
    }
}