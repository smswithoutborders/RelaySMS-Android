package com.example.sw0b_001.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun OnboardingNextButton(onNext: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onNext),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Continue",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingNextButtonPreview() {
    AppTheme {
        OnboardingNextButton(onNext = {})
    }
}
