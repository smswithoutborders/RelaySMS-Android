package com.example.sw0b_001.ui.onboarding

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNextButton
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun OnboardingVaultStoreView(
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            Spacer(modifier = Modifier.height(72.dp))

            // Vault Store Illustration
            Image(
                painter = painterResource(id = R.drawable.vault_illus),
                contentDescription = "Vault Store Illustration",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(72.dp))

            // Vault Store Text
            Text(
                text = "RelaySMS Vaults securely stores your online accounts, so that you can access them without an internet connection",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(144.dp))

            // Continue Button
            OnboardingNextButton(onNext = onContinue)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingVaultStorePreview() {
    AppTheme(darkTheme = false) {
        OnboardingVaultStoreView (onBack = {}, onSkip = {}, onContinue = {})
    }
}