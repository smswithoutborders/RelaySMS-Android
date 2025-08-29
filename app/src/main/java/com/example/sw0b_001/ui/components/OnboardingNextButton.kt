package com.example.sw0b_001.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun OnboardingNextButton(onNext: () -> Unit, nextButtonText: String) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onNext),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = nextButtonText, color = MaterialTheme.colorScheme.onPrimary)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.continue_text),
                modifier = Modifier
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingNextButtonPreview() {
    AppTheme {
        OnboardingNextButton(onNext = {}, nextButtonText = "Continue")
    }
}
