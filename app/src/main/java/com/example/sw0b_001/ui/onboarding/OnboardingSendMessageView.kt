package com.example.sw0b_001.ui.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNextButton
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun OnboardingSendMessageView(
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Row(
                    modifier = Modifier.clickable(onClick = onSkip),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Illustration
            Image(
                painter = painterResource(id = R.drawable.try_sending_message_illus),
                contentDescription = "Onboarding Send Illustration",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Text
            Text(
                text = "You can also send out emails without a Vault account",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtext 1
            Text(
                text = "This will create a default email (your_phonenumber@relaysms.me) using your phone number.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(144.dp))

            OnboardingNextButton(onNext = onContinue)

        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingSendViewPreview() {
    AppTheme(darkTheme = true) {
        OnboardingSendMessageView(
            onBack = {},
            onSkip = {},
            onContinue = {}
        )
    }
}