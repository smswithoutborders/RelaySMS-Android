package com.example.sw0b_001.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun ChooseMessageModeScreen(
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    var selected by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                modifier = Modifier.clickable { onSkip() },
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Choose how you want to\nsend a message.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        MessageOptionCard(
            selected = selected == 0,
            title = "Relay account (Default)",
            badge = "NO SETUP REQUIRED",
            badgeContainerColor = MaterialTheme.colorScheme.errorContainer,
            badgeContentColor = MaterialTheme.colorScheme.onErrorContainer,
            description = "Send with your RelaySMS email alias created with your phone number, e.g. 12345689@relaysms.me.",
            imageRes = R.drawable.relay_to_account,
            imageCentered = true,
            onClick = { selected = 0 }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Or",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        MessageOptionCard(
            selected = selected == 1,
            title = "Use your online accounts",
            badge = "REQUIRES INTERNET",
            badgeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            badgeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            description = "Link your existing Gmail, Telegram, or X account(s) to send messages from your real identity.",
            imageRes = R.drawable.relay_sms_save_vault,
            imageCentered = false,
            onClick = { selected = 1 }
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomNavigationRow(
            currentStep = 1,
            onBack = onBack,
            onNext = onNext
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MessageOptionCard(
    selected: Boolean,
    title: String,
    badge: String,
    badgeContainerColor: Color,
    badgeContentColor: Color,
    description: String,
    imageRes: Int,
    imageCentered: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeContentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (imageCentered) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(190.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
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

@Composable
private fun BottomNavigationRow(
    currentStep: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            repeat(3) { index ->

                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentStep) 22.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedButton(
            onClick = onNext,
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(
                start = 22.dp,
                end = 6.dp,
                top = 6.dp,
                bottom = 6.dp
            )
        ) {

            Text("Next")

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
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