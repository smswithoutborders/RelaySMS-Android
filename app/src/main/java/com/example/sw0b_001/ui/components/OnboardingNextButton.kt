package com.example.sw0b_001.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R

@Composable
fun OnboardingNavigationRow(
    currentPage: Int,
    pageCount: Int,
    isLastPage: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            repeat(pageCount) { index ->
                val isSelected = index == currentPage

                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "dotWidth"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected)  MaterialTheme.colorScheme.primaryContainer else Color(0xFFD8D8D8),
                    label = "dotColor"
                )

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        ContinueButton(
            text = if (isLastPage) "Done" else "Continue",
            outlined = isLastPage,
            onClick = onNext
        )
    }
}

@Composable
private fun ContinueButton(
    text: String,
    outlined: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor = if (outlined) Color.White else  MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (outlined)  MaterialTheme.colorScheme.primaryContainer else Color.White
    val iconBoxColor = if (outlined)  MaterialTheme.colorScheme.primaryContainer else Color.White
    val iconTint = if (outlined) Color.White else  MaterialTheme.colorScheme.primaryContainer


    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    val iconOffset by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconOffset"
    )

    Surface(
        modifier = Modifier
            .width(180.dp)
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                onClick = onClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        border = if (outlined) BorderStroke(1.5.dp,  MaterialTheme.colorScheme.primaryContainer) else null
    ) {
        Row(
            modifier = Modifier
                .height(50.dp)
                .width(160.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBoxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            translationX = iconOffset
                        }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}


@Preview(showBackground = true, name = "First page")
@Composable
private fun OnboardingNavigationRowFirstPagePreview() {
    MaterialTheme {
        Surface(color = Color.White) {
            OnboardingNavigationRow(
                currentPage = 0,
                pageCount = 3,
                isLastPage = false,
                onNext = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Preview(showBackground = true, name = "Last page (Done)")
@Composable
private fun OnboardingNavigationRowLastPagePreview() {
    MaterialTheme {
        Surface(color = Color.White) {
            OnboardingNavigationRow(
                currentPage = 2,
                pageCount = 3,
                isLastPage = true,
                onNext = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}