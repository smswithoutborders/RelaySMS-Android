package com.example.sw0b_001.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class MissingTokenAccountInfo(
    val platform: String,
    val accountIdentifier: String,
    val accountId: String
)

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

@Composable
fun MissingTokenInfoDialog(
    groupedAccounts: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onConfirm: (doNotShowAgain: Boolean) -> Unit
) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Missing Tokens Info") },
        text = {
            Column {
                Text("Your following accounts may have missing tokens. You might need to revoke and add them again to ensure message publishing works correctly, especially if 'Store Tokens On-device' is enabled:")
                Spacer(Modifier.height(8.dp))

                groupedAccounts.forEach { (platform, identifiers) ->
                    Text(
                        text = platform.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        } + ":",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = identifiers.joinToString(", "),
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { doNotShowAgain = !doNotShowAgain }
                ) {
                    Checkbox(
                        checked = doNotShowAgain,
                        onCheckedChange = { isChecked -> doNotShowAgain = isChecked }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Do not show this again")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(doNotShowAgain) }) {
                Text("OK")
            }
        }

    )
}