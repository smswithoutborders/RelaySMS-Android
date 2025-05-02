package com.example.sw0b_001.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R

@Composable
fun MissingTokenDialog(
    account: StoredPlatformsEntity,
    onDismiss: () -> Unit,
    onRevoke: (StoredPlatformsEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Missing Tokens") },
        text = {
            Text("Tokens not found for ${account.account}. Please revoke and store again\"")

        },
        confirmButton = {
            TextButton (onClick = { onRevoke(account) }) {
                Text("Revoke")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}