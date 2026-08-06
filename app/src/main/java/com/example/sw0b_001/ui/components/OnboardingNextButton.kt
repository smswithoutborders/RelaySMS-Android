package com.example.sw0b_001.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.R


@Composable
fun OnboardingTopBar(
    currentPage: Int,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Previous",
            color =
                if (currentPage == 0)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = .35f)
                else
                    MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                enabled = currentPage > 0,
                indication = null,
                interactionSource = MutableInteractionSource()
            ) {
                onPrevious()
            }
        )

        Text(
            text = "Skip",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = MutableInteractionSource()
            ) {
                onSkip()
            }
        )
    }
}

@Composable
fun OnboardingNavigationRow(
    currentPage: Int,
    pageCount: Int,
    isLastPage: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = when (currentPage) {
        0 -> "Awesome!"
        1 -> "Got it!"
        else -> "Done!"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row {
            repeat(pageCount) { index ->

                Box(
                    modifier = Modifier
                        .width(if (index == currentPage) 20.dp else 8.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.LightGray
                        )
                )

                if (index != pageCount - 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            onClick = onNext,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(50.dp)
        ) {

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .width(190.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = buttonText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Preview(
    name = "Top Bar - Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Top Bar - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun OnboardingTopBarPreview() {
    AppTheme {
        OnboardingTopBar(
            currentPage = 1,
            onPrevious = {},
            onSkip = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Navigation Row - Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Navigation Row - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun OnboardingNavigationRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OnboardingNavigationRow(
                currentPage = 1,
                pageCount = 3,
                isLastPage = false,
                onNext = {}
            )
        }
    }
}