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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.HomepageView
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class MissingTokenAccountInfo(
    val platform: String,
    val accountIdentifier: String,
    val accountId: String
)

@Composable
fun MissingTokenInfoDialog(
    groupedAccounts: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onConfirm: (doNotShowAgain: Boolean) -> Unit
) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.missing_tokens_info)) },
        text = {
            Column {
                Text(stringResource(R.string.missing_token_text))
                Spacer(Modifier.height(8.dp))

                groupedAccounts.forEach { (platform, identifiers) ->
                    Text(
                        text = platform.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                            else it.toString()
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
                    Text(stringResource(R.string.do_not_show_this_again))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(doNotShowAgain) }) {
                Text(stringResource(R.string.ok))
            }
        }

    )
}

@Preview(showBackground = false)
@Composable
fun HomepageViewLoggedInMessages_Preview() {
    AppTheme(darkTheme = false) {
        val map: MutableMap<String, List<String>> = mutableMapOf()
        map["one"] = listOf<String>("one_1", "one_2")
        MissingTokenInfoDialog(
            groupedAccounts = map,
            {}
        ) {}
    }
}


