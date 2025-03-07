package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class MessageContent(val from: String, val to: String, val message: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel
) {
    val inspectMode = LocalInspectionMode.current
    var recipientNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var showSelectAccountModal by remember { mutableStateOf(!inspectMode) }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }
    val context = LocalContext.current

    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
            onDismissRequest = {
                if (selectedAccount == null) {
                    navController.popBackStack()
                }
                Toast.makeText(context, "No account selected", Toast.LENGTH_SHORT).show()
            },
            onAccountSelected = { account ->
                selectedAccount = account
                from = account.account!!
                showSelectAccountModal = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { TODO("Handle send") }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = from,
                onValueChange = { },
                label = { Text("Sender") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Recipient Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = recipientNumber,
                    onValueChange = { recipientNumber = it },
                    label = { Text("Recipient Number", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    isError = verifyPhoneNumberFormat(recipientNumber),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    sendMessage(
                        context = context,
                        messageContent = MessageContent(
                            from = from,
                            to = recipientNumber,
                            message = message,
                        ),
                        account = selectedAccount!!,
                        onFailureCallback = {}
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            navController.popBackStack()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Contacts,
                        contentDescription = "Select Contact",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = { TODO("Handle select contact") })
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dialing Code Hint
            Text(
                text = "Always add the dialing code if absent. e.g +237",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message Body
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

private fun processMessageForEncryption(
    to: String,
    message: String,
    account: StoredPlatformsEntity
): String {
    return "${account.account}:$to:$message"
}

private fun verifyPhoneNumberFormat(phoneNumber: String): Boolean {
    val newPhoneNumber = phoneNumber
        .replace("[\\s-]".toRegex(), "")
    return newPhoneNumber.matches("^\\+[1-9]\\d{1,14}$".toRegex())
}


private fun sendMessage(
    context: Context,
    messageContent: MessageContent,
    account: StoredPlatformsEntity,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val availablePlatforms = Datastore.getDatastore(context)
            .availablePlatformsDao().fetch(account.name!!)
        val formattedString =
            processMessageForEncryption(messageContent.to, messageContent.message, account)

        try {
            ComposeHandlers.compose(context,
                formattedString,
                availablePlatforms,
                account,
            ) {
                onCompleteCallback()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}

@Preview(showBackground = false)
@Composable
fun MessageComposePreview() {
    AppTheme(darkTheme = false) {
        MessageComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel()
        )
    }
}