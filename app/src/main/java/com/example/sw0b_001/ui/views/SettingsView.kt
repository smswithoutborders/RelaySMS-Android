package com.example.sw0b_001.ui.views


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.components.LanguageOption
import com.example.sw0b_001.ui.components.LanguageSelectionPopup
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun SettingsView(
    navController: NavController,
) {
    var showLanguagePopup by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(LanguageOption("English", "en")) }
    Scaffold(
        topBar = {
            RelayAppBar(screenName = "Settings", navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Accessibility Section
            SettingsSection(title = "Accessibility") {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = "Language",
                    subtext = currentLanguage.name,
                    onClick = { showLanguagePopup = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Messaging Section
            SettingsSection(title = "Messaging") {
                var isPhoneValidationEnabled by remember { mutableStateOf(false) }
                SettingsRowWithToggle(
                    icon = Icons.AutoMirrored.Filled.Message,
                    title = "Validate with phone number",
                    subtext = "You have a Device ID for the device you log into. It is used to identify you on the Vault. You can switch to phone number to reduce size of SMS message.",
                    isChecked = isPhoneValidationEnabled,
                    onCheckedChange = { isPhoneValidationEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security and Privacy Section
            SettingsSection(title = "Security and Privacy") {
                SettingsRow(
                    icon = Icons.Filled.Security,
                    title = "Security",
                    subtext = "Enable app locks and pin codes",
                    onClick = {}
                )
            }
        }

        if (showLanguagePopup) {
            LanguageSelectionPopup(
                currentLanguageCode = currentLanguage.code,
                onLanguageSelected = { selectedLanguage ->
                    currentLanguage = selectedLanguage
                    // TODO: Implement language change logic
                },
                onDismiss = { showLanguagePopup = false }
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtext: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
fun SettingsRowWithToggle(
    icon: ImageVector,
    title: String,
    subtext: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme(darkTheme = false) {
        SettingsView(navController = NavController(LocalContext.current))
    }
}