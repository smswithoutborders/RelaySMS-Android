package com.example.sw0b_001.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R

@Composable
fun OnboardingContinueButton(
    text: String = "Continue",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(52.dp),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(start = 22.dp, end = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp
        )
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.weight(1f))

        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onPrimary
        ) {

            Box(
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


@Composable
fun OnboardingDoneButton(
    text: String = "Done!",
    onClick: () -> Unit
) {

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(52.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(start = 22.dp, end = 6.dp)
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.weight(1f))

        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )

            }

        }

    }

}@Composable
fun OnboardingPagerIndicator(
    currentPage: Int,
    pageCount: Int
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(pageCount) { page ->

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(
                        if (page == currentPage) 22.dp else 8.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (page == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = .25f)
                    )
            )

        }

    }

}




@Composable
fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OnboardingPagerIndicator(
            currentPage = currentPage,
            pageCount = pageCount
        )

        Spacer(Modifier.weight(1f))

        if (isDone) {
            OnboardingDoneButton(onClick = onClick)
        } else {
            OnboardingContinueButton(onClick = onClick)
        }

    }

}