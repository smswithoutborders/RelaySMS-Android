package com.example.sw0b_001.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun OnboardingCircleButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun OnboardingGetStartedButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(56.dp)
    ) {
        Text(
            text = "Get Started",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingTextButton(
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun OnboardingNavigationRow(
    onNext: () -> Unit,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    showBack: Boolean = true,
    showSkip: Boolean = true,
    isDone: Boolean = false,
    isGetStarted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (showBack) {
            OnboardingTextButton(
                label = "Back",
                onClick = onBack
            )
        } else {
            Spacer(modifier = Modifier.width(64.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (showSkip) {
                OnboardingTextButton(
                    label = "Skip",
                    onClick = onSkip
                )
            }

            if (isGetStarted) {
                OnboardingGetStartedButton(onClick = onNext)
            } else {
                OnboardingCircleButton(
                    icon = if (isDone)
                        R.drawable.arrow_forward
                    else
                        R.drawable.arrow_forward,
                    contentDescription = if (isDone) "Done" else "Next",
                    onClick = onNext
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircleButtonPreview() {
    AppTheme {
        OnboardingCircleButton(
            icon = R.drawable.arrow_forward,
            contentDescription = "Next",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedPreview() {
    AppTheme {
        OnboardingNavigationRow(
            onNext = {},
            showBack = false,
            showSkip = false,
            isGetStarted = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationRowPreview() {
    AppTheme {
        OnboardingNavigationRow(
            onNext = {},
            onBack = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoneNavigationPreview() {
    AppTheme {
        OnboardingNavigationRow(
            onNext = {},
            onBack = {},
            onSkip = {},
            isDone = true
        )
    }
}