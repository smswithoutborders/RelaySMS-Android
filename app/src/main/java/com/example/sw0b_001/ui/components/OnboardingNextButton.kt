package com.example.sw0b_001.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
            .size(48.dp)
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
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun OnboardingOutlinedNextButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(
            start = 22.dp,
            end = 6.dp,
            top = 6.dp,
            bottom = 6.dp
        ),
        modifier = Modifier.height(48.dp)
    ) {
        Text(
            text = "Next",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        modifier = Modifier.height(48.dp)
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
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
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
fun OnboardingTopBar(
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    showBack: Boolean = true,
    showSkip: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
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

        if (showSkip) {
            OnboardingTextButton(
                label = "Skip",
                onClick = onSkip
            )
        }
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
    isGetStarted: Boolean = false,
    isOutlined: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalArrangement = if (showBack)
            Arrangement.SpaceBetween
        else
            Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            OnboardingTextButton(label = "Back", onClick = onBack)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showSkip) {
                OnboardingTextButton(label = "Skip", onClick = onSkip)
            }

            when {
                isGetStarted -> OnboardingGetStartedButton(onClick = onNext)
                isOutlined -> OnboardingOutlinedNextButton(onClick = onNext)
                else -> OnboardingCircleButton(
                    icon = if (isDone) R.drawable.arrow_forward
                    else R.drawable.arrow_forward,
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