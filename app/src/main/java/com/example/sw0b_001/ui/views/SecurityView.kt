package com.example.sw0b_001.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.modals.ConfirmationModal
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityView(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showLogoutBottomSheet by remember { mutableStateOf(false) }
    var showDeleteBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
//            RelayAppBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Phone Lockscreen Options Section
                SecuritySection(title = "Phone Lockscreen options") {
                    var isLockscreenEnabled by remember { mutableStateOf(false) }
                    SecurityRowWithToggle(
                        title = "Enable lockscreen",
                        subtext = "Require lockscreen pin/fingerprint when starting the app",
                        isChecked = isLockscreenEnabled,
                        onCheckedChange = { isLockscreenEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Vault Section
                SecuritySection(title = "Vault") {
                    SecurityRow(
                        title = "Revoke Platforms",
                        subtext = "Choose which platforms you want to remove from the vault",
                        onClick = {TODO("Implement platform revoking")}
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Account Section
                SecuritySection(title = "Account") {
                    SecurityRow(
                        title = "Clear aliasing",
                        subtext = "This would delete your auth codes stored for aliasing. You can request for another code at anytime.",
                        onClick = {},
                        isEnabled = false,
                        icon = Icons.Filled.Refresh
                    )
                    SecurityRow(
                        title = "Logout",
                        subtext = "Logout of your account. You can always log back in, however your current messages would be deleted.",
                        onClick = {showLogoutBottomSheet = true},
                        icon = Icons.AutoMirrored.Filled.ExitToApp
                    )
                    SecurityRow(
                        title = "Delete",
                        subtext = "Deleting your account means deleting all your saved accounts online. You can always recreate your account once needed.",
                        onClick = {showDeleteBottomSheet = true},
                        icon = Icons.Filled.Delete
                    )
                }
            }

            if (showLogoutBottomSheet) {
                ConfirmationModal(
                    showBottomSheet = showLogoutBottomSheet,
                    onContinue = {
                        TODO("Implement logout")
                    },
                    onCancel = {
                        showLogoutBottomSheet = false
                    }
                )
            }

            if (showDeleteBottomSheet) {
                ConfirmationModal(
                    showBottomSheet = showDeleteBottomSheet,
                    onContinue = {
                        TODO("Implement delete")
                    },
                    onCancel = {
                        showDeleteBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun SecuritySection(title: String, content: @Composable () -> Unit) {
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
fun SecurityRow(
    title: String,
    subtext: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    icon: ImageVector? = null
) {
    val textColor = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
    val subtextColor = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = subtextColor
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
fun SecurityRowWithToggle(
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
            imageVector = Icons.Filled.Lock,
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
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Preview(showBackground = true)
@Composable
fun SecurityScreenPreview() {
    AppTheme(darkTheme = false) {
        SecurityView(
            navController = NavController(LocalContext.current)
        )
    }
}